package org.egon12.pesanin.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "products")
data class Product(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val description: String,
    val price: Double,
    val category: String,
    val imageUrl: String? = null,
    val stock: Int = 0
)
