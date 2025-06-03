package com.example.techshop.Activity

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.techshop.Model.ItemsModel
import com.example.techshop.R
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class SearchResultsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SearchResultsScreen()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchResultsScreen() {
    val context = LocalContext.current
    val query = (context as? ComponentActivity)?.intent?.getStringExtra("query")?.trim() ?: ""
    val firebaseDatabase = FirebaseDatabase.getInstance()
    var searchResults by remember { mutableStateOf(listOf<ItemsModel>()) }
    var filteredResults by remember { mutableStateOf(listOf<ItemsModel>()) }
    var isLoading by remember { mutableStateOf(true) }
    var showFilterDialog by remember { mutableStateOf(false) }

    // Trạng thái bộ lọc
    var sortByPrice by remember { mutableStateOf(SortOption.NONE) }
    var sortByRating by remember { mutableStateOf(SortOption.NONE) }

    // Trạng thái tạm thời cho bộ lọc trong dialog
    var tempSortByPrice by remember { mutableStateOf(sortByPrice) }
    var tempSortByRating by remember { mutableStateOf(sortByRating) }

    // Lấy dữ liệu từ Firebase và tìm kiếm
    LaunchedEffect(query) {
        if (query.isNotEmpty()) {
            val ref = firebaseDatabase.getReference("Items")
            ref.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val itemsWithScore = mutableListOf<Pair<ItemsModel, Float>>()
                    // Tách từ khóa thành các từ riêng lẻ
                    val keywords = query.lowercase().split(" ").filter { it.isNotEmpty() }

                    for (childSnapshot in snapshot.children) {
                        val item = childSnapshot.getValue(ItemsModel::class.java)
                        if (item != null) {
                            var score = 0f
                            // Kiểm tra dữ liệu null
                            val titleLower = item.title?.lowercase() ?: ""
                            val categoryLower = item.categoryId?.lowercase() ?: ""

                            // Tách thành các từ
                            val titleWords = titleLower.split(" ").filter { it.isNotEmpty() }
                            val categoryWords = categoryLower.split(" ").filter { it.isNotEmpty() }

                            keywords.forEach { keyword ->
                                // Khớp chính xác với từng từ
                                if (titleWords.contains(keyword)) {
                                    score += 3f
                                } else if (categoryWords.contains(keyword)) {
                                    score += 0.5f
                                }

                                // Khớp một phần với toàn bộ chuỗi
                                if (titleLower.contains(keyword) && !titleWords.contains(keyword)) {
                                    score += 2f
                                }
                                if (categoryLower.contains(keyword) && !categoryWords.contains(keyword)) {
                                    score += 0.3f
                                }
                            }

                            if (score > 0) {
                                itemsWithScore.add(Pair(item.copy(id = childSnapshot.key ?: ""), score))
                            }
                        }
                    }

                    // Sắp xếp theo trọng số giảm dần
                    searchResults = itemsWithScore.sortedByDescending { it.second }.map { it.first }
                    filteredResults = searchResults
                    isLoading = false
                }

                override fun onCancelled(error: DatabaseError) {
                    searchResults = emptyList()
                    filteredResults = emptyList()
                    isLoading = false
                }
            })
        } else {
            isLoading = false
        }
    }

    // Áp dụng bộ lọc
    LaunchedEffect(sortByPrice, sortByRating) {
        var result = searchResults
        if (sortByPrice != SortOption.NONE) {
            result = if (sortByPrice == SortOption.ASCENDING) {
                result.sortedBy { it.price }
            } else {
                result.sortedByDescending { it.price }
            }
        }
        if (sortByRating != SortOption.NONE) {
            result = if (sortByRating == SortOption.ASCENDING) {
                result.sortedBy { it.rating }
            } else {
                result.sortedByDescending { it.rating }
            }
        }
        filteredResults = result
    }

    // Hiển thị dialog bộ lọc
    AnimatedVisibility(
        visible = showFilterDialog,
        enter = fadeIn(animationSpec = tween(500)) + scaleIn(animationSpec = tween(500)),
        exit = fadeOut(animationSpec = tween(500)) + scaleOut(animationSpec = tween(500))
    ) {
        AlertDialog(
            onDismissRequest = { showFilterDialog = false },
            modifier = Modifier
                .shadow(12.dp, RoundedCornerShape(20.dp))
                .background(Color.White, RoundedCornerShape(20.dp))
                .padding(16.dp),
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.FilterList,
                        contentDescription = "Filter Icon",
                        tint = colorResource(R.color.purple),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Lọc kết quả",
                        color = colorResource(R.color.purple),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            text = {
                Column {
                    Text(
                        "Sắp xếp theo giá",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(3),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(70.dp)
                    ) {
                        item {
                            FilterChip(
                                selected = tempSortByPrice == SortOption.ASCENDING,
                                onClick = { tempSortByPrice = SortOption.ASCENDING },
                                label = { Text("Thấp đến cao", fontSize = 12.sp) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .shadow(
                                        elevation = if (tempSortByPrice == SortOption.ASCENDING) 4.dp else 0.dp,
                                        shape = RoundedCornerShape(12.dp)
                                    ),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = colorResource(R.color.purple),
                                    selectedLabelColor = Color.White,
                                    containerColor = Color(0xFFF5F5F5),
                                    labelColor = Color.Black
                                ),
                                shape = RoundedCornerShape(12.dp)
                            )
                        }
                        item {
                            FilterChip(
                                selected = tempSortByPrice == SortOption.DESCENDING,
                                onClick = { tempSortByPrice = SortOption.DESCENDING },
                                label = { Text("Cao đến thấp", fontSize = 12.sp) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .shadow(
                                        elevation = if (tempSortByPrice == SortOption.DESCENDING) 4.dp else 0.dp,
                                        shape = RoundedCornerShape(12.dp)
                                    ),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = colorResource(R.color.purple),
                                    selectedLabelColor = Color.White,
                                    containerColor = Color(0xFFF5F5F5),
                                    labelColor = Color.Black
                                ),
                                shape = RoundedCornerShape(12.dp)
                            )
                        }
                        item {
                            FilterChip(
                                selected = tempSortByPrice == SortOption.NONE,
                                onClick = { tempSortByPrice = SortOption.NONE },
                                label = { Text("Không", fontSize = 12.sp) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .shadow(
                                        elevation = if (tempSortByPrice == SortOption.NONE) 4.dp else 0.dp,
                                        shape = RoundedCornerShape(12.dp)
                                    ),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = colorResource(R.color.purple),
                                    selectedLabelColor = Color.White,
                                    containerColor = Color(0xFFF5F5F5),
                                    labelColor = Color.Black
                                ),
                                shape = RoundedCornerShape(12.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "Sắp xếp theo đánh giá",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(3),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(70.dp)
                    ) {
                        item {
                            FilterChip(
                                selected = tempSortByRating == SortOption.ASCENDING,
                                onClick = { tempSortByRating = SortOption.ASCENDING },
                                label = { Text("Thấp đến cao", fontSize = 12.sp) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .shadow(
                                        elevation = if (tempSortByRating == SortOption.ASCENDING) 4.dp else 0.dp,
                                        shape = RoundedCornerShape(12.dp)
                                    ),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = colorResource(R.color.purple),
                                    selectedLabelColor = Color.White,
                                    containerColor = Color(0xFFF5F5F5),
                                    labelColor = Color.Black
                                ),
                                shape = RoundedCornerShape(12.dp)
                            )
                        }
                        item {
                            FilterChip(
                                selected = tempSortByRating == SortOption.DESCENDING,
                                onClick = { tempSortByRating = SortOption.DESCENDING },
                                label = { Text("Cao đến thấp", fontSize = 12.sp) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .shadow(
                                        elevation = if (tempSortByRating == SortOption.DESCENDING) 4.dp else 0.dp,
                                        shape = RoundedCornerShape(12.dp)
                                    ),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = colorResource(R.color.purple),
                                    selectedLabelColor = Color.White,
                                    containerColor = Color(0xFFF5F5F5),
                                    labelColor = Color.Black
                                ),
                                shape = RoundedCornerShape(12.dp)
                            )
                        }
                        item {
                            FilterChip(
                                selected = tempSortByRating == SortOption.NONE,
                                onClick = { tempSortByRating = SortOption.NONE },
                                label = { Text("Không", fontSize = 12.sp) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .shadow(
                                        elevation = if (tempSortByRating == SortOption.NONE) 4.dp else 0.dp,
                                        shape = RoundedCornerShape(12.dp)
                                    ),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = colorResource(R.color.purple),
                                    selectedLabelColor = Color.White,
                                    containerColor = Color(0xFFF5F5F5),
                                    labelColor = Color.Black
                                ),
                                shape = RoundedCornerShape(12.dp)
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        sortByPrice = tempSortByPrice
                        sortByRating = tempSortByRating
                        showFilterDialog = false
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(40.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colorResource(R.color.purple),
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        "Áp dụng",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showFilterDialog = false
                    // Khôi phục trạng thái tạm thời về giá trị ban đầu
                    tempSortByPrice = sortByPrice
                    tempSortByRating = sortByRating
                }) {
                    Text(
                        "Hủy",
                        color = Color.Gray,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Kết quả tìm kiếm: $query", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = { (context as? ComponentActivity)?.finish() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { showFilterDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.FilterList,
                            contentDescription = "Filter",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = colorResource(R.color.purple)
                )
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(padding)
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            } else if (filteredResults.isEmpty()) {
                Text(
                    text = "Không tìm thấy sản phẩm nào.",
                    fontSize = 16.sp,
                    color = Color.Gray,
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(start = 8.dp, end = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(filteredResults.size) { index ->
                        RecommendedItem(items = filteredResults, pos = index)
                    }
                }
            }
        }
    }
}

enum class SortOption {
    NONE, ASCENDING, DESCENDING
}