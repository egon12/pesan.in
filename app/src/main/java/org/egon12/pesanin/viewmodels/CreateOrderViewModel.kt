package org.egon12.pesanin.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.egon12.pesanin.model.Invoice
import org.egon12.pesanin.model.Product
import org.egon12.pesanin.repository.OrderRepository
import org.egon12.pesanin.repository.ProductRepository
import java.util.Date
import javax.inject.Inject

@HiltViewModel
class CreateOrderViewModel @Inject constructor(
    private val repository: OrderRepository,
    private val productRepository: ProductRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow<CreateOrderUiState>(
        CreateOrderUiState(
            isLoading = true,
        )
    );
    val uiState: StateFlow<CreateOrderUiState> = _uiState.asStateFlow();

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
        _uiState.update { it.copy(items = emptyList()) }
    }
}

data class CreateOrderUiState(
    val isLoading: Boolean = true,
    val phoneNumber: String = "",
    val items: List<Item> = emptyList(),
    val products: List<Product> = emptyList(),
) {
    val isSaveEnabled: Boolean
        get() = phoneNumber.isNotEmpty() && items.isNotEmpty() && !isLoading;

    val totalAmount: Double
        get() = items.sumOf { it.totalPrice }
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