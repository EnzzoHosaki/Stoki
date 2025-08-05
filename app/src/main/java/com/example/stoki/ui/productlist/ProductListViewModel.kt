package com.example.stoki.ui.productlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.stoki.data.Product
import com.example.stoki.data.ProductRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.MutableStateFlow
import com.example.stoki.data.Brand
import com.example.stoki.data.Category
import com.example.stoki.data.ProductMovement
import kotlinx.coroutines.flow.combine
import com.example.stoki.data.MovementType
import com.example.stoki.data.TransactionItem
import kotlinx.coroutines.flow.map
import com.example.stoki.data.CategoryStock

@OptIn(ExperimentalCoroutinesApi::class)
class ProductListViewModel(private val repository: ProductRepository) : ViewModel() {

    // Estados para os filtros
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _categoryFilter = MutableStateFlow("") // Guardará o nome da categoria selecionada
    val categoryFilter: StateFlow<String> = _categoryFilter.asStateFlow()

    private val _brandFilter = MutableStateFlow("") // Guardará o nome da marca selecionada
    val brandFilter: StateFlow<String> = _brandFilter.asStateFlow()

    // Buscando as listas de categorias e marcas do banco
    val categories: StateFlow<List<Category>> = repository.getAllCategories()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val brands: StateFlow<List<Brand>> = repository.getAllBrands()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val movements: StateFlow<List<ProductMovement>> = repository.getAllMovements()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // O fluxo de produtos agora combina os 3 filtros!
    val products: StateFlow<List<Product>> = combine(
        _searchQuery.debounce(300),
        _categoryFilter,
        _brandFilter
    ) { query, category, brand ->
        Triple(query, category, brand) // Agrupa os 3 valores
    }.flatMapLatest { (query, category, brand) ->
        // Sempre que um dos 3 filtros mudar, esta busca é refeita
        repository.getAllProducts(query, category, brand)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000L),
        initialValue = emptyList()
    )

    // Funções para a UI atualizar os filtros
    fun onSearchQueryChange(newQuery: String) { _searchQuery.value = newQuery }
    fun onCategoryFilterChange(newCategory: String) { _categoryFilter.value = newCategory }
    fun onBrandFilterChange(newBrand: String) { _brandFilter.value = newBrand }

    // Funções para adicionar novas categorias e marcas
    fun insertCategory(name: String) = viewModelScope.launch {
        if (name.isNotBlank()) repository.insertCategory(Category(name = name))
    }
    fun deleteCategory(category: Category) = viewModelScope.launch { repository.deleteCategory(category) }
    fun insertBrand(name: String) = viewModelScope.launch {
        if (name.isNotBlank()) repository.insertBrand(Brand(name = name))
    }
    fun deleteBrand(brand: Brand) = viewModelScope.launch { repository.deleteBrand(brand) }
    fun getProductById(id: Int): StateFlow<Product?> {
        return repository.getProductById(id)
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000L),
                initialValue = null
            )
    }
    fun update(product: Product) = viewModelScope.launch { repository.update(product) }
    fun delete(product: Product) = viewModelScope.launch { repository.delete(product) }
    fun insert(product: Product) = viewModelScope.launch { repository.insert(product) }

    private val _transactionItems = MutableStateFlow<List<TransactionItem>>(emptyList())
    val transactionItems: StateFlow<List<TransactionItem>> = _transactionItems.asStateFlow()

    private val _transactionType = MutableStateFlow(MovementType.SALE)
    val transactionType: StateFlow<MovementType> = _transactionType.asStateFlow()

    fun setTransactionType(type: MovementType) {
        _transactionType.value = type
    }

    fun addProductToTransaction(product: Product, quantity: Int, price: Double) {
        val currentList = _transactionItems.value
        val existingItem = currentList.find { it.product.id == product.id }

        val newList = if (existingItem != null) {
            // Se o item já existe, criamos uma nova lista mapeando sobre a antiga
            currentList.map {
                if (it.product.id == product.id) {
                    // Criamos uma cópia do item com a quantidade atualizada
                    it.copy(quantity = it.quantity + quantity)
                } else {
                    it // Mantemos os outros itens como estão
                }
            }
        } else {
            // Se é um item novo, criamos uma nova lista adicionando-o
            currentList + TransactionItem(product, quantity, price)
        }
        _transactionItems.value = newList
    }

    fun clearTransaction() {
        _transactionItems.value = emptyList()
        _transactionType.value = MovementType.SALE
    }

    fun completeTransaction(onComplete: () -> Unit) = viewModelScope.launch {
        repository.completeTransaction(_transactionItems.value, _transactionType.value)
        clearTransaction()
        onComplete()
    }

    val allProducts: StateFlow<List<Product>> = repository.getAllProducts()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = emptyList()
        )

    val totalStockValue: StateFlow<Double> = repository.getTotalStockValue()
        .map { it ?: 0.0 } // Converte nulo para 0.0
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val lowStockCount: StateFlow<Int> = repository.getLowStockCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val stockByCategory: StateFlow<List<CategoryStock>> = repository.getStockByCategory()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
}

class ProductListViewModelFactory(private val repository: ProductRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ProductListViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ProductListViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}