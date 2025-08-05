package com.example.stoki.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

enum class MovementType {
    PURCHASE, SALE
}

@Entity(tableName = "movements")
data class ProductMovement(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val productId: Int,
    val productName: String,
    val type: MovementType,
    val quantity: Int,
    val amount: Double,
    val date: Date
)