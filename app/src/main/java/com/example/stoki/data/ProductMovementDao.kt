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
}