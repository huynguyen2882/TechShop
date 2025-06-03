package com.example.techshop.Activity

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.techshop.R
import com.example.techshop.ViewModel.OrdersViewModel
import com.example.techshop.ui.components.OrderItem
import com.example.techshop.utils.toVND

class OrdersActivity : ComponentActivity() {

    private val viewModel: OrdersViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.loadOrders()
        setContent {
            OrdersScreen(viewModel = viewModel)
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun OrdersScreen(viewModel: OrdersViewModel) {
        val orders by viewModel.orders.collectAsState()

        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = "Đơn hàng của tôi",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { finish() }) {
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
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                if (orders.isEmpty()) {
                    Text(
                        text = "Bạn chưa có đơn hàng nào.",
                        fontSize = 16.sp,
                        color = Color.Gray,
                        modifier = Modifier.align(Alignment.Center)
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(orders.size) { index ->
                            OrderCard(order = orders[index])
                        }
                    }
                }
            }
        }
    }

    @Composable
    fun OrderCard(order: com.example.techshop.Model.OrderModel) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp),
            colors = CardDefaults.cardColors(
                containerColor = colorResource(R.color.lightGrey)
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Đơn hàng: ${order.orderId}",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(text = "Trạng thái: ${order.status}")
                Text(text = "Người nhận: ${order.name}")
                Text(text = "Địa chỉ: ${order.address}")
                Text(text = "Số điện thoại: ${order.phone}")
                Text(text = "Phương thức thanh toán: ${order.paymentMethod}")
                Text(text = "Thời gian: ${java.text.SimpleDateFormat("dd/MM/yyyy HH:mm").format(order.timestamp)}")

                order.items.forEach { item ->
                    OrderItem(item = item)
                }

                Text(
                    text = "Tổng tiền: ${order.total.toVND()}",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = colorResource(R.color.purple)
                )
            }
        }
    }
}