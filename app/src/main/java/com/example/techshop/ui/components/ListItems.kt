package com.example.techshop.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.techshop.Helper.FavoriteManager
import com.example.techshop.Model.ItemsModel
import com.example.techshop.R

@Composable
fun ListItems(
    modifier: Modifier = Modifier,
    itemsList: List<ItemsModel>,
    onClick: (ItemsModel) -> Unit = {}
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = modifier,
        contentPadding = PaddingValues(bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(itemsList.size) { index ->
            val item = itemsList[index]
            RecommendedItem(item = item, onClick = onClick)
        }
    }
}

@Composable
fun RecommendedItem(item: ItemsModel, onClick: (ItemsModel) -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick(item) },
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .clip(RoundedCornerShape(12.dp))
            ) {
                AsyncImage(
                    model = if (item.picUrl.isNotEmpty()) item.picUrl[0] else "",
                    contentDescription = item.title,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )

//                Box(
//                    modifier = Modifier
//                        .align(Alignment.TopEnd)
//                        .padding(8.dp)
//                        .size(30.dp)
//                        .clip(CircleShape)
//                        .background(Color.White),
//                    contentAlignment = Alignment.Center
//                ) {
//                    val isItemFavorite = FavoriteManager.isFavorite(item)
//                    IconButton(
//                        onClick = {
//                            if (isItemFavorite) {
//                                FavoriteManager.removeFavorite(item)
//                            } else {
//                                FavoriteManager.addFavorite(item)
//                            }
//                        }
//                    ) {
//                        Icon(
//                            painter = painterResource(R.drawable.ic_fav),
//                            contentDescription = "Favorite",
//                            tint = if (isItemFavorite) Color.Red else Color.Gray,
//                            modifier = Modifier.size(20.dp)
//                        )
//                    }
//                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = item.title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "${item.price} ₫",
                fontSize = 14.sp,
                color = colorResource(id = R.color.purple),
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.star),
                    contentDescription = "Rating",
                    tint = Color(0xFFFFD700),
                    modifier = Modifier.size(16.dp)
                )

                Spacer(modifier = Modifier.width(4.dp))

                Text(
                    text = item.rating.toString(),
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }
        }
    }
}