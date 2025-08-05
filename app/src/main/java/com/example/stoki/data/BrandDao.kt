package com.example.stoki.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface BrandDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(brand: Brand)

    @Delete
    suspend fun delete(brand: Brand)

    @Query("SELECT * FROM brands ORDER BY name ASC")
    fun getAllBrands(): Flow<List<Brand>>
}