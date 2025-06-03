package com.example.techshop.Activity

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme.colors
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.rememberAsyncImagePainter
import com.example.techshop.Helper.FavoriteManager
import com.example.techshop.Helper.ManagmentCart
import com.example.techshop.Model.ItemsModel
import com.example.techshop.R
import com.example.techshop.utils.toVND
import com.google.firebase.auth.FirebaseAuth
import java.text.NumberFormat
import java.util.Locale

class FavoriteActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            FavoriteScreen()
        }
    }

    override fun onBackPressed() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        startActivity(intent)
        super.onBackPressed()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoriteScreen() {
    val favoriteItems by FavoriteManager.favoriteItems.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val managmentCart = remember { ManagmentCart(context) }
    val isLoggedIn = FirebaseAuth.getInstance().currentUser != null

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Sản phẩm yêu thích", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = {
                        val intent = Intent(context, MainActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                        context.startActivity(intent)
                        (context as? ComponentActivity)?.finish()
                    }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
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
            when {
                !isLoggedIn -> {
                    Text(
                        text = "Vui lòng đăng nhập để xem sản phẩm yêu thích.",
                        fontSize = 16.sp,
                        color = Color.Gray,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                favoriteItems.isEmpty() -> {
                    Text(
                        text = "Bạn chưa có sản phẩm yêu thích nào.",
                        fontSize = 16.sp,
                        color = Color.Gray,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                else -> {
                    ListItems(
                        items = favoriteItems,
                        managmentCart = managmentCart
                    )
                }
            }
        }
    }
}

@Composable
fun ListItems(
    items: List<ItemsModel>,
    managmentCart: ManagmentCart? = null
) {
    val context = LocalContext.current
    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(
            items = items,
            key = { item -> item.id } // Sử dụng id làm key để đảm bảo trạng thái không bị tái sử dụng sai
        ) { item ->
            ItemRow(
                item = item,
                onItemClick = {
                    val intent = Intent(context, DetailActivity::class.java).apply {
                        putExtra("object", item)
                    }
                    context.startActivity(intent)
                },
                onAddToCartClick = {
                    item.numberInCart = 1
                    managmentCart?.insertItem(item)
                },
                onFavoriteClick = {
                    if (FavoriteManager.isFavorite(item)) {
                        FavoriteManager.removeFavorite(item)
                    } else {
                        FavoriteManager.addFavorite(item)
                    }
                }
            )
        }
    }
}

@Composable
fun ItemRow(
    item: ItemsModel,
    onItemClick: () -> Unit,
    onAddToCartClick: () -> Unit,
    onFavoriteClick: () -> Unit
) {
    val isFavorite by FavoriteManager.favoriteItems.collectAsStateWithLifecycle()
    val isItemFavorite by remember(item.id) { // Sử dụng item.id làm key cho remember
        derivedStateOf { isFavorite.any { it.id == item.id } }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(130.dp) // Giữ chiều cao
            .clickable(onClick = onItemClick),
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFDEDFE3))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = rememberAsyncImagePainter(model = item.picUrl.firstOrNull()),
                    contentDescription = item.title,
                    modifier = Modifier
                        .size(120.dp) // Tăng kích thước hình ảnh từ 80.dp lên 100.dp
                        .background(
                            colorResource(R.color.lightGrey),
                            shape = RoundedCornerShape(8.dp)
                        )
                        .padding(8.dp),
                    contentScale = ContentScale.Inside
                )

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 16.dp)
                ) {
                    Text(
                        text = item.title,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.star),
                            contentDescription = "Rating",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = item.rating.toString(),
                            color = Color.Black,
                            fontSize = 15.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "${NumberFormat.getNumberInstance(Locale("vi", "VN")).format(item.price)} ₫",
                        fontSize = 14.sp,
                        color = Color(0xFFC72216),
                        fontWeight = FontWeight.Bold
                    )
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    IconButton(onClick = onFavoriteClick) {
                        Icon(
                            painter = painterResource(R.drawable.ic_fav),
                            contentDescription = "Favorite",
                            tint = if (isItemFavorite) Color.Red else Color.Gray
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    IconButton(onClick = onAddToCartClick) {
                        Icon(
                            painter = painterResource(R.drawable.btn_2),
                            contentDescription = "Add to Cart",
                            tint = Color.Black
                        )
                    }
                }
            }
        }
    }
}