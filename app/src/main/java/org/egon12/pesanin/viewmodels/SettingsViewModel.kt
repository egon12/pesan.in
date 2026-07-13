package org.egon12.pesanin.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import org.egon12.pesanin.repository.SettingsRepository
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    val uiState: StateFlow<SettingsUiState> = combine(
        settingsRepository.shopName,
        settingsRepository.shopPhone,
        settingsRepository.taxPercentage,
        settingsRepository.countryCode,
    ) { shopName, shopPhone, taxPercentage, countryCode ->
        SettingsUiState(
            shopName = shopName,
            shopPhone = shopPhone,
            taxPercentage = taxPercentage.toString(),
            countryCode = countryCode,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SettingsUiState()
    )

    fun setShopName(name: String) {
        settingsRepository.setShopName(name)
    }

    fun setShopPhone(phone: String) {
        settingsRepository.setShopPhone(phone)
    }

    fun setTaxPercentage(percentageStr: String) {
        val percentage = percentageStr.toFloatOrNull() ?: 0f
        settingsRepository.setTaxPercentage(percentage)
    }

    fun setCountryCode(code: String) {
        settingsRepository.setCountryCode(code)
    }
}

data class SettingsUiState(
    val shopName: String = "",
    val shopPhone: String = "",
    val taxPercentage: String = "0",
    val countryCode: String = "+62",
)
