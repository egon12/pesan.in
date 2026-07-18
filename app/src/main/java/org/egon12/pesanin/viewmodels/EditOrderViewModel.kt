package org.egon12.pesanin.viewmodels

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import org.egon12.pesanin.model.Product
import org.egon12.pesanin.repository.OrderRepository
import org.egon12.pesanin.repository.ProductRepository
import javax.inject.Inject

@HiltViewModel
class EditOrderViewModel @Inject constructor(
    private val orderRepository: OrderRepository,
    private val productRepository: ProductRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    val orderId: String = checkNotNull(savedStateHandle["orderId"])

    private val _uiState = MutableStateFlow<EditOrderUiState>(EditOrderUiState.Loading)
    val uiState: StateFlow<EditOrderUiState> = _uiState.asStateFlow()

    private val _sideEffect = MutableSharedFlow<EditOrderSideEffect>()
    val sideEffect = _sideEffect.asSharedFlow()

    init {
        TODO("load order and products")
    }

    fun setCustomerName(name: String): Unit = TODO()

    fun setPhoneNumber(phone: String): Unit = TODO()

    fun addProduct(product: Product): Unit = TODO()

    fun removeProduct(product: Product): Unit = TODO()

    fun saveOrder(): Unit = TODO()
}

sealed class EditOrderUiState {
    object Loading : EditOrderUiState()
    data class Success(
        val customerName: String,
        val phoneNumber: String,
        val items: List<Item>,
        val products: List<Product>,
    ) : EditOrderUiState()
    data class Error(val message: String) : EditOrderUiState()
}

sealed class EditOrderSideEffect {
    object Saved : EditOrderSideEffect()
}
