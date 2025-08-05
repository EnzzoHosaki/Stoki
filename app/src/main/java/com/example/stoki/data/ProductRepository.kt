package com.example.stoki.data

import androidx.room.withTransaction
import kotlinx.coroutines.flow.Flow
import java.util.Date

// O construtor agora recebe a instância do AppDatabase
class ProductRepository(private val db: AppDatabase) {

    // Acessamos os DAOs através da instância do banco
    private val productDao = db.productDao()
    private val categoryDao = db.categoryDao()
    private val brandDao = db.brandDao()
    private val productMovementDao = db.productMovementDao()


    // Funções de Produto (agora usam o 'productDao' local)
    fun getAllProducts(query: String, category: String, brand: String): Flow<List<Product>> =
        productDao.getAllProducts(query, category, brand)

    fun getAllProducts(): Flow<List<Product>> = productDao.getAllProducts()

    suspend fun update(product: Product) { productDao.update(product) }
    suspend fun delete(product: Product) { productDao.delete(product) }
    fun getProductById(id: Int): Flow<Product> = productDao.getProductById(id)
    suspend fun insert(product: Product) { productDao.insert(product) }

    // Funções de Categoria
    fun getAllCategories(): Flow<List<Category>> = categoryDao.getAllCategories()
    suspend fun insertCategory(category: Category) { categoryDao.insert(category) }
    suspend fun deleteCategory(category: Category) { categoryDao.delete(category) }

    // Funções de Marca
    fun getAllBrands(): Flow<List<Brand>> = brandDao.getAllBrands()
    suspend fun insertBrand(brand: Brand) { brandDao.insert(brand) }
    suspend fun deleteBrand(brand: Brand) { brandDao.delete(brand) }

    // Funções de Movimentação
    fun getAllMovements(): Flow<List<ProductMovement>> = productMovementDao.getAllMovements()
    suspend fun insertMovement(movement: ProductMovement) { productMovementDao.insert(movement) }

    fun getTotalStockValue(): Flow<Double?> = productDao.getTotalStockValue()
    fun getLowStockCount(): Flow<Int> = productDao.getLowStockCount()
    fun getStockByCategory(): Flow<List<CategoryStock>> = productDao.getStockByCategory()

    // A função de transação agora funciona corretamente
    suspend fun completeTransaction(items: List<TransactionItem>, type: MovementType) {
        db.withTransaction {
            items.forEach { item ->
                val newQuantity = if (type == MovementType.SALE) {
                    item.product.quantity - item.quantity
                } else { // PURCHASE
                    item.product.quantity + item.quantity
                }

                productDao.update(item.product.copy(quantity = newQuantity))

                val movement = ProductMovement(
                    productId = item.product.id,
                    productName = item.product.name,
                    type = type,
                    quantity = item.quantity,
                    amount = item.finalPrice * item.quantity,
                    date = Date()
                )
                productMovementDao.insert(movement)
            }
        }
    }
}