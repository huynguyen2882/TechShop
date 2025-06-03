package com.example.techshop.Model

data class OrderModel(
    val orderId: String = "", // Thêm orderId
    val name: String = "",
    val address: String = "",
    val phone: String = "",
    val paymentMethod: String = "",
    val total: Double = 0.0,
    val items: List<OrderItemModel> = emptyList(),
    val timestamp: Long = 0L, // Thêm timestamp
    val status: String = "Pending", // Thêm trạng thái
    val userId: String = ""
)