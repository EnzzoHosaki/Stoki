package com.example.stoki.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductMovementDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(movement: ProductMovement)

    @Query("SELECT * FROM movements ORDER BY date DESC")
    fun getAllMovements(): Flow<List<ProductMovement>>

    @Query("SELECT SUM(amount) FROM movements WHERE type = 'SALE'")
    fun getTotalRevenue(): Flow<Double?>

    @Query("""
        SELECT productId, productName, SUM(amount) as totalAmount, SUM(quantity) as totalQuantity
        FROM movements
        WHERE type = 'SALE'
        GROUP BY productId, productName
        ORDER BY totalQuantity DESC
        LIMIT 1
    """)
    fun getTopSellingProductByQuantity(): Flow<ProductTransactionInfo?>

    @Query("""
        SELECT productId, productName, SUM(amount) as totalAmount, SUM(quantity) as totalQuantity
        FROM movements
        WHERE type = 'SALE'
        GROUP BY productId, productName
        ORDER BY totalAmount DESC
        LIMIT 1
    """)
    fun getTopProductByRevenue(): Flow<ProductTransactionInfo?>
}