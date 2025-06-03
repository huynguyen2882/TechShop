package com.example.techshop.Model

import android.os.Parcel
import android.os.Parcelable

data class ItemsModel(
    var id: String = "", // Thêm thuộc tính id
    var title: String = "",
    var description: String = "",
    var picUrl: ArrayList<String> = ArrayList(),
    var model: ArrayList<String> = ArrayList(),
    var price: Double = 0.0,
    var rating: Double = 0.0,
    var numberInCart: Int = 0,
    var showRecommended: Boolean = false,
    var categoryId: String = "",
    var timestamp: Long = System.currentTimeMillis()
) : Parcelable {
    constructor(parcel: Parcel) : this(
        id = parcel.readString() ?: "", // Đọc id từ Parcel
        title = parcel.readString() ?: "",
        description = parcel.readString() ?: "",
        picUrl = (parcel.createStringArrayList() as ArrayList<String>?) ?: ArrayList(),
        model = (parcel.createStringArrayList() as ArrayList<String>?) ?: ArrayList(),
        price = parcel.readDouble(),
        rating = parcel.readDouble(),
        numberInCart = parcel.readInt(),
        showRecommended = parcel.readByte() != 0.toByte(),
        categoryId = parcel.readString() ?: "",
        timestamp = parcel.readLong()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id) // Ghi id vào Parcel
        parcel.writeString(title)
        parcel.writeString(description)
        parcel.writeStringList(picUrl)
        parcel.writeStringList(model)
        parcel.writeDouble(price)
        parcel.writeDouble(rating)
        parcel.writeInt(numberInCart)
        parcel.writeByte(if (showRecommended) 1 else 0)
        parcel.writeString(categoryId)
        parcel.writeLong(timestamp)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<ItemsModel> {
        override fun createFromParcel(parcel: Parcel): ItemsModel {
            return ItemsModel(parcel)
        }

        override fun newArray(size: Int): Array<ItemsModel?> {
            return arrayOfNulls(size)
        }
    }
}