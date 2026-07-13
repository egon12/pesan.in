package org.egon12.pesanin.util

fun normalizePhone(phone: String, countryCode: String): String = when {
    phone.startsWith("+") -> phone
    phone.startsWith("0") -> countryCode + phone.substring(1)
    else -> countryCode + phone
}
