package org.kotakwarna.pesanin.viewmodels

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
import org.kotakwarna.pesanin.R
import org.kotakwarna.pesanin.model.Invoice
import org.kotakwarna.pesanin.model.Order
import org.kotakwarna.pesanin.model.OrderItem
import org.kotakwarna.pesanin.model.Product
import org.kotakwarna.pesanin.repository.OrderRepository
import org.kotakwarna.pesanin.repository.ProductRepository
import org.kotakwarna.pesanin.screen.formatter
import org.kotakwarna.pesanin.util.normalizePhone
import java.util.Date
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class CreateOrderViewModel @Inject constructor(
    private val repository: OrderRepository,
    private val productRepository: ProductRepository,
    private val settingsRepository: org.kotakwarna.pesanin.repository.SettingsRepository
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
        val context = org.kotakwarna.pesanin.PesaninApp.instance
        val itemsText = invoice.items.joinToString("\n") { item ->
            "${item.productName} x${item.quantity}: ${formatter.format(item.total)}"
        }

        val dateFormat = java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale.getDefault())

        return """
            ${context.getString(R.string.invoice_header)}
            
            ${context.getString(R.string.invoice_date, dateFormat.format(Date(invoice.invoiceDate)))}
            
            ${context.getString(R.string.invoice_customer, invoice.customerName)}
            ${context.getString(R.string.invoice_whatsapp, invoice.whatsappNumber)}
            
            ${context.getString(R.string.invoice_items_header)}
            $itemsText
            
            -----------------
            ${context.getString(R.string.invoice_subtotal, formatter.format(invoice.subtotal))}
            ${context.getString(R.string.invoice_tax, "${settingsRepository.taxPercentage.value}%", formatter.format(invoice.tax))}
            ${context.getString(R.string.invoice_total, formatter.format(invoice.totalAmount))}
            
            ${context.getString(R.string.invoice_footer)}
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

                val phone = normalizePhone(invoice.whatsappNumber, settingsRepository.countryCode.value)
                _sideEffect.emit(CreateOrderSideEffect.OpenWhatsApp(phone, message))

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

    fun saveOrder() {
        viewModelScope.launch {
            val currentState = _uiState.value
            if (!currentState.isSaveOnlyEnabled) return@launch

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

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        items = emptyList(),
                        showSummary = false,
                        phoneNumber = "",
                        customerName = ""
                    )
                }
                _sideEffect.emit(CreateOrderSideEffect.OrderSaved)
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
        get() = phoneNumber.isNotEmpty() && items.isNotEmpty() && !isLoading

    val isSaveOnlyEnabled: Boolean
        get() = items.isNotEmpty() && !isLoading

    fun qtyProduct(product: Product): Int {
        val item = items.firstOrNull { item -> item.id == product.id } ?: return 0
        return item.qty
    }

    val totalAmount: Double
        get() = items.sumOf { it.totalPrice }

    val totalQty: Int
        get() = items.sumOf { it.qty }
}

sealed class CreateOrderSideEffect {
    data class OpenWhatsApp(val phoneNumber: String, val message: String) : CreateOrderSideEffect()
    object OrderSaved : CreateOrderSideEffect()
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
