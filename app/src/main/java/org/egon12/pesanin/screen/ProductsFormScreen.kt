package org.egon12.pesanin.screen

// ProductFormScreen.kt
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import org.egon12.pesanin.viewmodels.MainViewModel
import org.egon12.pesanin.viewmodels.ProductUiState
import org.egon12.pesanin.viewmodels.ProductViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductFormScreen(
    modifier: Modifier,
    productId: String?,
    onError: (String) -> Unit,
    viewModel: ProductViewModel = hiltViewModel(),
) {
    val scope = rememberCoroutineScope()

    val product by viewModel.selectedProduct.collectAsState()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    var shortName by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var nameError by remember { mutableStateOf<String?>(null) }
    var priceError by remember { mutableStateOf<String?>(null) }

    val nameFocusRequester = remember { FocusRequester() }

    // Load product if editing
    LaunchedEffect(productId) {
        if (productId != null) {
            // In a real app, you would fetch the product here
            // For simplicity, we'll assume the product is already selected
        }
    }

    // Update form when product loads
    LaunchedEffect(product) {
        product?.let {
            name = it.name
            price = it.price.toString()
            nameFocusRequester.requestFocus()
        }
    }

    // Handle UI State
    when (uiState) {
        is ProductUiState.Success -> {
            val message = (uiState as ProductUiState.Success).message
            if (productId == null) {
                // Clear form for new product
                name = ""
                price = ""
                nameError = null
                priceError = null
                nameFocusRequester.requestFocus()
            }
        }

        is ProductUiState.Error -> {
            val message = (uiState as ProductUiState.Error).message
            onError(message)
        }

        else -> {}
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // Product Name
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    "Tambah Produk",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        nameError = null
                    },
                    label = { Text("Nama") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(nameFocusRequester),
                    singleLine = true,
                    isError = nameError != null,
                    supportingText = {
                        if (nameError != null) {
                            Text(nameError!!)
                        }
                    },
                    leadingIcon = {
                        Icon(Icons.Default.Inventory, contentDescription = null)
                    }
                )

                OutlinedTextField(
                    value = shortName,
                    onValueChange = {
                        shortName = it
                        nameError = null
                    },
                    label = { Text("Nama Pendek") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    isError = nameError != null,
                    supportingText = {
                        if (nameError != null) {
                            Text(nameError!!)
                        }
                    },
                    leadingIcon = {
                        Icon(Icons.Default.Inventory, contentDescription = null)
                    }
                )


                OutlinedTextField(
                    value = price,
                    onValueChange = {
                        price = it
                        priceError = null
                    },
                    label = { Text("Harga") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    isError = priceError != null,
                    supportingText = {
                        if (priceError != null) {
                            Text(priceError!!)
                        }
                    },
                    leadingIcon = {
                        Text("Rp")
                    },
                )
            }
        }

        // Summary
        /*
        if (name.isNotBlank() && price.isNotBlank() && price.toDoubleOrNull() != null) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Summary",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Product:")
                        Text(name, style = MaterialTheme.typography.bodyLarge)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Price:")
                        Text(
                            "₹$price",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }

         */

        // Save Button
        Button(
            onClick = {
                // Validate
                var isValid = true

                if (name.isBlank()) {
                    nameError = "Product name is required"
                    isValid = false
                }

                val priceValue = price.toDoubleOrNull()
                if (priceValue == null || priceValue <= 0) {
                    priceError = "Please enter a valid price"
                    isValid = false
                }

                if (isValid) {
                    scope.launch {
                        if (productId == null) {
                            viewModel.createProduct(shortName, name, price)
                        } else {
                            viewModel.updateProduct(productId, name, price)
                        }
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            enabled = name.isNotBlank() && price.isNotBlank()
        ) {
            if (uiState is ProductUiState.Loading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Icon(Icons.Default.Save, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    if (productId == null) "Add Product"
                    else "Update Product"
                )
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductFormTopBar(
    mainViewModel: MainViewModel = hiltViewModel()
) {
    TopAppBar(
        title = {
            Text(
                "Tambah"
                /*
                if (productId == null) "Add Product"
                else "Edit Product"
                 */
            )
        },
        navigationIcon = {
            IconButton(onClick = mainViewModel::back) {
                Icon(Icons.Default.ArrowBackIosNew, contentDescription = "Back")
            }
        }
    )
}