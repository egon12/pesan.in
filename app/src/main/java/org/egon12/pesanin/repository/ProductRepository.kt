package org.egon12.pesanin.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import org.egon12.pesanin.dao.ProductDao
import org.egon12.pesanin.model.Product
import javax.inject.Inject

class ProductRepository @Inject constructor(
    private val productDao: ProductDao
) {

    suspend fun createProduct(name: String, shortName: String, price: Double): Result<String> {
        if (name.isBlank()) {
            return Result.failure(IllegalArgumentException("Product name is required"))
        }
        if (price <= 0) {
            return Result.failure(IllegalArgumentException("Price must be greater than 0"))
        }

        return try {
            val product = Product(name = name.trim(), price = price, shortName = shortName)
            productDao.insertProduct(product)
            Result.success(product.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateProduct(product: Product): Result<Unit> {
        if (product.name.isBlank()) {
            return Result.failure(IllegalArgumentException("Product name is required"))
        }
        if (product.price <= 0) {
            return Result.failure(IllegalArgumentException("Price must be greater than 0"))
        }

        return try {
            productDao.updateProduct(product)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteProduct(productId: String): Result<Unit> {
        return try {
            val product = productDao.getProductById(productId).first()
            if (product == null) {
                Result.failure(NoSuchElementException("Product not found"))
            } else {
                productDao.deleteProduct(product)
                Result.success(Unit)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getAllProducts(): Flow<List<Product>> = productDao.getAllProducts()

    fun getProductById(productId: String): Flow<Product?> = productDao.getProductById(productId)

    fun searchProducts(query: String): Flow<List<Product>> =
        productDao.searchProducts("%$query%")

    suspend fun importFromCSV(csvContent: String): Pair<Int, List<String>> {
        val lines = csvContent.trim().lines()
        if (lines.size < 2) return Pair(0, listOf("CSV must have at least header and one data row"))

        val header = lines[0].split(",").map { it.trim().lowercase() }
        val nameIndex = header.indexOf("name")
        val priceIndex = header.indexOf("price")

        if (nameIndex == -1 || priceIndex == -1) {
            return Pair(0, listOf("CSV must contain 'name' and 'price' columns"))
        }

        val errors = mutableListOf<String>()
        var successCount = 0

        for (i in 1 until lines.size) {
            val values = lines[i].split(",").map { it.trim() }
            if (values.size != header.size) {
                errors.add("Row ${i + 1}: Column count mismatch")
                continue
            }

            val name = values[nameIndex]
            val priceStr = values[priceIndex]

            if (name.isBlank()) {
                errors.add("Row ${i + 1}: Product name is required")
                continue
            }

            val price = priceStr.toDoubleOrNull()
            if (price == null || price <= 0) {
                errors.add("Row ${i + 1}: Invalid price: $priceStr")
                continue
            }

            try {
                val product = Product(name = name, price = price)
                productDao.insertProduct(product)
                successCount++
            } catch (e: Exception) {
                errors.add("Row ${i + 1}: ${e.message}")
            }
        }

        return Pair(successCount, errors)
    }

    suspend fun exportToCSV(): String {
        val products = productDao.getAllProducts().first()
        val csvLines = mutableListOf<String>()

        // Header
        csvLines.add("short_name,name,price")

        // Data
        products.forEach { product ->
            csvLines.add("\"${product.shortName}\",\"${product.name}\",${product.price}")
        }

        return csvLines.joinToString("\n")
    }
}