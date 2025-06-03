package com.example.techshop.Helper

import android.content.Context
import android.widget.Toast
import com.example.techshop.Model.ItemsModel

// Lớp ManagmentCart quản lý giỏ hàng cho ứng dụng TechShop
class ManagmentCart(val context: Context) {

    // Khai báo biến tinyDB để lưu trữ dữ liệu giỏ hàng bằng TinyDB
    private val tinyDB = TinyDB(context)

    // Hàm thêm sản phẩm vào giỏ hàng
    fun insertItem(item: ItemsModel) {
        val listFood = getListCart() // Lấy danh sách sản phẩm trong giỏ hàng
        val existAlready = listFood.any { it.title == item.title } // Kiểm tra sản phẩm đã tồn tại chưa
        val index = listFood.indexOfFirst { it.title == item.title } // Lấy vị trí của sản phẩm nếu đã tồn tại

        if (existAlready) {
            // Nếu sản phẩm đã tồn tại, cập nhật số lượng và timestamp
            listFood[index].numberInCart = item.numberInCart
            listFood[index].timestamp = System.currentTimeMillis()
        } else {
            // Nếu chưa tồn tại, thêm sản phẩm mới vào danh sách
            item.timestamp = System.currentTimeMillis() // Cập nhật timestamp
            listFood.add(item)
        }
        tinyDB.putListObject("CartList", listFood) // Lưu danh sách giỏ hàng vào bộ nhớ
        Toast.makeText(context, "Added to your Cart", Toast.LENGTH_SHORT).show() // Hiển thị thông báo
    }

    // Hàm lấy danh sách sản phẩm trong giỏ hàng
    fun getListCart(): ArrayList<ItemsModel> {
        return tinyDB.getListObject("CartList") ?: arrayListOf() // Nếu giỏ hàng rỗng, trả về danh sách trống
    }

    // Hàm giảm số lượng sản phẩm trong giỏ hàng
    fun minusItem(listFood: ArrayList<ItemsModel>, position: Int, listener: ChangeNumberItemsListener) {
        if (listFood[position].numberInCart == 1) {
            // Nếu số lượng sản phẩm bằng 1, xóa sản phẩm khỏi giỏ hàng
            listFood.removeAt(position)
        } else {
            // Giảm số lượng sản phẩm
            listFood[position].numberInCart--
            listFood[position].timestamp = System.currentTimeMillis() // Cập nhật timestamp
        }
        tinyDB.putListObject("CartList", listFood) // Cập nhật lại giỏ hàng
        listener.onChanged() // Gọi callback để cập nhật UI
    }

    // Hàm tăng số lượng sản phẩm trong giỏ hàng
    fun plusItem(listFood: ArrayList<ItemsModel>, position: Int, listener: ChangeNumberItemsListener) {
        listFood[position].numberInCart++ // Tăng số lượng sản phẩm
        listFood[position].timestamp = System.currentTimeMillis() // Cập nhật timestamp
        tinyDB.putListObject("CartList", listFood) // Cập nhật lại giỏ hàng
        listener.onChanged() // Gọi callback để cập nhật UI
    }

    // Hàm tính tổng giá tiền của giỏ hàng
    fun getTotalFee(): Double {
        val listFood = getListCart() // Lấy danh sách sản phẩm trong giỏ hàng
        var fee = 0.0
        for (item in listFood) {
            // Tính tổng tiền: giá sản phẩm * số lượng
            fee += item.price * item.numberInCart
        }
        return fee // Trả về tổng giá trị đơn hàng
    }

    // Hàm xóa toàn bộ giỏ hàng
    fun clearCart() {
        tinyDB.remove("CartList") // Xóa key CartList trong TinyDB
    }
}