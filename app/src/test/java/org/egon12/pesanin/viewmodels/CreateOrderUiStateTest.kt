package org.egon12.pesanin.viewmodels

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CreateOrderUiStateTest {

    private val item1 = Item(id = "1", shortName = "P1", name = "Product 1", price = 10000.0, qty = 2)
    private val item2 = Item(id = "2", shortName = "P2", name = "Product 2", price = 5000.0, qty = 3)

    @Test
    fun `Item totalPrice is qty times price`() {
        assertEquals(20000.0, item1.totalPrice, 0.0)
        assertEquals(15000.0, item2.totalPrice, 0.0)
    }

    @Test
    fun `totalAmount sums all item totalPrices`() {
        val state = CreateOrderUiState(items = listOf(item1, item2))
        assertEquals(35000.0, state.totalAmount, 0.0)
    }

    @Test
    fun `totalQty sums all item quantities`() {
        val state = CreateOrderUiState(items = listOf(item1, item2))
        assertEquals(5, state.totalQty)
    }

    @Test
    fun `isSaveEnabled requires phone, items, and not loading`() {
        val base = CreateOrderUiState(phoneNumber = "08123", items = listOf(item1), isLoading = false)
        assertTrue(base.isSaveEnabled)
        assertFalse(base.copy(phoneNumber = "").isSaveEnabled)
        assertFalse(base.copy(items = emptyList()).isSaveEnabled)
        assertFalse(base.copy(isLoading = true).isSaveEnabled)
    }

    @Test
    fun `isSaveOnlyEnabled requires only items and not loading`() {
        val base = CreateOrderUiState(items = listOf(item1), isLoading = false)
        assertTrue(base.isSaveOnlyEnabled)
        assertTrue(base.copy(phoneNumber = "").isSaveOnlyEnabled)
        assertFalse(base.copy(items = emptyList()).isSaveOnlyEnabled)
        assertFalse(base.copy(isLoading = true).isSaveOnlyEnabled)
    }

    @Test
    fun `empty state has zero totals`() {
        val state = CreateOrderUiState()
        assertEquals(0.0, state.totalAmount, 0.0)
        assertEquals(0, state.totalQty)
    }
}
