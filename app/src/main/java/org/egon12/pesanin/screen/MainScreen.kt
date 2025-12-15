package org.egon12.pesanin.screen

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import kotlinx.coroutines.launch
import org.egon12.pesanin.model.OrderItem
import org.egon12.pesanin.model.Product
import org.egon12.pesanin.viewmodels.OrderViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: OrderViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val whatsappNumber by viewModel.whatsappNumber.collectAsState()
    val customerName by viewModel.customerName.collectAsState()
    val selectedProducts: List<OrderItem> by viewModel.selectedProducts.collectAsState()
    val products: List<Product> by viewModel.allProducts.collectAsState(initial = emptyList())

    var isLoading by remember { mutableStateOf(false) }
    var showProductDialog by remember { mutableStateOf(false) }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Order Management") },
                actions = {
                    IconButton(onClick = { /* Navigate to orders list */ }) {
                        Icon(Icons.Default.List, contentDescription = "Orders")
                    }
                }
            )
        },
        floatingActionButton = {
            if (selectedProducts.isNotEmpty()) {
                FloatingActionButton(
                    onClick = { showProductDialog = true },
                    containerColor = MaterialTheme.colorScheme.primary
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Product")
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Customer Information
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Customer Information",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = whatsappNumber,
                        onValueChange = viewModel::setWhatsappNumber,
                        label = { Text("WhatsApp Number") },
                        placeholder = { Text("Enter WhatsApp number with country code") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        leadingIcon = {
                            Icon(Icons.Default.Phone, contentDescription = "Phone")
                        }
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = customerName,
                        onValueChange = viewModel::setCustomerName,
                        label = { Text("Customer Name (Optional)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        leadingIcon = {
                            Icon(Icons.Default.Person, contentDescription = "Person")
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Products Selection
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Products",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Button(
                            onClick = { showProductDialog = true },
                            enabled = whatsappNumber.isNotEmpty()
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null)
                            Text("Add Product")
                        }
                    }

                    if (selectedProducts.isEmpty()) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Spacer(modifier = Modifier.height(32.dp))
                            Icon(
                                Icons.Default.ShoppingCart,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "No products selected",
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            )
                            Text(
                                text = "Tap 'Add Product' to start",
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            )
                            Spacer(modifier = Modifier.height(32.dp))
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(selectedProducts, key = { it.productId }) { item ->
                                OrderItemCard(
                                    item = item,
                                    onRemove = { viewModel.removeProductFromOrder(item.productId) },
                                    onQuantityChange = { quantity ->
                                        viewModel.updateProductQuantity(item.productId, quantity)
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // Summary and Actions
            if (selectedProducts.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Total Items:", style = MaterialTheme.typography.bodyMedium)
                            Text(
                                "${selectedProducts.sumOf { it.quantity }}",
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Total Amount:", style = MaterialTheme.typography.bodyMedium)
                            Text(
                                "₹${viewModel.getTotalAmount()}",
                                style = MaterialTheme.typography.headlineSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Button(
                        onClick = {
                            viewModel.clearCart()
                            scope.launch {
                                snackbarHostState.showSnackbar("Cart cleared")
                            }
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer,
                            contentColor = MaterialTheme.colorScheme.onErrorContainer
                        )
                    ) {
                        Icon(Icons.Default.Clear, contentDescription = null)
                        Text("Clear")
                    }

                    Button(
                        onClick = {
                            scope.launch {
                                isLoading = true
                                val result = viewModel.sendInvoice()
                                isLoading = false

                                if (result.isSuccess) {
                                    val message = result.getOrNull() ?: ""
                                    val intent = Intent(Intent.ACTION_VIEW).apply {
                                        data = Uri.parse(
                                            "https://api.whatsapp.com/send?phone=$whatsappNumber&text=${
                                                Uri.encode(
                                                    message
                                                )
                                            }"
                                        )
                                        `package` = "com.whatsapp"
                                    }

                                    try {
                                        context.startActivity(intent)
                                    } catch (e: Exception) {
                                        // WhatsApp not installed, open in browser
                                        val webIntent = Intent(Intent.ACTION_VIEW).apply {
                                            data = Uri.parse(
                                                "https://web.whatsapp.com/send?phone=$whatsappNumber&text=${
                                                    Uri.encode(
                                                        message
                                                    )
                                                }"
                                            )
                                        }
                                        context.startActivity(webIntent)
                                    }
                                } else {
                                    snackbarHostState.showSnackbar(
                                        "Error: ${result.exceptionOrNull()?.message}"
                                    )
                                }
                            }
                        },
                        modifier = Modifier.weight(2f),
                        enabled = whatsappNumber.isNotEmpty() && selectedProducts.isNotEmpty() && !isLoading
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
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
    }
}

@Composable
fun OrderItemCard(
    item: OrderItem,
    onRemove: () -> Unit,
    onQuantityChange: (Int) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Column(
                modifier = Modifier.weight(1f)
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
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                IconButton(
                    onClick = {
                        if (item.quantity > 1) {
                            onQuantityChange(item.quantity - 1)
                        }
                    },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(Icons.Filled.Close, contentDescription = "Decrease")
                }

                Text(
                    text = item.quantity.toString(),
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.width(24.dp)
                )

                IconButton(
                    onClick = {
                        onQuantityChange(item.quantity + 1)
                    },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Increase")
                }
            }

            // Total and Remove
            Column(
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = "₹${item.total}",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.primary
                )
                IconButton(
                    onClick = onRemove,
                    modifier = Modifier.size(24.dp)
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
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(products) { product ->
                    Card(
                        onClick = { onProductSelected(product) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Column(
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = product.name,
                                    style = MaterialTheme.typography.bodyLarge
                                )
                                Text(
                                    text = product.description,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                                    maxLines = 2
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