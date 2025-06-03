package com.example.techshop.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.techshop.Model.OrderItemModel

@Composable
fun OrderItem(item: OrderItemModel) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "${item.title} (x${item.numberInCart})",
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
        )
        Text(
            text = "${item.price * item.numberInCart} ₫",
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
        )
    }
}