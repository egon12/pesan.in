package org.egon12.pesanin.screen

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.egon12.pesanin.model.Product
import org.egon12.pesanin.viewmodels.CreateOrderUiState
import org.egon12.pesanin.viewmodels.CreateOrderViewModel
import org.egon12.pesanin.viewmodels.Item
import java.text.DecimalFormat

val formatter = DecimalFormat("Rp#,###.##")

@Composable
fun CreateOrderScreen(
    modifier: Modifier = Modifier,
    viewModel: CreateOrderViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LazyVerticalGrid(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        columns = GridCells.Fixed(2),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(state.products) {
            ProductCard(it, state, viewModel)
        }
    }

    if (state.showSummary) {
        ReviewBottomSheet()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReviewBottomSheet(
    viewModel: CreateOrderViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    ModalBottomSheet(
        onDismissRequest = { viewModel.hideSummary() },
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 32.dp), // Extra padding for the navigation bar area
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Summary(state)
            CustomerName(state, viewModel::setCustomerName)
            CustomerPhoneNumber(state, viewModel::setPhoneNumber)
            ActionButtons(state, viewModel)
        }
    }
}

@Composable
fun OrderItemCard(
    item: Item
) {
    Row(
        modifier = Modifier.Companion
            .fillMaxWidth()
            .padding(12.dp),
        verticalAlignment = Alignment.Companion.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Column(
            modifier = Modifier.Companion.weight(1f)
        ) {
            Text(
                text = item.name, style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = "@ Rp${item.price}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        }

        // Quantity Selector
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {

            Text(
                text = item.qty.toString(),
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.width(24.dp)
            )

        }

        // Total and Remove
        Column(
            horizontalAlignment = Alignment.End
        ) {
            Text(
                text = formatter.format(item.totalPrice),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}


@Composable
fun CustomerName(
    state: CreateOrderUiState,
    onNameChange: (String) -> Unit,
) {
    OutlinedTextField(
        value = state.customerName,
        onValueChange = onNameChange,
        label = { Text("Nama Pelanggan") },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
    )
}

@Composable
fun CustomerPhoneNumber(
    state: CreateOrderUiState,
    onPhoneNumberChange: (String) -> Unit,
) {
    OutlinedTextField(
        value = state.phoneNumber,
        onValueChange = onPhoneNumberChange,
        label = { Text("No. Telp Pelanggan") },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
        leadingIcon = {
            Icon(Icons.Default.Phone, contentDescription = "Telepon")
        })
}


@Composable
fun ProductCard(product: Product, state: CreateOrderUiState, viewModel: CreateOrderViewModel) {
    Column(
        horizontalAlignment = Alignment.Start,
        modifier = Modifier
            .border(
                1.dp, MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(12.dp)
            )
            .padding(horizontal = 12.dp, vertical = 12.dp),
    ) {
        Text(
            product.shortName.ifBlank { product.name }, style = MaterialTheme.typography.titleMedium
        )
        Text(
            text = formatter.format(product.price), color = MaterialTheme.colorScheme.primary
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            FilledTonalIconButton(onClick = { viewModel.removeProduct(product) }) {
                Icon(
                    Icons.Default.Remove, contentDescription = "kurangi"
                )
            }
            Text(state.qtyProduct(product).toString())
            FilledIconButton(
                onClick = { viewModel.addProduct(product) },
            ) {
                Icon(
                    Icons.Default.Add, contentDescription = "tambah"
                )
            }
        }
    }
}


@Composable
fun Summary(state: CreateOrderUiState) {
    Card(
        modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            state.items.forEach {
                OrderItemCard(it )
            }
            Row(
                modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Jumlah barang:", style = MaterialTheme.typography.bodyMedium)
                Text(
                    "${state.items.sumOf { it.qty }}", style = MaterialTheme.typography.bodyLarge
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Total:", style = MaterialTheme.typography.bodyMedium)
                Text(
                    formatter.format(state.totalAmount),
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
fun ActionButtons(state: CreateOrderUiState, viewModel: CreateOrderViewModel) {

    Row(
        modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Button(
            onClick = { viewModel.sendInvoice() }, modifier = Modifier.weight(2f), enabled = state.isSaveEnabled
        ) {
            if (state.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp), strokeWidth = 2.dp
                )
            } else {
                Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null)
                Text("Send Invoice via WhatsApp")
            }
        }
    }
}
