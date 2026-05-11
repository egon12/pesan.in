package org.egon12.pesanin.repository

import android.content.Context
import android.content.SharedPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingsRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("pesanin_settings", Context.MODE_PRIVATE)

    private val _shopName = MutableStateFlow(prefs.getString(KEY_SHOP_NAME, "Toko Saya") ?: "Toko Saya")
    val shopName: StateFlow<String> = _shopName.asStateFlow()

    private val _shopPhone = MutableStateFlow(prefs.getString(KEY_SHOP_PHONE, "") ?: "")
    val shopPhone: StateFlow<String> = _shopPhone.asStateFlow()

    private val _taxPercentage = MutableStateFlow(prefs.getFloat(KEY_TAX_PERCENTAGE, 10f))
    val taxPercentage: StateFlow<Float> = _taxPercentage.asStateFlow()

    fun setShopName(name: String) {
        prefs.edit().putString(KEY_SHOP_NAME, name).apply()
        _shopName.value = name
    }

    fun setShopPhone(phone: String) {
        prefs.edit().putString(KEY_SHOP_PHONE, phone).apply()
        _shopPhone.value = phone
    }

    fun setTaxPercentage(percentage: Float) {
        prefs.edit().putFloat(KEY_TAX_PERCENTAGE, percentage).apply()
        _taxPercentage.value = percentage
    }

    companion object {
        private const val KEY_SHOP_NAME = "shop_name"
        private const val KEY_SHOP_PHONE = "shop_phone"
        private const val KEY_TAX_PERCENTAGE = "tax_percentage"
    }
}
