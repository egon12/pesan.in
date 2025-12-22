package org.egon12.pesanin.screen

import android.content.Context
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import kotlinx.coroutines.launch
import org.egon12.pesanin.model.Product
import org.egon12.pesanin.viewmodels.MainViewModel
import org.egon12.pesanin.viewmodels.ProductUiState
import org.egon12.pesanin.viewmodels.ProductViewModel
import org.egon12.pesanin.viewmodels.UiEvent


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductListScreen(
    onAddProduct: () -> Unit,
    onEditProduct: (String) -> Unit,
    viewModel: ProductViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val products by viewModel.products.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    val importResult by viewModel.importResult.collectAsState()

    var showDeleteDialog by remember { mutableStateOf(false) }
    var showImportDialog by remember { mutableStateOf(false) }
    var selectedProductForDelete by remember { mutableStateOf<Product?>(null) }
    var showCSVTemplate by remember { mutableStateOf(false) }

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let {
            scope.launch {
                val content = context.contentResolver.openInputStream(it)?.use { stream ->
                    stream.bufferedReader().readText()
                }
                content?.let { csvContent ->
                    viewModel.importFromCSV(csvContent)
                }
            }
        }
    }

    // Handle UI State
    LaunchedEffect(uiState) {
        when (uiState) {
            is ProductUiState.Success -> {
                val message = (uiState as ProductUiState.Success).message
                snackbarHostState.showSnackbar(message)
            }

            is ProductUiState.Error -> {
                val message = (uiState as ProductUiState.Error).message
                snackbarHostState.showSnackbar(message)
            }

            else -> {}
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
    ) {
        // Search Bar
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            elevation = CardDefaults.cardElevation(2.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Search,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                TextField(
                    value = searchQuery,
                    onValueChange = viewModel::setSearchQuery,
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Search products...") },
                    colors = TextFieldDefaults.colors(
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                        focusedContainerColor = MaterialTheme.colorScheme.surface
                    ),
                    singleLine = true
                )
            }
        }

        if (products.isEmpty()) {
            EmptyProductsView(
                onImportClick = { filePickerLauncher.launch(arrayOf("text/csv")) }
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(8.dp)
            ) {
                items(products, key = { it.id }) { product ->
                    ProductItemCard(
                        product = product,
                        onClick = { onEditProduct(product.id) },
                        onDelete = {
                            selectedProductForDelete = product
                            showDeleteDialog = true
                        }
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                }
            }
        }
    }

    // Delete Dialog
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Product") },
            text = {
                Text("Are you sure you want to delete '${selectedProductForDelete?.name}'?")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        scope.launch {
                            selectedProductForDelete?.id?.let { productId ->
                                viewModel.deleteProduct(productId)
                            }
                            showDeleteDialog = false
                            selectedProductForDelete = null
                        }
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Import Options Dialog
    if (showImportDialog) {
        AlertDialog(
            onDismissRequest = { showImportDialog = false },
            title = { Text("Import Products") },
            text = {
                Column {
                    Text("Import products from CSV file.")
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("CSV Format:", style = MaterialTheme.typography.titleSmall)
                    Text("name,price")
                    Text("\"Product Name\",100.0")
                    Text("\"Another Product\",200.0")
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showImportDialog = false
                        filePickerLauncher.launch(arrayOf("text/csv"))
                    }
                ) {
                    Text("Import CSV")
                }
            },
            dismissButton = {
                TextButton(onClick = { showImportDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Show Import Errors Dialog if needed
    importResult?.let { (successCount, errors) ->
        if (errors.isNotEmpty()) {
            AlertDialog(
                onDismissRequest = { viewModel.clearImportResult() },
                title = {
                    Text("Import Results")
                },
                text = {
                    Column {
                        Text("Successfully imported: $successCount products")
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Errors (${errors.size}):",
                            style = MaterialTheme.typography.titleSmall
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        LazyColumn(
                            modifier = Modifier.heightIn(max = 200.dp)
                        ) {
                            items(errors.take(10)) { error ->
                                Text(
                                    text = "• $error",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                            if (errors.size > 10) {
                                item {
                                    Text("...and ${errors.size - 10} more errors")
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { viewModel.clearImportResult() }) {
                        Text("OK")
                    }
                }
            )
        }
    }

    // CSV Template Dialog
    if (showCSVTemplate) {
        AlertDialog(
            onDismissRequest = { showCSVTemplate = false },
            title = { Text("CSV Template") },
            text = {
                Column {
                    Text("Required format:", style = MaterialTheme.typography.titleSmall)
                    Spacer(modifier = Modifier.height(8.dp))
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Text(
                            text = "name,price\n\"Product 1\",100.0\n\"Product 2\",200.0\n\"Product 3\",300.0",
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch {
                            val csvTemplate =
                                "name,price\n\"Product 1\",100.0\n\"Product 2\",200.0\n\"Product 3\",300.0"
                            // Copy to clipboard
                            val clipboard =
                                context.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                            val clip =
                                android.content.ClipData.newPlainText("CSV Template", csvTemplate)
                            clipboard.setPrimaryClip(clip)
                            snackbarHostState.showSnackbar("Template copied to clipboard")
                            showCSVTemplate = false
                        }
                    }
                ) {
                    Text("Copy Template")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCSVTemplate = false }) {
                    Text("Close")
                }
            }
        )
    }
}

@Composable
fun ProductItemCard(
    product: Product,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = product.name,
                    style = MaterialTheme.typography.bodyLarge,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Text(
                text = "₹${product.price}",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )

            IconButton(
                onClick = onDelete,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
fun EmptyProductsView(
    onImportClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            //Icons.Default.Inventory2,
            Icons.Default.AccountBox,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "No products yet",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Add your first product or import from CSV",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = onImportClick,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                contentColor = MaterialTheme.colorScheme.onSecondaryContainer
            )
        ) {
            Icon(Icons.Default.KeyboardArrowUp, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Import from CSV")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductListTopBar(
    mainViewModel: MainViewModel = hiltViewModel()
) {
    TopAppBar(
        title = { Text("Daftar Harga") },
        actions = {
            IconButton(
                onClick = { mainViewModel.emit(UiEvent.Navigate(Screen.CreateProduct)) }
            ) {
                Icon(Icons.Default.AddCircle, contentDescription = "Tambah Produk")
            }
        }
    )
}

@Composable
fun ProductListFAB(
    mainViewModel: MainViewModel = hiltViewModel()
) {
    FloatingActionButton(
        onClick = { mainViewModel.emit(UiEvent.Navigate(Screen.CreateProduct)) }
    ) {
        Icon(Icons.Default.Add, contentDescription = "Tambah Produk")
    }
}
