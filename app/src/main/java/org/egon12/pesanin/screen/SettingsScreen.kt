package org.egon12.pesanin.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.egon12.pesanin.R
import org.egon12.pesanin.viewmodels.SettingsViewModel

@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        SettingsSection(title = stringResource(R.string.setting_header_business)) {
            SettingsTextField(
                value = uiState.shopName,
                onValueChange = viewModel::setShopName,
                label = stringResource(R.string.setting_business_name)
            )
            SettingsTextField(
                value = uiState.shopPhone,
                onValueChange = viewModel::setShopPhone,
                label = stringResource(R.string.setting_business_phone),
                keyboardType = KeyboardType.Phone
            )
            SettingsTextField(
                value = uiState.countryCode,
                onValueChange = viewModel::setCountryCode,
                label = stringResource(R.string.setting_country_code),
                keyboardType = KeyboardType.Phone
            )
        }

        /*
        SettingsSection(title = stringResource(R.string.setting_header_order)) {
            SettingsTextField(
                value = uiState.taxPercentage,
                onValueChange = viewModel::setTaxPercentage,
                label = stringResource(R.string.setting_tax_percentage),
                keyboardType = KeyboardType.Decimal
            )
        }
         */

        SettingsSection(title = stringResource(R.string.setting_header_app)) {
            InfoRow(
                label = stringResource(R.string.setting_language),
                value = java.util.Locale.getDefault().displayLanguage
            )
            InfoRow(
                label = stringResource(R.string.setting_version),
                value = "1.0.0"
            )
        }
    }
}

@Composable
fun SettingsSection(
    title: String,
    content: @Composable () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold
        )
        content()
        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
    }
}

@Composable
fun SettingsTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType)
    )
}

@Composable
fun InfoRow(
    label: String,
    value: String
) {
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Text(text = label, style = MaterialTheme.typography.labelMedium)
        Text(text = value, style = MaterialTheme.typography.bodyLarge)
    }
}
