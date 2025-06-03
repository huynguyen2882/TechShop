package com.example.techshop.ViewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.techshop.Model.OrderItemModel
import com.example.techshop.Model.OrderModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class OrdersViewModel : ViewModel() {

    private val databaseRef = FirebaseDatabase.getInstance().reference
    private val auth = FirebaseAuth.getInstance()
    private val _orders = MutableStateFlow<List<OrderModel>>(emptyList())
    val orders: StateFlow<List<OrderModel>> get() = _orders

    fun loadOrders() {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            _orders.value = emptyList() // Không có người dùng đăng nhập
            return
        }

        val userId = currentUser.uid
        val ordersRef = databaseRef.child("orders").orderByChild("userId").equalTo(userId)

        viewModelScope.launch(Dispatchers.IO) {
            ordersRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val orderList = mutableListOf<OrderModel>()

                    for (orderSnapshot in snapshot.children) {
                        val orderId = orderSnapshot.key ?: ""
                        val name = orderSnapshot.child("name").getValue(String::class.java) ?: ""
                        val address = orderSnapshot.child("address").getValue(String::class.java) ?: ""
                        val phone = orderSnapshot.child("phone").getValue(String::class.java) ?: ""
                        val paymentMethod = orderSnapshot.child("paymentMethod").getValue(String::class.java) ?: ""
                        val total = orderSnapshot.child("total").getValue(Double::class.java) ?: 0.0
                        val timestamp = orderSnapshot.child("timestamp").getValue(Long::class.java) ?: 0L
                        val status = orderSnapshot.child("status").getValue(String::class.java) ?: "Pending"
                        val userId = orderSnapshot.child("userId").getValue(String::class.java) ?: ""

                        val items = mutableListOf<OrderItemModel>()
                        val itemsSnapshot = orderSnapshot.child("items")
                        for (itemSnapshot in itemsSnapshot.children) {
                            val item = itemSnapshot.getValue(OrderItemModel::class.java)
                            item?.let { items.add(it) }
                        }

                        val order = OrderModel(
                            orderId = orderId,
                            name = name,
                            address = address,
                            phone = phone,
                            paymentMethod = paymentMethod,
                            total = total,
                            items = items,
                            timestamp = timestamp,
                            status = status,
                            userId = userId
                        )
                        orderList.add(order)
                    }

                    _orders.value = orderList.sortedByDescending { it.timestamp }
                }

                override fun onCancelled(error: DatabaseError) {
                    _orders.value = emptyList()
                }
            })
        }
    }
}