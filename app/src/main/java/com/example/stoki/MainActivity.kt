package com.example.stoki

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import coil.compose.AsyncImage
import com.example.stoki.data.AppDatabase
import com.example.stoki.data.Product
import com.example.stoki.data.ProductRepository
import com.example.stoki.ui.NavigationItem
import com.example.stoki.ui.addproduct.AddProductScreen
import com.example.stoki.ui.barcodescanner.BarcodeScannerScreen
import com.example.stoki.ui.home.HomeScreen
import com.example.stoki.ui.movements.MovementScreen
import com.example.stoki.ui.productdetail.ProductDetailScreen
import com.example.stoki.ui.productlist.ProductListViewModel
import com.example.stoki.ui.productlist.ProductListViewModelFactory
import com.example.stoki.ui.settings.SettingsScreen
import com.example.stoki.ui.theme.StokiTheme
import com.example.stoki.data.preferences.SettingsManager
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.foundation.isSystemInDarkTheme
import kotlinx.coroutines.launch
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import com.example.stoki.ui.movements.AddMovementScreen
import com.example.stoki.ui.productselection.ProductSelectionScreen
import com.example.stoki.ui.home.HomeViewModel
import com.example.stoki.ui.home.HomeViewModelFactory

class MainActivity : ComponentActivity() {
    private val database by lazy { AppDatabase.getDatabase(this) }
    private val repository by lazy { ProductRepository(database) }
    //private val viewModel: ProductListViewModel by viewModels { ProductListViewModelFactory(repository) }

    private val settingsManager by lazy { SettingsManager(this) }

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val isDarkTheme by settingsManager.themeFlow.collectAsState(
                initial = isSystemInDarkTheme()
            )

            val scope = rememberCoroutineScope()

            StokiTheme(darkTheme = isDarkTheme ?: isSystemInDarkTheme()) {
                val navController = rememberNavController()
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination

                val mainScreenRoutes = NavigationItem.entries.map { it.route }
                val shouldShowBars = currentDestination?.route in mainScreenRoutes

                val currentScreenTitle = if (shouldShowBars) {
                    NavigationItem.entries.find { it.route == currentDestination?.route }?.title ?: "Stoki"
                } else {
                    ""
                }

                Scaffold(
                    topBar = {
                        if (shouldShowBars) {
                            CenterAlignedTopAppBar(
                                title = { Text(currentScreenTitle) },
                                colors = TopAppBarDefaults.topAppBarColors(
                                    containerColor = MaterialTheme.colorScheme.background
                                )
                            )
                        }
                    },
                    bottomBar = {
                        if (shouldShowBars) {
                            NavigationBar {
                                val navigationItems = listOf(
                                    NavigationItem.Home,
                                    NavigationItem.Stock,
                                    NavigationItem.Movements,
                                    NavigationItem.Settings
                                )
                                navigationItems.forEach { item ->
                                    val isSelected = currentDestination?.hierarchy?.any { it.route == item.route } == true
                                    NavigationBarItem(
                                        selected = isSelected,
                                        onClick = { navigateTo(navController, item.route) },
                                        icon = { Icon(item.icon, contentDescription = item.title) },
                                        alwaysShowLabel = false
                                    )
                                }
                            }
                        }
                    },
                    floatingActionButton = {
                        if (currentDestination?.route == NavigationItem.Stock.route) {
                            FloatingActionButton(
                                onClick = { navController.navigate("addProduct?productId=-1") },
                                shape = CircleShape
                            ) {
                                Icon(Icons.Filled.Add, "Adicionar Produto")
                            }
                        }
                    }
                ) { innerPadding ->
                    NavHost(
                        navController,
                        startDestination = NavigationItem.Home.route,
                        Modifier.padding(innerPadding)
                    ) {
                        composable(NavigationItem.Home.route) {
                            val homeViewModel: HomeViewModel by viewModels { HomeViewModelFactory(repository) }
                            HomeScreen(viewModel = homeViewModel)
                        }

                        composable(NavigationItem.Stock.route) {
                            val productListViewModel: ProductListViewModel by viewModels {
                                ProductListViewModelFactory(
                                    repository
                                )
                            }
                            ProductListScreen(navController, productListViewModel)
                        }

                        composable(NavigationItem.Movements.route) {
                            val productListViewModel: ProductListViewModel by viewModels {
                                ProductListViewModelFactory(
                                    repository
                                )
                            }
                            MovementScreen(navController, productListViewModel)
                        }

                        composable(NavigationItem.Settings.route) {
                            val productListViewModel: ProductListViewModel by viewModels {
                                ProductListViewModelFactory(
                                    repository
                                )
                            }
                            SettingsScreen(
                                isDarkTheme = isDarkTheme ?: isSystemInDarkTheme(),
                                onThemeChange = { isChecked ->
                                    scope.launch {
                                        settingsManager.setTheme(isChecked)
                                    }
                                },
                                viewModel = productListViewModel
                            )
                        }

                        composable(
                            route = "productDetail/{productId}",
                            arguments = listOf(navArgument("productId") { type = NavType.IntType })
                        ) { backStackEntry ->
                            val productListViewModel: ProductListViewModel by viewModels {
                                ProductListViewModelFactory(
                                    repository
                                )
                            }
                            val productId = backStackEntry.arguments?.getInt("productId") ?: 0
                            ProductDetailScreen(productId, navController, productListViewModel)
                        }
                        composable(
                            route = "addProduct?productId={productId}",
                            arguments = listOf(navArgument("productId") {
                                type = NavType.IntType; defaultValue = -1
                            })
                        ) { backStackEntry ->
                            val productListViewModel: ProductListViewModel by viewModels {
                                ProductListViewModelFactory(
                                    repository
                                )
                            }
                            val productId = backStackEntry.arguments?.getInt("productId")
                            val barcodeResult =
                                navController.currentBackStackEntry?.savedStateHandle?.getStateFlow<String?>(
                                    "barcode_result",
                                    null
                                )?.collectAsState()
                            AddProductScreen(
                                navController,
                                productListViewModel,
                                barcodeResult?.value,
                                productId
                            )
                        }
                        composable("barcodeScanner") {
                            BarcodeScannerScreen(navController)
                        }
                        composable("addMovement") {
                            val productListViewModel: ProductListViewModel by viewModels {
                                ProductListViewModelFactory(
                                    repository
                                )
                            }
                            AddMovementScreen(navController, productListViewModel)
                        }
                        composable("productSelection") {
                            val productListViewModel: ProductListViewModel by viewModels {
                                ProductListViewModelFactory(
                                    repository
                                )
                            }
                            ProductSelectionScreen(navController, productListViewModel)
                        }
                    }
                }
            }
        }
    }
}

fun navigateTo(navController: NavController, route: String) {
    navController.navigate(route) {
        popUpTo(navController.graph.findStartDestination().id) {
            saveState = true
        }
        launchSingleTop = true
        restoreState = true
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductListScreen(navController: NavController, viewModel: ProductListViewModel) {
    val products by viewModel.products.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val brands by viewModel.brands.collectAsState()
    val selectedCategory by viewModel.categoryFilter.collectAsState()
    val selectedBrand by viewModel.brandFilter.collectAsState()

    var isCategoryFilterExpanded by remember { mutableStateOf(false) }
    var isBrandFilterExpanded by remember { mutableStateOf(false) }

    Column {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { viewModel.onSearchQueryChange(it) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            placeholder = { Text("Procurar produto...") },
            singleLine = true
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ExposedDropdownMenuBox(
                expanded = isCategoryFilterExpanded,
                onExpandedChange = { isCategoryFilterExpanded = it },
                modifier = Modifier.weight(1f)
            ) {
                OutlinedTextField(
                    value = if (selectedCategory.isEmpty()) "Todas Cat." else selectedCategory,
                    onValueChange = {},
                    readOnly = true,
                    textStyle = MaterialTheme.typography.bodyMedium,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isCategoryFilterExpanded) },
                    modifier = Modifier.menuAnchor()
                )
                ExposedDropdownMenu(
                    expanded = isCategoryFilterExpanded,
                    onDismissRequest = { isCategoryFilterExpanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Todas Cat.") },
                        onClick = {
                            viewModel.onCategoryFilterChange("")
                            isCategoryFilterExpanded = false
                        }
                    )
                    categories.forEach { category ->
                        DropdownMenuItem(
                            text = { Text(category.name) },
                            onClick = {
                                viewModel.onCategoryFilterChange(category.name)
                                isCategoryFilterExpanded = false
                            }
                        )
                    }
                }
            }

            ExposedDropdownMenuBox(
                expanded = isBrandFilterExpanded,
                onExpandedChange = { isBrandFilterExpanded = it },
                modifier = Modifier.weight(1f)
            ) {
                OutlinedTextField(
                    value = if (selectedBrand.isEmpty()) "Todas Marcas" else selectedBrand,
                    onValueChange = {},
                    readOnly = true,
                    textStyle = MaterialTheme.typography.bodyMedium,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isBrandFilterExpanded) },
                    modifier = Modifier.menuAnchor()
                )
                ExposedDropdownMenu(
                    expanded = isBrandFilterExpanded,
                    onDismissRequest = { isBrandFilterExpanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Todas Marcas") },
                        onClick = {
                            viewModel.onBrandFilterChange("")
                            isBrandFilterExpanded = false
                        }
                    )
                    brands.forEach { brand ->
                        DropdownMenuItem(
                            text = { Text(brand.name) },
                            onClick = {
                                viewModel.onBrandFilterChange(brand.name)
                                isBrandFilterExpanded = false
                            }
                        )
                    }
                }
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(top = 8.dp, bottom = 16.dp)
        ) {
            items(products) { product ->
                ProductListItem(product = product, navController = navController)
            }
        }
    }
}

@Composable
fun ProductListItem(product: Product, navController: NavController) {
    Card(
        modifier = Modifier.clickable {
            navController.navigate("productDetail/${product.id}")
        },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = product.imageUrl,
                contentDescription = product.name,
                modifier = Modifier.size(64.dp).clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop,
                error = painterResource(id = R.drawable.ic_launcher_background),
                placeholder = painterResource(id = R.drawable.ic_launcher_background)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = product.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${product.quantity} em estoque - R$${"%.2f".format(product.salePrice)}",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }
        }
    }
}