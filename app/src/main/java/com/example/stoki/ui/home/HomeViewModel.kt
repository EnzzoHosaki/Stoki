package com.example.stoki.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.stoki.data.ProductRepository
import kotlinx.coroutines.flow.*
import com.example.stoki.data.CategoryStock

class HomeViewModel(private val repository: ProductRepository) : ViewModel() {

    val totalStockValue: StateFlow<Double> = repository.getTotalStockValue()
        .map { it ?: 0.0 }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val lowStockCount: StateFlow<Int> = repository.getLowStockCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val totalRevenue = repository.getTotalRevenue()
        .map { it ?: 0.0 }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val topSellingProduct = repository.getTopSellingProductByQuantity()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val topRevenueProduct = repository.getTopProductByRevenue()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val topSellingProductsChartData: StateFlow<List<Pair<String, Int>>> = topSellingProduct
        .map {
            if (it != null) {
                listOf(it.productName to it.totalQuantity)
            } else {
                emptyList()
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val topRevenueProductsChartData: StateFlow<List<Pair<String, Double>>> = topRevenueProduct
        .map {
            if (it != null) {
                listOf(it.productName to it.totalAmount)
            } else {
                emptyList()
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val stockByCategory: StateFlow<List<CategoryStock>> = repository.getStockByCategory()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

}

class HomeViewModelFactory(private val repository: ProductRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return HomeViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}