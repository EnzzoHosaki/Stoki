package com.example.stoki.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(product: Product)

    @Update
    suspend fun update(product: Product)

    @Delete
    suspend fun delete(product: Product)

    @Query("""
        SELECT * FROM products
        WHERE
            (:query = '' OR name LIKE '%' || :query || '%') AND
            (:categoryFilter = '' OR category = :categoryFilter) AND
            (:brandFilter = '' OR brand = :brandFilter)
        ORDER BY name ASC
    """)
    fun getAllProducts(query: String, categoryFilter: String, brandFilter: String): Flow<List<Product>>

    @Query("SELECT * FROM products WHERE id = :id")
    fun getProductById(id: Int): Flow<Product>

    @Query("SELECT * FROM products ORDER BY name ASC")
    fun getAllProducts(): Flow<List<Product>>

    @Query("SELECT SUM(costPrice * quantity) FROM products")
    fun getTotalStockValue(): Flow<Double?>

    @Query("SELECT COUNT(*) FROM products WHERE quantity < :lowStockThreshold")
    fun getLowStockCount(lowStockThreshold: Int = 5): Flow<Int>

    @Query("SELECT category, SUM(quantity) as totalQuantity FROM products GROUP BY category ORDER BY totalQuantity DESC")
    fun getStockByCategory(): Flow<List<CategoryStock>>
}