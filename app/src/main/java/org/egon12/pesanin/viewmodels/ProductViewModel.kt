package org.egon12.pesanin.viewmodels

// ProductViewModel.kt
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import org.egon12.pesanin.model.Product
import org.egon12.pesanin.repository.ProductRepository
import javax.inject.Inject

@HiltViewModel
class ProductViewModel @Inject constructor(
    private val repository: ProductRepository
) : ViewModel() {

    private val _products = MutableStateFlow<List<Product>>(emptyList())
    val products: StateFlow<List<Product>> = _products.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedProduct = MutableStateFlow<Product?>(null)
    val selectedProduct: StateFlow<Product?> = _selectedProduct.asStateFlow()

    private val _uiState = MutableStateFlow<ProductUiState>(ProductUiState.Idle)
    val uiState: StateFlow<ProductUiState> = _uiState.asStateFlow()

    private val _importResult = MutableStateFlow<Pair<Int, List<String>>?>(null)
    val importResult: StateFlow<Pair<Int, List<String>>?> = _importResult.asStateFlow()

    init {
        loadProducts()
    }

    private fun loadProducts() {
        viewModelScope.launch {
            repository.getAllProducts()
                .combine(_searchQuery) { products, query ->
                    if (query.isBlank()) products else {
                        products.filter { it.name.contains(query, ignoreCase = true) }
                    }
                }
                .collect { products ->
                    _products.value = products
                }
        }
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun selectProduct(product: Product?) {
        _selectedProduct.value = product
    }

    suspend fun createProduct(shortName: String, name: String, price: String): Result<String> {
        return try {
            _uiState.value = ProductUiState.Loading

            val priceDouble = price.toDoubleOrNull()
            if (priceDouble == null || priceDouble <= 0) {
                _uiState.value = ProductUiState.Error("Please enter a valid price")
                return Result.failure(IllegalArgumentException("Invalid price"))
            }

            val result = repository.createProduct(shortName, name, priceDouble)

            if (result.isSuccess) {
                _uiState.value = ProductUiState.Success("Product added successfully")
            } else {
                _uiState.value = ProductUiState.Error(
                    result.exceptionOrNull()?.message ?: "Failed to add product"
                )
            }

            result
        } catch (e: Exception) {
            _uiState.value = ProductUiState.Error(e.message ?: "Failed to add product")
            Result.failure(e)
        }
    }

    suspend fun updateProduct(productId: String, name: String, price: String): Result<Unit> {

        return try {
            _uiState.value = ProductUiState.Loading

            val priceDouble = price.toDoubleOrNull()
            if (priceDouble == null || priceDouble <= 0) {
                _uiState.value = ProductUiState.Error("Please enter a valid price")
                throw IllegalArgumentException("Invalid price")
            }

            val product = Product(id = productId, name = name, price = priceDouble)
            val result = repository.updateProduct(product)

            if (result.isSuccess) {
                _uiState.value = ProductUiState.Success("Product updated successfully")
                selectProduct(null)
            } else {
                _uiState.value = ProductUiState.Error(
                    result.exceptionOrNull()?.message ?: "Failed to update product"
                )
            }

            result
        } catch (e: Exception) {
            _uiState.value = ProductUiState.Error(e.message ?: "Failed to update product")
            Result.failure(e)
        }
    }

    suspend fun deleteProduct(productId: String): Result<Unit> {
        return try {
            _uiState.value = ProductUiState.Loading
            val result = repository.deleteProduct(productId)

            if (result.isSuccess) {
                _uiState.value = ProductUiState.Success("Product deleted successfully")
            } else {
                _uiState.value = ProductUiState.Error(
                    result.exceptionOrNull()?.message ?: "Failed to delete product"
                )
            }

            result
        } catch (e: Exception) {
            _uiState.value = ProductUiState.Error(e.message ?: "Failed to delete product")
            Result.failure(e)
        }
    }

    suspend fun importFromCSV(csvContent: String) {
        _uiState.value = ProductUiState.Loading
        val result = repository.importFromCSV(csvContent)
        _importResult.value = result

        if (result.second.isNotEmpty()) {
            val errorText = if (result.second.size > 3) {
                "${
                    result.second.take(3).joinToString("\n")
                }\n...and ${result.second.size - 3} more errors"
            } else {
                result.second.joinToString("\n")
            }

            if (result.first > 0) {
                _uiState.value =
                    ProductUiState.Success("Imported ${result.first} products, but had ${result.second.size} errors")
            } else {
                _uiState.value = ProductUiState.Error("Import failed:\n$errorText")
            }
        } else {
            _uiState.value =
                ProductUiState.Success("Successfully imported ${result.first} products")
        }

        loadProducts()
    }

    suspend fun exportToCSV(): Result<String> {
        return try {
            _uiState.value = ProductUiState.Loading
            val csvContent = repository.exportToCSV()
            _uiState.value = ProductUiState.Success("Exported ${_products.value.size} products")
            Result.success(csvContent)
        } catch (e: Exception) {
            _uiState.value = ProductUiState.Error(e.message ?: "Failed to export")
            Result.failure(e)
        }
    }

    fun clearImportResult() {
        _importResult.value = null
    }

    fun clearSelection() {
        _selectedProduct.value = null
    }
}

sealed class ProductUiState {
    object Idle : ProductUiState()
    object Loading : ProductUiState()
    data class Success(val message: String) : ProductUiState()
    data class Error(val message: String) : ProductUiState()
}