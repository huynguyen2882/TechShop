package com.example.techshop.ViewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.techshop.Model.CategoryModel
import com.example.techshop.Model.ItemsModel
import com.example.techshop.Model.SliderModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.Query
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainViewModel : ViewModel() {

    private val firebaseDatabase = FirebaseDatabase.getInstance()

    private val _category = MutableLiveData<List<CategoryModel>>()
    private val _banner = MutableLiveData<List<SliderModel>>()
    private val _recommended = MutableLiveData<List<ItemsModel>>()

    val banners: LiveData<List<SliderModel>> get() = _banner
    val categories: LiveData<List<CategoryModel>> get() = _category
    val recommended: LiveData<List<ItemsModel>> get() = _recommended

    fun loadFiltered(id: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val ref = firebaseDatabase.getReference("Items")
            val query: Query = ref.orderByChild("categoryId").equalTo(id).limitToFirst(20)
            query.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val lists = mutableListOf<ItemsModel>()
                    for (childSnapshot in snapshot.children) {
                        val list = childSnapshot.getValue(ItemsModel::class.java)
                        if (list != null) {
                            // Gán id từ key của node
                            val itemWithId = list.copy(id = childSnapshot.key ?: "")
                            lists.add(itemWithId)
                        }
                    }
                    _recommended.postValue(lists)
                }

                override fun onCancelled(error: DatabaseError) {
                    _recommended.postValue(emptyList())
                }
            })
        }
    }

    fun loadRecommended() {
        viewModelScope.launch(Dispatchers.IO) {
            val ref = firebaseDatabase.getReference("Items")
            val query: Query = ref.orderByChild("showRecommended").equalTo(true).limitToFirst(20)
            query.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val lists = mutableListOf<ItemsModel>()
                    for (childSnapshot in snapshot.children) {
                        val list = childSnapshot.getValue(ItemsModel::class.java)
                        if (list != null) {
                            // Gán id từ key của node
                            val itemWithId = list.copy(id = childSnapshot.key ?: "")
                            lists.add(itemWithId)
                        }
                    }
                    _recommended.postValue(lists)
                }

                override fun onCancelled(error: DatabaseError) {
                    _recommended.postValue(emptyList())
                }
            })
        }
    }

    fun loadBanners() {
        viewModelScope.launch(Dispatchers.IO) {
            val ref = firebaseDatabase.getReference("Banner")
            ref.limitToFirst(5).addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val lists = mutableListOf<SliderModel>()
                    for (childSnapshot in snapshot.children) {
                        val list = childSnapshot.getValue(SliderModel::class.java)
                        if (list != null) {
                            lists.add(list)
                        }
                    }
                    _banner.postValue(lists)
                }

                override fun onCancelled(error: DatabaseError) {
                    _banner.postValue(emptyList())
                }
            })
        }
    }

    fun loadCategory() {
        viewModelScope.launch(Dispatchers.IO) {
            val ref = firebaseDatabase.getReference("Category")
            ref.limitToFirst(10).addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val lists = mutableListOf<CategoryModel>()
                    for (childSnapshot in snapshot.children) {
                        val list = childSnapshot.getValue(CategoryModel::class.java)
                        if (list != null) {
                            lists.add(list)
                        }
                    }
                    _category.postValue(lists)
                }

                override fun onCancelled(error: DatabaseError) {
                    _category.postValue(emptyList())
                }
            })
        }
    }
}