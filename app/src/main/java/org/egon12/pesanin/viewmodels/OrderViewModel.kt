package org.egon12.pesanin.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.egon12.pesanin.model.Invoice
import org.egon12.pesanin.model.Order
import org.egon12.pesanin.model.OrderItem
import org.egon12.pesanin.model.Product
import org.egon12.pesanin.repository.OrderRepository
import java.util.Date
import javax.inject.Inject

@HiltViewModel
class OrderViewModel @Inject constructor(
    private val repository: OrderRepository
) : ViewModel() {

    private val _selectedProducts = MutableStateFlow<List<OrderItem>>(emptyList())
    val selectedProducts: StateFlow<List<OrderItem>> = _selectedProducts.asStateFlow()

    private val _whatsappNumber = MutableStateFlow("")
    val whatsappNumber: StateFlow<String> = _whatsappNumber.asStateFlow()

    private val _customerName = MutableStateFlow("")
    val customerName: StateFlow<String> = _customerName.asStateFlow()

    val allProducts: Flow<List<Product>> = repository.getAllProducts()
    val allOrders: Flow<List<Order>> = repository.getAllOrders()

    fun setWhatsappNumber(number: String) {
        _whatsappNumber.value = number
    }

    fun setCustomerName(name: String) {
        _customerName.value = name
    }

    fun addProductToOrder(product: Product, quantity: Int = 1) {
        viewModelScope.launch {
            val existingItem = _selectedProducts.value.find { it.productId == product.id }
            val updatedList = if (existingItem != null) {
                val updatedItem = existingItem.copy(
                    quantity = existingItem.quantity + quantity,
                    total = (existingItem.quantity + quantity) * product.price
                )
                _selectedProducts.value.map { if (it.productId == product.id) updatedItem else it }
            } else {
                _selectedProducts.value + OrderItem(
                    orderId = "",
                    productId = product.id,
                    productName = product.name,
                    quantity = quantity,
                    price = product.price,
                    total = product.price * quantity
                )
            }
            _selectedProducts.value = updatedList
        }
    }

    fun removeProductFromOrder(productId: String) {
        _selectedProducts.value = _selectedProducts.value.filter { it.productId != productId }
    }

    fun updateProductQuantity(productId: String, quantity: Int) {
        viewModelScope.launch {
            _selectedProducts.value = _selectedProducts.value.map { item ->
                if (item.productId == productId) {
                    item.copy(quantity = quantity, total = item.price * quantity)
                } else {
                    item
                }
            }
        }
    }

    suspend fun createOrder(): Result<String> {
        return try {
            if (_whatsappNumber.value.isEmpty()) {
                return Result.failure(IllegalArgumentException("WhatsApp number is required"))
            }

            if (_selectedProducts.value.isEmpty()) {
                return Result.failure(IllegalArgumentException("Select at least one product"))
            }

            val order = Order(
                whatsappNumber = _whatsappNumber.value,
                customerName = _customerName.value,
                items = _selectedProducts.value,
                totalAmount = _selectedProducts.value.sumOf { it.total }
            )

            repository.createOrder(order, _selectedProducts.value)
            Result.success(order.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun sendInvoice(): Result<String> {
        return try {
            val orderResult = createOrder()
            if (orderResult.isFailure) {
                return Result.failure(orderResult.exceptionOrNull()!!)
            }

            val orderId = orderResult.getOrNull() ?: return Result.failure(
                IllegalStateException("Order creation failed")
            )

            val order = repository.getOrderWithItems(orderId)
            if (order == null) {
                return Result.failure(IllegalStateException("Order not found"))
            }

            val invoice = repository.generateInvoice(order)
            val message = formatInvoiceMessage(invoice)

            // Reset the form
            _selectedProducts.value = emptyList()
            _whatsappNumber.value = ""
            _customerName.value = ""

            Result.success(message)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun formatInvoiceMessage(invoice: Invoice): String {
        val itemsText = invoice.items.joinToString("\n") { item ->
            "${item.productName} x${item.quantity}: ₹${item.total}"
        }

        return """
            🧾 *INVOICE* 🧾
            
            Invoice: ${invoice.invoiceNumber}
            Date: ${Date(invoice.invoiceDate)}
            
            Customer: ${invoice.customerName}
            WhatsApp: ${invoice.whatsappNumber}
            
            ITEMS:
            $itemsText
            
            -----------------
            Subtotal: ₹${invoice.subtotal}
            Tax (10%): ₹${invoice.tax}
            Total: ₹${invoice.totalAmount}
            
            Thank you for your order! 🎉
        """.trimIndent()
    }

    fun clearCart() {
        _selectedProducts.value = emptyList()
    }

    fun getTotalAmount(): Double {
        return _selectedProducts.value.sumOf { it.total }
    }
}