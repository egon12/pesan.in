package org.egon12.pesanin.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import org.egon12.pesanin.dao.OrderDao
import org.egon12.pesanin.dao.OrderItemDao
import org.egon12.pesanin.dao.ProductDao
import org.egon12.pesanin.model.Invoice
import org.egon12.pesanin.model.Order
import org.egon12.pesanin.model.OrderItem
import org.egon12.pesanin.model.Product
import javax.inject.Inject

class OrderRepository @Inject constructor(
    private val orderDao: OrderDao,
    private val productDao: ProductDao,
    private val orderItemDao: OrderItemDao
) {
    suspend fun createOrder(order: Order, items: List<OrderItem>) {
        orderDao.insertOrder(order)
        orderItemDao.insertAllOrderItems(items)
    }

    fun getAllOrders(): Flow<List<Order>> = orderDao.getAllOrders()

    suspend fun getOrderWithItems(orderId: String): Order? {
        val order = orderDao.getOrderById(orderId)
        val items = orderItemDao.getOrderItems(orderId)
        return order.firstOrNull()?.copy(items = items)
    }

    fun getAllProducts(): Flow<List<Product>> = productDao.getAllProducts()

    suspend fun addProduct(product: Product) = productDao.insertProduct(product)

    suspend fun generateInvoice(order: Order): Invoice {
        val subtotal = order.items.sumOf { it.total }
        val tax = subtotal * 0.10 // 10% tax
        val total = subtotal + tax

        return Invoice(
            orderId = order.id,
            customerName = order.customerName,
            whatsappNumber = order.whatsappNumber,
            items = order.items,
            subtotal = subtotal,
            tax = tax,
            totalAmount = total,
            invoiceNumber = "INV-${System.currentTimeMillis()}"
        )
    }
}