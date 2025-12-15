package org.egon12.pesanin.model

import java.util.Date

data class Invoice(
    val orderId: String,
    val customerName: String,
    val whatsappNumber: String,
    val items: List<OrderItem>,
    val subtotal: Double,
    val tax: Double = 0.0,
    val discount: Double = 0.0,
    val totalAmount: Double,
    val invoiceDate: Long = Date().time,
    val invoiceNumber: String
)