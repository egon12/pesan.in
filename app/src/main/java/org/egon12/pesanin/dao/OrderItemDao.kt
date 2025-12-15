package org.egon12.pesanin.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import org.egon12.pesanin.model.OrderItem

@Dao
interface OrderItemDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrderItem(orderItem: OrderItem)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllOrderItems(orderItems: List<OrderItem>)

    @Query("SELECT * FROM order_items WHERE orderId = :orderId")
    suspend fun getOrderItems(orderId: String): List<OrderItem>
}
