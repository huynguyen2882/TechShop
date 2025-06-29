package com.example.techshop.Activity

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.Observer
import com.example.techshop.Helper.FavoriteManager
import com.example.techshop.Model.ItemsModel
import com.example.techshop.Model.FilterOptions
import com.example.techshop.Model.SortOption
import com.example.techshop.R
import com.example.techshop.ViewModel.MainViewModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.delay

class ListItemsActivity : BaseActivity() {
    private val viewModel = MainViewModel()
    private var id: String = ""
    private var title: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        id = intent.getStringExtra("id") ?: ""
        title = intent.getStringExtra("title") ?: ""

        setContent {
            ListItemScreen(
                title = title,
                onBackClick = { finish() },
                viewModel = viewModel,
                id = id
            )
        }
    }
}

@Composable
fun ListItemScreen(
    title: String,
    onBackClick: () -> Unit,
    viewModel: MainViewModel,
    id: String
) {
    var items by remember { mutableStateOf(listOf<ItemsModel>()) }
    var isLoading by remember { mutableStateOf(true) }
    var showFilterDialog by remember { mutableStateOf(false) }
    var filterOptions by remember { mutableStateOf(FilterOptions()) }
    var filteredItems by remember { mutableStateOf(listOf<ItemsModel>()) }

    // Thêm trạng thái loading cho danh sách yêu thích
    val favoriteItems by FavoriteManager.favoriteItems.collectAsStateWithLifecycle()
    var isFavoritesLoaded by remember { mutableStateOf(false) }

    // Trích xuất các tùy chọn filter từ danh sách sản phẩm
    val availableBrands = remember(items) {
        items.flatMap { item: ItemsModel ->
            extractBrandsFromDescription(item.description)
        }.distinct().sorted()
    }

    val availableRamOptions = remember(items) {
        items.flatMap { item: ItemsModel ->
            extractRamFromDescription(item.description)
        }.distinct().sorted()
    }

    val availableStorageOptions = remember(items) {
        items.flatMap { item: ItemsModel ->
            extractStorageFromDescription(item.description)
        }.distinct().sorted()
    }

    LaunchedEffect(id) {
        // Tải danh sách yêu thích
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            delay(100) // Đảm bảo FavoriteManager đã khởi tạo
            isFavoritesLoaded = true
        } else {
            isFavoritesLoaded = true // Không cần tải nếu chưa đăng nhập
        }

        // Tải danh sách sản phẩm
        viewModel.loadFiltered(id)
        isLoading = false
    }

    // Lắng nghe thay đổi từ viewModel
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = androidx.lifecycle.Observer<List<ItemsModel>> { newItems ->
            items = newItems
        }
        viewModel.recommended.observe(lifecycleOwner, observer)
        onDispose {
            viewModel.recommended.removeObserver(observer)
        }
    }

    // Áp dụng filter khi items hoặc filterOptions thay đổi
    LaunchedEffect(items, filterOptions) {
        filteredItems = applyFilters(items, filterOptions)
    }

    Column(modifier = Modifier.fillMaxSize()) {
        ConstraintLayout(modifier = Modifier.padding(top = 36.dp, start = 16.dp, end = 16.dp)) {
            val (backBtn, cartTxt, filterBtn) = createRefs()

            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .constrainAs(cartTxt) { centerTo(parent) },
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold,
                fontSize = 25.sp,
                text = title
            )

            Image(
                painter = painterResource(R.drawable.back),
                contentDescription = null,
                modifier = Modifier
                    .clickable {
                        onBackClick()
                    }
                    .constrainAs(backBtn) {
                        top.linkTo(parent.top)
                        bottom.linkTo(parent.bottom)
                        start.linkTo(parent.start)
                    }
            )

            // Nút filter
            IconButton(
                onClick = { showFilterDialog = true },
                modifier = Modifier.constrainAs(filterBtn) {
                    top.linkTo(parent.top)
                    bottom.linkTo(parent.bottom)
                    end.linkTo(parent.end)
                }
            ) {
                Icon(
                    imageVector = Icons.Default.FilterList,
                    contentDescription = "Filter",
                    tint = colorResource(R.color.purple),
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        // Hiển thị active filters
        if (filterOptions.priceRange.first > 0.0 ||
            filterOptions.priceRange.second < Double.MAX_VALUE ||
            filterOptions.brands.isNotEmpty() ||
            filterOptions.ramOptions.isNotEmpty() ||
            filterOptions.storageOptions.isNotEmpty() ||
            filterOptions.sortByPrice != SortOption.NONE) {
            ActiveFiltersChips(
                filterOptions = filterOptions,
                onRemoveFilter = { newFilterOptions ->
                    filterOptions = newFilterOptions
                }
            )
        }

        if (isLoading || !isFavoritesLoaded) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (filteredItems.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Không có sản phẩm nào phù hợp với bộ lọc",
                    fontSize = 16.sp,
                    color = Color.Gray
                )
            }
        } else {
            ListItemsFullSize(filteredItems)
        }
    }

    // Filter Dialog
    if (showFilterDialog) {
        FilterDialog(
            filterOptions = filterOptions,
            availableBrands = availableBrands,
            availableRamOptions = availableRamOptions,
            availableStorageOptions = availableStorageOptions,
            onDismiss = { showFilterDialog = false },
            onApplyFilter = { newFilterOptions ->
                filterOptions = newFilterOptions
                showFilterDialog = false
            }
        )
    }

    LaunchedEffect(items) {
        isLoading = items.isEmpty()
    }
}

@Composable
fun ActiveFiltersChips(
    filterOptions: FilterOptions,
    onRemoveFilter: (FilterOptions) -> Unit
) {
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Hiển thị chip cho price range
        if (filterOptions.priceRange.first > 0.0 || filterOptions.priceRange.second < Double.MAX_VALUE) {
            item {
                FilterChip(
                    selected = true,
                    onClick = {
                        onRemoveFilter(filterOptions.copy(priceRange = Pair(0.0, Double.MAX_VALUE)))
                    },
                    label = {
                        Text(
                            text = "Giá: ${filterOptions.priceRange.first.toInt()}₫ - ${if (filterOptions.priceRange.second == Double.MAX_VALUE) "∞" else filterOptions.priceRange.second.toInt()}₫",
                            fontSize = 12.sp
                        )
                    },
                    trailingIcon = {
                        Icon(
                            painter = painterResource(R.drawable.ic_fav), // Sử dụng icon có sẵn
                            contentDescription = "Remove",
                            modifier = Modifier.size(16.dp)
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = colorResource(R.color.purple),
                        selectedLabelColor = Color.White
                    )
                )
            }
        }

        // Hiển thị chip cho brands
        filterOptions.brands.forEach { brand ->
            item {
                FilterChip(
                    selected = true,
                    onClick = {
                        val newBrands = filterOptions.brands.toMutableSet()
                        newBrands.remove(brand)
                        onRemoveFilter(filterOptions.copy(brands = newBrands))
                    },
                    label = { Text("Hãng: $brand", fontSize = 12.sp) },
                    trailingIcon = {
                        Icon(
                            painter = painterResource(R.drawable.ic_fav),
                            contentDescription = "Remove",
                            modifier = Modifier.size(16.dp)
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = colorResource(R.color.purple),
                        selectedLabelColor = Color.White
                    )
                )
            }
        }

        // Hiển thị chip cho RAM
        filterOptions.ramOptions.forEach { ram ->
            item {
                FilterChip(
                    selected = true,
                    onClick = {
                        val newRamOptions = filterOptions.ramOptions.toMutableSet()
                        newRamOptions.remove(ram)
                        onRemoveFilter(filterOptions.copy(ramOptions = newRamOptions))
                    },
                    label = { Text("RAM: $ram", fontSize = 12.sp) },
                    trailingIcon = {
                        Icon(
                            painter = painterResource(R.drawable.ic_fav),
                            contentDescription = "Remove",
                            modifier = Modifier.size(16.dp)
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = colorResource(R.color.purple),
                        selectedLabelColor = Color.White
                    )
                )
            }
        }

        // Hiển thị chip cho Storage
        filterOptions.storageOptions.forEach { storage ->
            item {
                FilterChip(
                    selected = true,
                    onClick = {
                        val newStorageOptions = filterOptions.storageOptions.toMutableSet()
                        newStorageOptions.remove(storage)
                        onRemoveFilter(filterOptions.copy(storageOptions = newStorageOptions))
                    },
                    label = { Text("Bộ nhớ: $storage", fontSize = 12.sp) },
                    trailingIcon = {
                        Icon(
                            painter = painterResource(R.drawable.ic_fav),
                            contentDescription = "Remove",
                            modifier = Modifier.size(16.dp)
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = colorResource(R.color.purple),
                        selectedLabelColor = Color.White
                    )
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterDialog(
    filterOptions: FilterOptions,
    availableBrands: List<String>,
    availableRamOptions: List<String>,
    availableStorageOptions: List<String>,
    onDismiss: () -> Unit,
    onApplyFilter: (FilterOptions) -> Unit
) {
    var tempFilterOptions by remember { mutableStateOf(filterOptions) }
    var minPrice by remember { mutableStateOf(filterOptions.priceRange.first.toString()) }
    var maxPrice by remember { mutableStateOf(if (filterOptions.priceRange.second == Double.MAX_VALUE) "" else filterOptions.priceRange.second.toString()) }
    var selectedBrands by remember { mutableStateOf(filterOptions.brands.toMutableSet()) }
    var selectedRamOptions by remember { mutableStateOf(filterOptions.ramOptions.toMutableSet()) }
    var selectedStorageOptions by remember { mutableStateOf(filterOptions.storageOptions.toMutableSet()) }
    var sortByPrice by remember { mutableStateOf(filterOptions.sortByPrice) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = false
        )
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .heightIn(max = 600.dp),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Bộ lọc sản phẩm",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF4A548D)
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.FilterList,
                            contentDescription = "Đóng",
                            tint = Color.Gray
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Khoảng giá
                Text(
                    text = "Khoảng giá",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF4A548D)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = minPrice,
                        onValueChange = { minPrice = it },
                        label = { Text("Từ (₫)") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number
                        )
                    )
                    OutlinedTextField(
                        value = maxPrice,
                        onValueChange = { maxPrice = it },
                        label = { Text("Đến (₫)") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number
                        )
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Sắp xếp theo giá
                Text(
                    text = "Sắp xếp theo giá",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF4A548D)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = sortByPrice == SortOption.NONE,
                        onClick = { sortByPrice = SortOption.NONE },
                        label = { Text("Không sắp xếp") },
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = sortByPrice == SortOption.ASCENDING,
                        onClick = { sortByPrice = SortOption.ASCENDING },
                        label = { Text("Tăng dần") },
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = sortByPrice == SortOption.DESCENDING,
                        onClick = { sortByPrice = SortOption.DESCENDING },
                        label = { Text("Giảm dần") },
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Hãng sản xuất
                if (availableBrands.isNotEmpty()) {
                    Text(
                        text = "Hãng sản xuất",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF4A548D)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(availableBrands) { brand ->
                            FilterChip(
                                selected = selectedBrands.contains(brand),
                                onClick = {
                                    if (selectedBrands.contains(brand)) {
                                        selectedBrands.remove(brand)
                                    } else {
                                        selectedBrands.add(brand)
                                    }
                                },
                                label = { Text(brand) }
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // RAM
                if (availableRamOptions.isNotEmpty()) {
                    Text(
                        text = "RAM",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF4A548D)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(availableRamOptions) { ram ->
                            FilterChip(
                                selected = selectedRamOptions.contains(ram),
                                onClick = {
                                    if (selectedRamOptions.contains(ram)) {
                                        selectedRamOptions.remove(ram)
                                    } else {
                                        selectedRamOptions.add(ram)
                                    }
                                },
                                label = { Text(ram) }
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Bộ nhớ
                if (availableStorageOptions.isNotEmpty()) {
                    Text(
                        text = "Bộ nhớ",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF4A548D)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(availableStorageOptions) { storage ->
                            FilterChip(
                                selected = selectedStorageOptions.contains(storage),
                                onClick = {
                                    if (selectedStorageOptions.contains(storage)) {
                                        selectedStorageOptions.remove(storage)
                                    } else {
                                        selectedStorageOptions.add(storage)
                                    }
                                },
                                label = { Text(storage) }
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            // Reset all filters
                            minPrice = ""
                            maxPrice = ""
                            selectedBrands.clear()
                            selectedRamOptions.clear()
                            selectedStorageOptions.clear()
                            sortByPrice = SortOption.NONE
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Đặt lại")
                    }
                    Button(
                        onClick = {
                            val newFilterOptions = FilterOptions(
                                priceRange = Pair(
                                    minPrice.toDoubleOrNull() ?: 0.0,
                                    maxPrice.toDoubleOrNull() ?: Double.MAX_VALUE
                                ),
                                brands = selectedBrands,
                                ramOptions = selectedRamOptions,
                                storageOptions = selectedStorageOptions,
                                sortByPrice = sortByPrice
                            )
                            onApplyFilter(newFilterOptions)
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = colorResource(R.color.purple))
                    ) {
                        Text("Áp dụng")
                    }
                }
            }
        }
    }
}

// Hàm trích xuất thông tin từ description
fun extractBrandsFromDescription(description: String): List<String> {
    val brands = mutableListOf<String>()
    val brandPatterns = listOf(
        Regex("(?i)(iphone|samsung|xiaomi|oppo|vivo|realme|oneplus|huawei|nokia|motorola|lg|sony|asus|lenovo|acer|dell|hp|msi|razer|alienware)"),
        Regex("(?i)(apple|google|microsoft)")
    )

    brandPatterns.forEach { pattern ->
        pattern.findAll(description).forEach { matchResult ->
            brands.add(matchResult.value.lowercase().capitalize())
        }
    }

    return brands.distinct()
}

fun extractRamFromDescription(description: String): List<String> {
    val ramPattern = Regex("(?i)(\\d+)\\s*(gb|mb)\\s*ram")
    return ramPattern.findAll(description).map { matchResult ->
        "${matchResult.groupValues[1]} ${matchResult.groupValues[2].uppercase()}"
    }.distinct().toList()
}

fun extractStorageFromDescription(description: String): List<String> {
    val storagePattern = Regex("(?i)(\\d+)\\s*(gb|tb|mb)\\s*(?:storage|rom|ssd|hdd)")
    return storagePattern.findAll(description).map { matchResult ->
        "${matchResult.groupValues[1]} ${matchResult.groupValues[2].uppercase()}"
    }.distinct().toList()
}

// Hàm áp dụng filter
fun applyFilters(items: List<ItemsModel>, filterOptions: FilterOptions): List<ItemsModel> {
    var filteredItems = items

    // Filter theo khoảng giá
    filteredItems = filteredItems.filter { item ->
        item.price >= filterOptions.priceRange.first &&
                item.price <= filterOptions.priceRange.second
    }

    // Filter theo hãng
    if (filterOptions.brands.isNotEmpty()) {
        filteredItems = filteredItems.filter { item ->
            val itemBrands = extractBrandsFromDescription(item.description)
            itemBrands.any { it in filterOptions.brands }
        }
    }

    // Filter theo RAM
    if (filterOptions.ramOptions.isNotEmpty()) {
        filteredItems = filteredItems.filter { item ->
            val itemRamOptions = extractRamFromDescription(item.description)
            itemRamOptions.any { it in filterOptions.ramOptions }
        }
    }

    // Filter theo bộ nhớ
    if (filterOptions.storageOptions.isNotEmpty()) {
        filteredItems = filteredItems.filter { item ->
            val itemStorageOptions = extractStorageFromDescription(item.description)
            itemStorageOptions.any { it in filterOptions.storageOptions }
        }
    }

    // Sắp xếp theo giá
    filteredItems = when (filterOptions.sortByPrice) {
        SortOption.ASCENDING -> filteredItems.sortedBy { it.price }
        SortOption.DESCENDING -> filteredItems.sortedByDescending { it.price }
        SortOption.NONE -> filteredItems
    }

    return filteredItems
}

// Extension function để capitalize
fun String.capitalize(): String {
    return if (this.isNotEmpty()) {
        this[0].uppercase() + this.substring(1).lowercase()
    } else {
        this
    }
}