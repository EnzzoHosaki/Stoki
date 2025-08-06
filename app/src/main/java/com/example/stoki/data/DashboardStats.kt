package com.example.stoki.data

data class ProductTransactionInfo(
    val productId: Int,
    val productName: String,
    val totalAmount: Double,
    val totalQuantity: Int
)