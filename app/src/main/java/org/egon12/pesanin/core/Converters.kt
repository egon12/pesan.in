package org.egon12.pesanin.core

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.egon12.pesanin.model.OrderItem

class Converters {
    private val gson = Gson()

    @TypeConverter
    fun fromOrderItemList(items: List<OrderItem>): String {
        return gson.toJson(items)
    }

    @TypeConverter
    fun toOrderItemList(itemsString: String): List<OrderItem> {
        val listType = object : TypeToken<List<OrderItem>>() {}.type
        return gson.fromJson(itemsString, listType)
    }
}
