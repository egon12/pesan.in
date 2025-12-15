package org.egon12.pesanin.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date
import java.util.UUID

@Entity(tableName = "orders")
data class Order(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val whatsappNumber: String = "",
    val customerName: String = "",
    val orderDate: Long = Date().time,
    val status: OrderStatus = OrderStatus.PENDING,
    val totalAmount: Double = 0.0,
    val items: List<OrderItem> = emptyList()
)

enum class OrderStatus {
    PENDING, CONFIRMED, PROCESSING, COMPLETED, CANCELLED
}

@Entity(tableName = "order_items")
data class OrderItem(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val orderId: String,
    val productId: String,
    val productName: String,
    val quantity: Int,
    val price: Double,
    val total: Double
)
