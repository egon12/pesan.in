package org.egon12.pesanin.core

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import org.egon12.pesanin.dao.OrderDao
import org.egon12.pesanin.dao.OrderItemDao
import org.egon12.pesanin.dao.ProductDao
import org.egon12.pesanin.model.Order
import org.egon12.pesanin.model.OrderItem
import org.egon12.pesanin.model.Product

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
