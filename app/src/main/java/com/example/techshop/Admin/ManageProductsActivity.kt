package com.example.techshop.Admin

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.techshop.Model.CategoryModel
import com.example.techshop.Model.ItemsModel
import com.example.techshop.R
import com.example.techshop.ui.components.ListItems // note
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import coil.compose.AsyncImage
import coil.request.ImageRequest
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.foundation.background

class ManageProductsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ManageProductsScreen(
                onBackClick = { finish() },
                onAddProduct = {
                    startActivity(Intent(this, AddEditProductActivity::class.java))
                },
                onEditProduct = { product ->
                    val intent = Intent(this, AddEditProductActivity::class.java)
                    intent.putExtra("PRODUCT", product)
                    startActivity(intent)
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageProductsScreen(
    onBackClick: () -> Unit,
    onAddProduct: () -> Unit,
    onEditProduct: (ItemsModel) -> Unit
) {
    val database = FirebaseDatabase.getInstance().getReference("Items")
    var products by remember { mutableStateOf(listOf<ItemsModel>()) }
    var isLoading by remember { mutableStateOf(true) }
    var categories by remember { mutableStateOf(listOf<CategoryModel>()) }

    LaunchedEffect(Unit) {
        // Load categories
        val categoryRef = FirebaseDatabase.getInstance().getReference("Category")
        categoryRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val categoryList = mutableListOf<CategoryModel>()
                for (childSnapshot in snapshot.children) {
                    val category = childSnapshot.getValue(CategoryModel::class.java)
                    if (category != null) {
                        categoryList.add(category.copy(id = childSnapshot.key?.toIntOrNull() ?: 0))
                    }
                }
                categories = categoryList
            }
            override fun onCancelled(error: DatabaseError) {}
        })

        // Load products
        database.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val productList = mutableListOf<ItemsModel>()
                for (childSnapshot in snapshot.children) {
                    val product = childSnapshot.getValue(ItemsModel::class.java)
                    if (product != null) {
                        productList.add(product.copy(id = childSnapshot.key ?: ""))
                    }
                }
                products = productList
                isLoading = false
            }

            override fun onCancelled(error: DatabaseError) {
                isLoading = false
            }
        })
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Quản lý sản phẩm",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = "Quay lại",
                            tint = Color.White
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onAddProduct) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Thêm sản phẩm",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = colorResource(R.color.purple),
                    titleContentColor = Color.White
                )
            )
        },
        containerColor = Color.White
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (products.isEmpty()) {
                Text(
                    text = "Không có sản phẩm nào.",
                    fontSize = 16.sp,
                    color = Color.Gray,
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(products.size) { index ->
                        AdminProductItem(
                            product = products[index],
                            categories = categories,
                            onClick = onEditProduct
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AdminProductItem(
    product: ItemsModel,
    categories: List<CategoryModel>,
    onClick: (ItemsModel) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick(product) },
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(8.dp)
        ) {
            // Hình ảnh sản phẩm
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .background(Color.LightGray, RoundedCornerShape(8.dp))
            ) {
                if (product.picUrl.isNotEmpty()) {
                    AsyncImage(
                        model = product.picUrl[0],
                        contentDescription = product.title,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Tên sản phẩm
            Text(
                text = product.title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Giá
            Text(
                text = "${product.price} ₫",
                fontSize = 12.sp,
                color = colorResource(R.color.purple),
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Stock
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Stock: ",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
                Text(
                    text = "${product.stock}",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (product.stock > 0) Color.Green else Color.Red
                )
            }

            // Rating
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Rating: ",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
                Text(
                    text = "${product.rating}",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // Category
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Category: ",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
                Text(
                    text = categories.find { it.id.toString() == product.categoryId }?.title ?: "Unknown",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = colorResource(R.color.purple)
                )
            }
        }
    }
}