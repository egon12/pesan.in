package org.kotakwarna.pesanin.viewmodels

import androidx.lifecycle.SavedStateHandle
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import org.kotakwarna.pesanin.MainCoroutineRule
import org.kotakwarna.pesanin.model.Order
import org.kotakwarna.pesanin.model.OrderItem
import org.kotakwarna.pesanin.model.Product
import org.kotakwarna.pesanin.repository.OrderRepository
import org.kotakwarna.pesanin.repository.ProductRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class EditOrderViewModelTest {

    @get:Rule
    val coroutineRule = MainCoroutineRule()

    private val productA = Product(id = "prod-1", name = "Product A", shortName = "PA", price = 10000.0)
    private val productB = Product(id = "prod-2", name = "Product B", shortName = "PB", price = 5000.0)

    private val existingItem = OrderItem(
        id = "item-1", orderId = "order-1", productId = "prod-1",
        productName = "Product A", quantity = 2, price = 10000.0, total = 20000.0
    )
    private val testOrder = Order(
        id = "order-1",
        customerName = "Budi",
        whatsappNumber = "08123456789",
        items = listOf(existingItem),
        totalAmount = 20000.0
    )

    private lateinit var orderRepository: OrderRepository
    private lateinit var productRepository: ProductRepository

    @Before
    fun setUp() {
        orderRepository = mockk()
        productRepository = mockk()
        coEvery { orderRepository.getOrderWithItems("order-1") } returns testOrder
        every { productRepository.getAllProducts() } returns flowOf(listOf(productA, productB))
    }

    private fun createViewModel() = EditOrderViewModel(
        orderRepository = orderRepository,
        productRepository = productRepository,
        savedStateHandle = SavedStateHandle(mapOf("orderId" to "order-1"))
    )

    @Test
    fun `loads order data on init`() = runTest {
        val vm = createViewModel()
        val state = vm.uiState.value
        assertTrue(state is EditOrderUiState.Success)
        state as EditOrderUiState.Success
        assertEquals("Budi", state.customerName)
        assertEquals("08123456789", state.phoneNumber)
        assertEquals(1, state.items.size)
        assertEquals("prod-1", state.items[0].id)
        assertEquals(2, state.items[0].qty)
    }

    @Test
    fun `loads product list on init`() = runTest {
        val vm = createViewModel()
        val state = vm.uiState.value as EditOrderUiState.Success
        assertEquals(2, state.products.size)
    }

    @Test
    fun `setCustomerName updates customer name in state`() = runTest {
        val vm = createViewModel()
        vm.setCustomerName("Siti")
        val state = vm.uiState.value as EditOrderUiState.Success
        assertEquals("Siti", state.customerName)
    }

    @Test
    fun `setPhoneNumber updates phone number in state`() = runTest {
        val vm = createViewModel()
        vm.setPhoneNumber("081999888777")
        val state = vm.uiState.value as EditOrderUiState.Success
        assertEquals("081999888777", state.phoneNumber)
    }

    @Test
    fun `addProduct new product appends to items`() = runTest {
        val vm = createViewModel()
        vm.addProduct(productB)
        val state = vm.uiState.value as EditOrderUiState.Success
        assertEquals(2, state.items.size)
        assertTrue(state.items.any { it.id == "prod-2" })
    }

    @Test
    fun `addProduct existing product increases qty`() = runTest {
        val vm = createViewModel()
        vm.addProduct(productA)
        val state = vm.uiState.value as EditOrderUiState.Success
        assertEquals(1, state.items.size)
        assertEquals(3, state.items[0].qty)
    }

    @Test
    fun `removeProduct decreases qty by one`() = runTest {
        val vm = createViewModel()
        vm.removeProduct(productA)
        val state = vm.uiState.value as EditOrderUiState.Success
        assertEquals(1, state.items.size)
        assertEquals(1, state.items[0].qty)
    }

    @Test
    fun `removeProduct at qty 1 removes item from list`() = runTest {
        val vm = createViewModel()
        vm.removeProduct(productA)
        vm.removeProduct(productA)
        val state = vm.uiState.value as EditOrderUiState.Success
        assertTrue(state.items.isEmpty())
    }

    @Test
    fun `saveOrder calls updateOrder with recalculated totalAmount`() = runTest {
        coEvery { orderRepository.updateOrder(any(), any()) } returns Unit
        val vm = createViewModel()
        vm.saveOrder()
        coVerify {
            orderRepository.updateOrder(
                match<Order> { it.totalAmount == 20000.0 },
                any()
            )
        }
    }

    @Test
    fun `saveOrder emits Saved side effect`() = runTest {
        coEvery { orderRepository.updateOrder(any(), any()) } returns Unit
        val vm = createViewModel()
        val effects = mutableListOf<EditOrderSideEffect>()
        val job = launch { vm.sideEffect.toList(effects) }
        vm.saveOrder()
        job.cancel()
        assertTrue(effects.any { it is EditOrderSideEffect.Saved })
    }
}
