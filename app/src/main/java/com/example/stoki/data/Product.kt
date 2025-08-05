package com.example.stoki.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "products",
    indices = [Index(value = ["name", "category", "brand"], unique = true)]
)
data class Product(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val productCode: String,
    val name: String,
    val description: String?,
    val sku: String?,
    val category: String,
    val brand: String,
    val imageUrl: String?,
    val observation: String?,
    val quantity: Int,
    val costPrice: Double,
    val salePrice: Double
)