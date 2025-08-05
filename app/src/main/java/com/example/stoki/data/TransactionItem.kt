package com.example.stoki.data

data class TransactionItem(
    val product: Product,
    var quantity: Int,
    val finalPrice: Double
)