package org.egon12.pesanin.viewmodels

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.egon12.pesanin.model.Invoice
import org.egon12.pesanin.model.Order
import org.egon12.pesanin.model.OrderItem
import org.egon12.pesanin.model.Product
import org.egon12.pesanin.repository.OrderRepository
import org.egon12.pesanin.repository.ProductRepository
import org.egon12.pesanin.screen.formatter
import java.util.Date
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class CreateOrderViewModel @Inject constructor(
    private val repository: OrderRepository,
    private val productRepository: ProductRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        CreateOrderUiState(
            isLoading = true,
        )
    )
    val uiState: StateFlow<CreateOrderUiState> = _uiState.asStateFlow()

    private val _sideEffect = MutableSharedFlow<CreateOrderSideEffect>()
    val sideEffect = _sideEffect.asSharedFlow()

    init {
        viewModelScope.launch {
            productRepository.getAllProducts().collect { products ->
                _uiState.update { it.copy(products = products, isLoading = false) }
            }
        }
    }

    fun setPhoneNumber(number: String) {
        _uiState.update { it.copy(phoneNumber = number) }
    }

    fun setCustomerName(name: String) {
        _uiState.update { it.copy(customerName = name) }
    }

    fun addProduct(product: Product) {
        _uiState.update {
            val index = it.items.indexOfFirst { item -> item.id == product.id }

            val items: List<Item> = (if (index >= 0) {
                val items = it.items.toMutableList()
                val item = items[index]
                items[index] = item.copy(qty = item.qty + 1)
                items
            } else {
                it.items + Item(
                    id = product.id,
                    shortName = product.shortName,
                    name = product.name,
                    price = product.price,
                    qty = 1
                )
            })

            it.copy(items = items)
        }
    }

    fun removeProduct(product: Product) {
        _uiState.update {
            val index = it.items.indexOfFirst { item -> item.id == product.id }
            if (index < 0) {
                return@update it
            }

            val items = it.items.toMutableList()
            val item = items[index]
            if (item.qty > 1) {
                items[index] = item.copy(qty = item.qty - 1)
            } else {
                items.removeAt(index)
            }

            it.copy(items = items)
        }
    }

    fun addItem(addedItem: Item) {
        _uiState.update {
            val index = it.items.indexOfFirst { item -> item.id == addedItem.id }
            if (index < 0) {
                return@update it
            }

            val items = it.items.toMutableList()
            val item = items[index]
            items[index] = item.copy(qty = item.qty + 1)
            items

            it.copy(items = items)
        }
    }

    fun removeItem(removedItem: Item) {
        _uiState.update {
            val index = it.items.indexOfFirst { item -> item.id == removedItem.id }

            if (index < 0) {
                return@update it
            }

            val items = if (it.items[index].qty > 1) {
                val items = it.items.toMutableList()
                val item = items[index]
                items[index] = item.copy(qty = item.qty - 1)
                items
            } else {
                val items = it.items.toMutableList()
                items.removeAt(index)
                items
            }

            it.copy(items = items)
        }
    }

    fun clearItem(item: Item) {
        _uiState.update {
            val index = it.items.indexOfFirst { it.id == item.id }

            if (index < 0) {
                return@update it
            }

            val items = it.items.toMutableList()
            items.removeAt(index)

            it.copy(items = items)
        }
    }


    private fun formatInvoiceMessage(invoice: Invoice): String {
        val itemsText = invoice.items.joinToString("\n") { item ->
            "${item.productName} x${item.quantity}: ${formatter.format(item.total)}"
        }

        val dateFormat = java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale.getDefault())

        return """
            🧾 *INVOICE* 🧾
            
            Invoice: ${invoice.invoiceNumber}
            Date: ${dateFormat.format(Date(invoice.invoiceDate))}
            
            Customer: ${invoice.customerName}
            WhatsApp: ${invoice.whatsappNumber}
            
            ITEMS:
            $itemsText
            
            -----------------
            Subtotal: ${formatter.format(invoice.subtotal)}
            Tax (10%): ${formatter.format(invoice.tax)}
            Total: ${formatter.format(invoice.totalAmount)}
            
            Thank you for your order! 🎉
        """.trimIndent()
    }

    fun sendInvoice() {
        viewModelScope.launch {
            val currentState = _uiState.value
            if (!currentState.isSaveEnabled) return@launch

            _uiState.update { it.copy(isLoading = true) }

            try {
                val orderId = UUID.randomUUID().toString()
                val orderItems = currentState.items.map { item ->
                    OrderItem(
                        orderId = orderId,
                        productId = item.id,
                        productName = item.name,
                        quantity = item.qty,
                        price = item.price,
                        total = item.totalPrice
                    )
                }
                val order = Order(
                    id = orderId,
                    whatsappNumber = currentState.phoneNumber,
                    customerName = currentState.customerName.ifBlank { "Pelanggan" },
                    items = orderItems,
                    totalAmount = currentState.totalAmount
                )

                repository.createOrder(order, orderItems)
                val invoice = repository.generateInvoice(order)
                val message = formatInvoiceMessage(invoice)

                _sideEffect.emit(CreateOrderSideEffect.OpenWhatsApp(invoice.whatsappNumber, message))

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        items = emptyList(),
                        showSummary = false,
                        phoneNumber = "",
                        customerName = ""
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun clearCart() {
        _uiState.update { it.copy(items = emptyList()) }
    }

    fun showSummary() {
        _uiState.update { it.copy(showSummary = true) }
    }

    fun hideSummary() {
        _uiState.update { it.copy(showSummary = false) }
    }
}

data class CreateOrderUiState(
    val isLoading: Boolean = true,
    val phoneNumber: String = "",
    val customerName: String = "",
    val items: List<Item> = emptyList(),
    val products: List<Product> = emptyList(),
    val showSummary: Boolean = false,
) {
    val isSaveEnabled: Boolean
        get() = phoneNumber.isNotEmpty() && items.isNotEmpty() && !isLoading;

    fun qtyProduct(product: Product): Int {
        val item = items.firstOrNull { item -> item.id == product.id } ?: return 0
        return item.qty
    }

    val totalAmount: Double
        get() = items.sumOf { it.totalPrice }
}

sealed class CreateOrderSideEffect {
    data class OpenWhatsApp(val phoneNumber: String, val message: String) : CreateOrderSideEffect()
}

data class Item(
    val id: String,
    val shortName: String,
    val name: String,
    val price: Double,
    val qty: Int,
) {
    val totalPrice: Double
        get() = qty * price
}

@Composable
fun CartFab(
    itemCount: Int,
    total: Double,
    onClick: () -> Unit,
) {
    if (itemCount == 0) return

    ExtendedFloatingActionButton(
        onClick = onClick,
        containerColor = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary,
    ) {
        Icon(Icons.Default.ShoppingCart, contentDescription = null)
        Spacer(Modifier.width(8.dp))
        Text("$itemCount item  •  ${formatter.format(total)}")
    }
}
