package com.example.techshop.Helper

import com.example.techshop.Model.ItemsModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

object FavoriteManager {
    private val _favoriteItems = MutableStateFlow<List<ItemsModel>>(emptyList())
    val favoriteItems: StateFlow<List<ItemsModel>> get() = _favoriteItems

    private val database = FirebaseDatabase.getInstance().reference
    private val auth = FirebaseAuth.getInstance()

    init {
        // Lắng nghe thay đổi từ Firebase khi người dùng đăng nhập
        auth.addAuthStateListener { firebaseAuth ->
            val user = firebaseAuth.currentUser
            if (user != null) {
                loadFavoritesFromFirebase(user.uid)
            } else {
                _favoriteItems.value = emptyList() // Xóa danh sách nếu người dùng đăng xuất
            }
        }

        // Tải dữ liệu ngay lập tức nếu người dùng đã đăng nhập
        val currentUser = auth.currentUser
        if (currentUser != null) {
            loadFavoritesFromFirebase(currentUser.uid)
        }
    }

    private fun loadFavoritesFromFirebase(userId: String) {
        val favoritesRef = database.child("favorites").child(userId)
        favoritesRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val favorites = mutableListOf<ItemsModel>()
                for (itemSnapshot in snapshot.children) {
                    val item = itemSnapshot.getValue(ItemsModel::class.java)
                    if (item != null) {
                        favorites.add(item)
                    }
                }
                _favoriteItems.value = favorites
            }

            override fun onCancelled(error: DatabaseError) {
                // Xử lý lỗi nếu cần
                _favoriteItems.value = emptyList()
            }
        })
    }

    fun addFavorite(item: ItemsModel) {
        val user = auth.currentUser
        if (user != null && item.id.isNotEmpty()) {
            val userId = user.uid
            val favoritesRef = database.child("favorites").child(userId)
            favoritesRef.child(item.id).setValue(item)
        }
    }

    fun removeFavorite(item: ItemsModel) {
        val user = auth.currentUser
        if (user != null && item.id.isNotEmpty()) {
            val userId = user.uid
            val favoritesRef = database.child("favorites").child(userId)
            favoritesRef.child(item.id).removeValue()
        }
    }

    fun isFavorite(item: ItemsModel): Boolean {
        return _favoriteItems.value.any { it.id == item.id }
    }
}