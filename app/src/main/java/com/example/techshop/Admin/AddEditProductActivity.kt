package com.example.techshop.Admin

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.techshop.Model.ItemsModel
import com.example.techshop.R
import com.google.firebase.database.FirebaseDatabase

class AddEditProductActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val product = intent.getParcelableExtra<ItemsModel>("PRODUCT")
        setContent {
            AddEditProductScreen(
                product = product,
                onBackClick = { finish() },
                onSave = { updatedProduct ->
                    saveProduct(updatedProduct)
                    finish()
                }
            )
        }
    }

    private fun saveProduct(product: ItemsModel) {
        val database = FirebaseDatabase.getInstance().getReference("Items")
        val productId = if (product.id.isEmpty()) database.push().key ?: "" else product.id
        database.child(productId).setValue(product.copy(id = productId))
            .addOnSuccessListener {
                Toast.makeText(this, "Lưu sản phẩm thành công", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Lưu sản phẩm thất bại: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditProductScreen(
    product: ItemsModel?,
    onBackClick: () -> Unit,
    onSave: (ItemsModel) -> Unit
) {
    var title by remember { mutableStateOf(product?.title ?: "") }
    var description by remember { mutableStateOf(product?.description ?: "") }
    var price by remember { mutableStateOf(product?.price?.toString() ?: "") }
    var categoryId by remember { mutableStateOf(product?.categoryId ?: "") }
    var rating by remember { mutableStateOf(product?.rating?.toString() ?: "") }
    var showRecommended by remember { mutableStateOf(product?.showRecommended ?: false) }
    var imageUrl by remember { mutableStateOf(product?.picUrl?.firstOrNull() ?: "") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (product == null) "Thêm sản phẩm" else "Sửa sản phẩm",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
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
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = colorResource(R.color.purple),
                    titleContentColor = Color.White
                )
            )
        },
        containerColor = Color.White
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Tiêu đề") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Mô tả") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = imageUrl,
                onValueChange = { imageUrl = it },
                label = { Text("URL hình ảnh") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = price,
                onValueChange = { price = it },
                label = { Text("Giá") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = categoryId,
                onValueChange = { categoryId = it },
                label = { Text("ID danh mục") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = rating,
                onValueChange = { rating = it },
                label = { Text("Đánh giá") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = showRecommended,
                    onCheckedChange = { showRecommended = it }
                )
                Text("Hiển thị trong đề xuất")
            }
            Button(
                onClick = {
                    val newProduct = ItemsModel(
                        id = product?.id ?: "",
                        title = title,
                        description = description,
                        price = price.toDoubleOrNull() ?: 0.0,
                        categoryId = categoryId,
                        rating = rating.toDoubleOrNull() ?: 0.0,
                        showRecommended = showRecommended,
                        picUrl = arrayListOf(imageUrl),
                        model = product?.model ?: arrayListOf()
                    )
                    onSave(newProduct)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = colorResource(R.color.purple),
                    contentColor = Color.White
                )
            ) {
                Text(
                    text = "Lưu",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}