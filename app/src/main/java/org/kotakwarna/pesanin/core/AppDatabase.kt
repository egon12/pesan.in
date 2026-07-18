package org.kotakwarna.pesanin.core

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import org.kotakwarna.pesanin.dao.OrderDao
import org.kotakwarna.pesanin.dao.OrderItemDao
import org.kotakwarna.pesanin.dao.ProductDao
import org.kotakwarna.pesanin.model.Order
import org.kotakwarna.pesanin.model.OrderItem
import org.kotakwarna.pesanin.model.Product

@Database(
    entities = [Order::class, OrderItem::class, Product::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun orderDao(): OrderDao
    abstract fun productDao(): ProductDao
    abstract fun orderItemDao(): OrderItemDao
}
