package org.kotakwarna.pesanin.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.net.toUri

fun Context.openWhatsApp(phoneNumber: String, message: String) {
    val uri = "https://api.whatsapp.com/send?phone=$phoneNumber&text=${Uri.encode(message)}".toUri()
    val intent = Intent(Intent.ACTION_VIEW, uri)
    startActivity(intent)
}