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
import com.example.techshop.Model.CategoryModel
import com.example.techshop.Model.ItemsModel
import com.example.techshop.R
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

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
    val context = LocalContext.current
    var title by remember { mutableStateOf(product?.title ?: "") }
    var description by remember { mutableStateOf(product?.description ?: "") }
    var price by remember { mutableStateOf(product?.price?.toString() ?: "") }
    var categoryId by remember { mutableStateOf(product?.categoryId ?: "") }
    var rating by remember { mutableStateOf(product?.rating?.toString() ?: "") }
    var stock by remember { mutableStateOf(product?.stock?.toString() ?: "0") }
    var showRecommended by remember { mutableStateOf(product?.showRecommended ?: false) }
    var imageUrl by remember { mutableStateOf(product?.picUrl?.firstOrNull() ?: "") }

    // Load categories
    var categories by remember { mutableStateOf(listOf<CategoryModel>()) }
    var expanded by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        val database = FirebaseDatabase.getInstance().getReference("Category")
        database.addListenerForSingleValueEvent(object : ValueEventListener {
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
    }

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

            // Text field cho category ID
            OutlinedTextField(
                value = categoryId,
                onValueChange = { categoryId = it },
                label = { Text("ID danh mục (bắt buộc)") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = rating,
                onValueChange = { rating = it },
                label = { Text("Đánh giá") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = stock,
                onValueChange = { stock = it },
                label = { Text("Số lượng tồn kho") },
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
                    // Validation
                    if (title.isEmpty()) {
                        Toast.makeText(context, "Vui lòng nhập tên sản phẩm", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    if (price.isEmpty() || price.toDoubleOrNull() == null) {
                        Toast.makeText(context, "Vui lòng nhập giá hợp lệ", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    if (categoryId.isEmpty()) {
                        Toast.makeText(context, "Vui lòng nhập ID danh mục", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    if (stock.isEmpty() || stock.toIntOrNull() == null) {
                        Toast.makeText(context, "Vui lòng nhập số lượng tồn kho", Toast.LENGTH_SHORT).show()
                        return@Button
                    }

                    // Kiểm tra category có tồn tại không
                    val categoryExists = categories.any { it.id.toString() == categoryId }
                    if (!categoryExists) {
                        Toast.makeText(context, "ID danh mục không tồn tại! Vui lòng kiểm tra lại", Toast.LENGTH_LONG).show()
                        return@Button
                    }

                    val newProduct = ItemsModel(
                        id = product?.id ?: "",
                        title = title,
                        description = description,
                        price = price.toDoubleOrNull() ?: 0.0,
                        categoryId = categoryId,
                        rating = rating.toDoubleOrNull() ?: 0.0,
                        stock = stock.toIntOrNull() ?: 0,
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