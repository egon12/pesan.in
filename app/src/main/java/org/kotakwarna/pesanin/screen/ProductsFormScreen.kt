package org.kotakwarna.pesanin.screen

// ProductFormScreen.kt
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
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import androidx.compose.ui.res.stringResource
import org.kotakwarna.pesanin.R
import org.kotakwarna.pesanin.viewmodels.MainViewModel
import org.kotakwarna.pesanin.viewmodels.ProductUiState
import org.kotakwarna.pesanin.viewmodels.ProductViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductFormScreen(
    modifier: Modifier,
    productId: String?,
    onError: (String) -> Unit,
    onSuccess: () -> Unit = {},
    viewModel: ProductViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val product by viewModel.selectedProduct.collectAsState()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    var shortName by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var nameError by remember { mutableStateOf<String?>(null) }
    var priceError by remember { mutableStateOf<String?>(null) }
    var saveAndDone by remember { mutableStateOf(false) }

    val nameFocusRequester = remember { FocusRequester() }

    LaunchedEffect(productId) {
        if (productId != null) {
            viewModel.loadProduct(productId)
        }
    }

    LaunchedEffect(product) {
        product?.let {
            name = it.name
            shortName = it.shortName
            price = it.price.toString()
            nameFocusRequester.requestFocus()
        }
    }

    LaunchedEffect(uiState) {
        when (uiState) {
            is ProductUiState.Success -> {
                if (productId == null && !saveAndDone) {
                    name = ""
                    shortName = ""
                    price = ""
                    nameError = null
                    priceError = null
                    nameFocusRequester.requestFocus()
                } else {
                    onSuccess()
                }
                saveAndDone = false
            }
            is ProductUiState.Error -> {
                onError((uiState as ProductUiState.Error).message)
                saveAndDone = false
            }
            else -> {}
        }
    }

    fun validateInputs(): Boolean {
        var isValid = true
        if (name.isBlank()) {
            nameError = context.getString(R.string.err_name_required)
            isValid = false
        }
        val priceValue = price.toDoubleOrNull()
        if (priceValue == null || priceValue <= 0) {
            priceError = context.getString(R.string.err_invalid_price)
            isValid = false
        }
        return isValid
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
                    stringResource(R.string.title_add_product),
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
                    label = { Text(stringResource(R.string.label_product_name)) },
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
                    label = { Text(stringResource(R.string.label_short_name)) },
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
                    label = { Text(stringResource(R.string.label_price)) },
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

        val isLoading = uiState is ProductUiState.Loading
        val formFilled = name.isNotBlank() && price.isNotBlank()

        if (productId == null) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        if (validateInputs()) {
                            saveAndDone = false
                            scope.launch { viewModel.createProduct(shortName, name, price) }
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp),
                    enabled = formFilled && !isLoading
                ) {
                    Text(stringResource(R.string.action_save_and_add_another))
                }

                Button(
                    onClick = {
                        if (validateInputs()) {
                            saveAndDone = true
                            scope.launch { viewModel.createProduct(shortName, name, price) }
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp),
                    enabled = formFilled && !isLoading
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Text(stringResource(R.string.action_save_and_done))
                    }
                }
            }
        } else {
            Button(
                onClick = {
                    if (validateInputs()) {
                        scope.launch { viewModel.updateProduct(productId, shortName, name, price) }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = formFilled && !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Icon(Icons.Default.Save, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(R.string.action_update_product))
                }
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductFormTopBar(
    productId: String?,
    mainViewModel: MainViewModel = hiltViewModel()
) {
    TopAppBar(
        title = {
            Text(
                if (productId == null) stringResource(R.string.title_add_product)
                else stringResource(R.string.title_edit_product)
            )
        },
        navigationIcon = {
            IconButton(onClick = mainViewModel::back) {
                Icon(Icons.Default.ArrowBackIosNew, contentDescription = stringResource(R.string.action_back))
            }
        }
    )
}