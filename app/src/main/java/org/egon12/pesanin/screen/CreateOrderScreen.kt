package org.egon12.pesanin.screen

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.egon12.pesanin.R
import org.egon12.pesanin.model.Product
import org.egon12.pesanin.util.openWhatsApp
import org.egon12.pesanin.viewmodels.CreateOrderSideEffect
import org.egon12.pesanin.viewmodels.CreateOrderUiState
import org.egon12.pesanin.viewmodels.CreateOrderViewModel
import org.egon12.pesanin.viewmodels.Item

@Composable
fun CreateOrderScreen(
    modifier: Modifier = Modifier,
    viewModel: CreateOrderViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(viewModel) {
        viewModel.sideEffect.collect { effect ->
            when (effect) {
                is CreateOrderSideEffect.OpenWhatsApp -> {
                    context.openWhatsApp(effect.phoneNumber, effect.message)
                }
            }
        }
    }

    if (state.isLoading) {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .fillMaxHeight(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CircularProgressIndicator()
        }
    } else {
        LazyVerticalGrid(
            modifier = modifier
                .fillMaxWidth()
                .padding(16.dp),
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(state.products) {
                ProductCard(
                    product = it,
                    qty = state.qtyProduct(it),
                    onAdd = { viewModel.addProduct(it) },
                    onRemove = { viewModel.removeProduct(it) })
            }
        }
    }

    if (state.showSummary) {
        ReviewBottomSheet(
            state = state,
            onDismiss = viewModel::hideSummary,
            onNameChange = viewModel::setCustomerName,
            onPhoneNumberChange = viewModel::setPhoneNumber,
            onAddItem = { item ->
                state.products.find { it.id == item.id }?.let { viewModel.addProduct(it) }
            },
            onRemoveItem = { item ->
                state.products.find { it.id == item.id }?.let { viewModel.removeProduct(it) }
            },
            onSend = viewModel::sendInvoice
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReviewBottomSheet(
    state: CreateOrderUiState,
    onDismiss: () -> Unit,
    onNameChange: (String) -> Unit,
    onPhoneNumberChange: (String) -> Unit,
    onAddItem: (Item) -> Unit,
    onRemoveItem: (Item) -> Unit,
    onSend: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.8f)
                .padding(horizontal = 16.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Summary(
                items = state.items,
                totalQty = state.totalQty,
                totalAmount = state.totalAmount,
                onAddItem = onAddItem,
                onRemoveItem = onRemoveItem,
                modifier = Modifier.weight(1f, fill = false)
            )
            PesaninTextField(
                value = state.customerName,
                onValueChange = onNameChange,
                label = stringResource(R.string.label_customer_name)
            )
            PesaninTextField(
                value = state.phoneNumber,
                onValueChange = onPhoneNumberChange,
                label = stringResource(R.string.label_phone_number),
                keyboardType = KeyboardType.Phone,
                leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) }
            )
            ActionButtons(
                isLoading = state.isLoading,
                isSaveEnabled = state.isSaveEnabled,
                onSend = onSend
            )
        }
    }
}

@Composable
fun OrderItemCard(
    item: Item,
    onAdd: () -> Unit,
    onRemove: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = item.name, style = MaterialTheme.typography.bodyLarge)
            Text(
                text = "@ ${formatter.format(item.price)}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilledTonalIconButton(onClick = onRemove, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Default.Remove, contentDescription = null, modifier = Modifier.size(16.dp))
            }
            Text(
                text = item.qty.toString(),
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.width(24.dp)
            )
            FilledTonalIconButton(onClick = onAdd, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
            }
        }

        Text(
            text = formatter.format(item.totalPrice),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
fun PesaninTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    keyboardType: KeyboardType = KeyboardType.Text,
    leadingIcon: @Composable (() -> Unit)? = null,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = modifier.fillMaxWidth(),
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        leadingIcon = leadingIcon
    )
}


@Composable
fun ProductCard(
    product: Product, qty: Int, onAdd: () -> Unit, onRemove: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.Start,
        modifier = Modifier
            .border(
                2.dp, MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(12.dp)
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
            FilledTonalIconButton(onClick = onRemove) {
                Icon(
                    Icons.Default.Remove, contentDescription = stringResource(R.string.action_minus)
                )
            }
            Text(qty.toString())
            FilledIconButton(onClick = onAdd) {
                Icon(
                    Icons.Default.Add, contentDescription = stringResource(R.string.action_plus)
                )
            }
        }
    }
}


@Composable
fun Summary(
    items: List<Item>,
    totalQty: Int,
    totalAmount: Double,
    onAddItem: (Item) -> Unit,
    onRemoveItem: (Item) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .weight(1f, fill = false)
                    .verticalScroll(rememberScrollState())
            ) {
                items.forEach {
                    OrderItemCard(
                        item = it,
                        onAdd = { onAddItem(it) },
                        onRemove = { onRemoveItem(it) }
                    )
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(stringResource(R.string.label_total_items), style = MaterialTheme.typography.bodyMedium)
                Text("$totalQty", style = MaterialTheme.typography.bodyLarge)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(stringResource(R.string.label_total), style = MaterialTheme.typography.bodyMedium)
                Text(
                    text = formatter.format(totalAmount),
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
fun ActionButtons(
    isLoading: Boolean, isSaveEnabled: Boolean, onSend: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Button(
            onClick = onSend, modifier = Modifier.weight(2f), enabled = isSaveEnabled
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp), strokeWidth = 2.dp
                )
            } else {
                Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null)
                Text(stringResource(R.string.action_send_whatsapp))
            }
        }
    }
}
