package org.egon12.pesanin.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.egon12.pesanin.model.OrderItem
import org.egon12.pesanin.model.Product
import org.egon12.pesanin.viewmodels.CreateOrderUiState
import org.egon12.pesanin.viewmodels.CreateOrderViewModel

@Composable
fun CreateOrderScreen(
    modifier: Modifier = Modifier,
    viewModel: CreateOrderViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle();

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        CustomerCard(state, viewModel::setPhoneNumber)
        Spacer(modifier = Modifier.Companion.height(16.dp))

        // Products Selection
        Card(
            modifier = Modifier.Companion.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(4.dp)
        ) {
            Column(
                modifier = Modifier.Companion.padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.Companion.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Companion.CenterVertically
                ) {
                    Text(
                        text = "Products",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                LazyVerticalGrid(
                    // 1. Define a fixed number of columns
                    columns = GridCells.Fixed(3)
                ) {
                    // 2. Populate the grid with items
                    items(state.products) { item ->
                        Box(
                            modifier = Modifier
                                .padding(8.dp)
                                .aspectRatio(1f) // Makes the item a square
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color.LightGray)
                                .clickable { viewModel.addProduct(item) },

                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = "${item.shortName}\n${item.price}")
                        }
                    }
                }
            }

            // Summary and Actions
            if (state.items.isNotEmpty()) {
                Spacer(modifier = Modifier.Companion.height(16.dp))

                Card(
                    modifier = Modifier.Companion.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.Companion.padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.Companion.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Total Items:", style = MaterialTheme.typography.bodyMedium)
                            Text(
                                "${state.items.sumOf { it.qty }}",
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                        Spacer(modifier = Modifier.Companion.height(4.dp))
                        Row(
                            modifier = Modifier.Companion.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Total Amount:", style = MaterialTheme.typography.bodyMedium)
                            Text(
                                "₹${state.totalAmount}",
                                style = MaterialTheme.typography.headlineSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.Companion.height(16.dp))

                Row(
                    modifier = Modifier.Companion.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Button(
                        onClick = { viewModel.clearCart() },
                        modifier = Modifier.Companion.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer,
                            contentColor = MaterialTheme.colorScheme.onErrorContainer
                        )
                    ) {
                        Icon(Icons.Default.Clear, contentDescription = null)
                        Text("Clear")
                    }

                    Button(
                        onClick = { },
                        modifier = Modifier.Companion.weight(2f),
                        enabled = state.isSaveEnabled
                    ) {
                        if (state.isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.Companion.size(20.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(Icons.Default.Send, contentDescription = null)
                            Text("Send Invoice via WhatsApp")
                        }
                    }
                }
            }
        }

        /*
        // Product Selection Dialog
        if (showProductDialog) {
            ProductSelectionDialog(
                products = products,
                onDismiss = { showProductDialog = false },
                onProductSelected = { product ->
                    viewModel.addProductToOrder(product)
                    showProductDialog = false
                }
            )
        }

         */
    }
}

@Composable
fun OrderItemCard(
    item: OrderItem,
    onRemove: () -> Unit,
    onQuantityChange: (Int) -> Unit
) {
    Card(
        modifier = Modifier.Companion.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
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
                    text = item.productName,
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = "₹${item.price} each",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }

            // Quantity Selector
            Row(
                verticalAlignment = Alignment.Companion.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                IconButton(
                    onClick = {
                        if (item.quantity > 1) {
                            onQuantityChange(item.quantity - 1)
                        }
                    },
                    modifier = Modifier.Companion.size(32.dp)
                ) {
                    Icon(Icons.Filled.Remove, contentDescription = "Decrease")
                }

                Text(
                    text = item.quantity.toString(),
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.Companion.width(24.dp)
                )

                IconButton(
                    onClick = {
                        onQuantityChange(item.quantity + 1)
                    },
                    modifier = Modifier.Companion.size(32.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Increase")
                }
            }

            // Total and Remove
            Column(
                horizontalAlignment = Alignment.Companion.End
            ) {
                Text(
                    text = "₹${item.total}",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.primary
                )
                IconButton(
                    onClick = onRemove,
                    modifier = Modifier.Companion.size(24.dp)
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Remove")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductSelectionDialog(
    products: List<Product>,
    onDismiss: () -> Unit,
    onProductSelected: (Product) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select Product") },
        text = {
            LazyColumn(
                modifier = Modifier.Companion.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(products) { product ->
                    Card(
                        onClick = { onProductSelected(product) },
                        modifier = Modifier.Companion.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
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
                                    text = product.name,
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            }
                            Text(
                                text = "₹${product.price}",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}


// Customer Information
@Composable
fun CustomerCard(
    state: CreateOrderUiState,
    onPhoneNumberChange: (String) -> Unit,
) {
    Card(
        modifier = Modifier.Companion.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(4.dp),
    ) {
        Column(
            modifier = Modifier.Companion.padding(16.dp)
        ) {
            Text(
                text = "Pelanggan",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary
            )

            OutlinedTextField(
                value = state.phoneNumber,
                onValueChange = onPhoneNumberChange,
                label = { Text("No. Telp") },
                modifier = Modifier.Companion.fillMaxWidth(),
                singleLine = true,
                leadingIcon = {
                    Icon(Icons.Default.Phone, contentDescription = "Phone")
                }
            )
        }
    }
}