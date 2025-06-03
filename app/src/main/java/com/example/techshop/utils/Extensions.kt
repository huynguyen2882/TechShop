package com.example.techshop.utils

import java.text.NumberFormat
import java.util.*

fun Double.toVND(): String {
    return NumberFormat.getNumberInstance(Locale("vi", "VN")).format(this) + " ₫"
}