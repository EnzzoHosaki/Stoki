package com.example.stoki.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

sealed interface NavigationItem {
    val route: String
    val icon: ImageVector
    val title: String

    data object Home : NavigationItem {
        override val route = "home"
        override val icon = Icons.Default.Home
        override val title = "Início"
    }
    data object Stock : NavigationItem {
        override val route = "stock"
        override val icon = Icons.Default.Inventory2
        override val title = "Estoque"
    }
    data object Movements : NavigationItem {
        override val route = "movements"
        override val icon = Icons.Default.SwapHoriz
        override val title = "Movimentação"
    }
    data object Settings : NavigationItem {
        override val route = "settings"
        override val icon = Icons.Default.Settings
        override val title = "Configurações"
    }

    companion object {
        val entries = listOf(Home, Stock, Movements, Settings)
    }
}