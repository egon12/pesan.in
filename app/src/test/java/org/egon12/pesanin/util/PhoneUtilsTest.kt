package org.egon12.pesanin.util

import org.junit.Assert.assertEquals
import org.junit.Test

class PhoneUtilsTest {

    @Test
    fun `number starting with + is used as-is`() {
        assertEquals("+628123456789", normalizePhone("+628123456789", "+62"))
    }

    @Test
    fun `number starting with 0 replaces leading zero with country code`() {
        assertEquals("+628123456789", normalizePhone("08123456789", "+62"))
    }

    @Test
    fun `number without prefix prepends country code`() {
        assertEquals("+628123456789", normalizePhone("8123456789", "+62"))
    }

    @Test
    fun `already correct country code is not doubled`() {
        assertEquals("+6281234", normalizePhone("+6281234", "+62"))
    }

    @Test
    fun `works with different country codes`() {
        assertEquals("+1555123456", normalizePhone("0555123456", "+1"))
    }
}
