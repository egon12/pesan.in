package org.egon12.pesanin.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.egon12.pesanin.model.Order
import org.egon12.pesanin.model.OrderStatus
import org.egon12.pesanin.repository.OrderRepository
import javax.inject.Inject

@HiltViewModel
class OrdersViewModel @Inject constructor(
    private val orderRepository: OrderRepository
) : ViewModel() {

    val orders: StateFlow<List<Order>> = orderRepository.getAllOrders()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun updateStatus(order: Order, status: OrderStatus) {
        viewModelScope.launch {
            orderRepository.updateOrderStatus(order.id, status)
        }
    }
}
