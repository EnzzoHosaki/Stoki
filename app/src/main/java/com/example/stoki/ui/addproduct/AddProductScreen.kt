package com.example.stoki.ui.addproduct

import android.Manifest
import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.stoki.data.Product
import com.example.stoki.ui.productlist.ProductListViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.io.File
import androidx.compose.runtime.saveable.rememberSaveable

fun createImageUri(context: Context): Uri {
    val imageFile = File(context.cacheDir, "images/product_image_${System.currentTimeMillis()}.jpg")
    imageFile.parentFile?.mkdirs()
    return FileProvider.getUriForFile(
        context,
        "${context.packageName}.provider",
        imageFile
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun AddProductScreen(
    navController: NavController,
    viewModel: ProductListViewModel,
    barcodeResult: String?,
    productId: Int?
) {
    val isEditMode = productId != null && productId != -1
    var originalProduct by rememberSaveable { mutableStateOf<Product?>(null) }

    // Declarando todos os estados
    var productCode by rememberSaveable { mutableStateOf("") }
    var name by rememberSaveable { mutableStateOf("") }
    var sku by rememberSaveable { mutableStateOf("") }
    var observation by rememberSaveable { mutableStateOf("") }
    var quantity by rememberSaveable { mutableStateOf("") }
    var costPrice by rememberSaveable { mutableStateOf("") }
    var salePrice by rememberSaveable { mutableStateOf("") }
    var selectedImageUrl by rememberSaveable { mutableStateOf<Uri?>(null) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    val categories by viewModel.categories.collectAsState()
    val brands by viewModel.brands.collectAsState()
    var selectedCategory by rememberSaveable { mutableStateOf("") }
    var isCategoryExpanded by rememberSaveable { mutableStateOf(false) }
    var selectedBrand by rememberSaveable { mutableStateOf("") }
    var isBrandExpanded by rememberSaveable { mutableStateOf(false) }
    var showAddCategoryDialog by rememberSaveable { mutableStateOf(false) }
    var showAddBrandDialog by rememberSaveable { mutableStateOf(false) }

    // Validação dos campos obrigatórios
    val isFormValid by remember(name, selectedCategory, selectedBrand) { // <-- Correção
        derivedStateOf { name.isNotBlank() && selectedCategory.isNotBlank() && selectedBrand.isNotBlank() }
    }
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri -> selectedImageUrl = uri }
    )
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture(),
        onResult = { success ->
            if (success) {
                val currentUri = selectedImageUrl
                selectedImageUrl = Uri.parse(currentUri.toString() + "?v=" + System.currentTimeMillis())
            } else {
                selectedImageUrl = null
            }
        }
    )
    val cameraPermissionState = rememberPermissionState(permission = Manifest.permission.CAMERA)

    LaunchedEffect(barcodeResult) {
        if (barcodeResult != null) {
            sku = barcodeResult
            navController.currentBackStackEntry?.savedStateHandle?.remove<String?>("barcode_result")
        }
    }

    LaunchedEffect(productId) {
        if (isEditMode && productId != null) {
            val product = viewModel.getProductById(productId).filterNotNull().first()
            originalProduct = product
            productCode = product.productCode
            name = product.name
            sku = product.sku ?: ""
            observation = product.observation ?: ""
            selectedCategory = product.category
            selectedBrand = product.brand
            quantity = product.quantity.toString()
            costPrice = (product.costPrice * 100).toLong().toString()
            salePrice = (product.salePrice * 100).toLong().toString()
            selectedImageUrl = product.imageUrl?.toUri()
        } else {
            productCode = "STK-${(1000..9999).random()}-${(100..999).random()}"
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isEditMode) "Editar Produto" else "Adicionar Produto") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        bottomBar = {
            Button(
                onClick = {
                    val productToSave = Product(
                        id = if (isEditMode) originalProduct!!.id else 0,
                        productCode = productCode, // Usa o código do estado
                        name = name,
                        sku = sku.ifEmpty { null },
                        category = selectedCategory,
                        brand = selectedBrand,
                        imageUrl = selectedImageUrl?.toString(),
                        observation = observation.ifEmpty { null },
                        quantity = quantity.toIntOrNull() ?: 0,
                        costPrice = (costPrice.toLongOrNull() ?: 0L) / 100.0,
                        salePrice = (salePrice.toLongOrNull() ?: 0L) / 100.0,
                        description = null
                    )
                    scope.launch {
                        if (isEditMode) viewModel.update(productToSave)
                        else viewModel.insert(productToSave)
                    }
                    navController.popBackStack()
                },
                enabled = isFormValid,
                modifier = Modifier.fillMaxWidth().padding(16.dp).height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                shape = RoundedCornerShape(12.dp)
            ) { Text("Concluir") }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(paddingValues).padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            contentPadding = PaddingValues(vertical = 8.dp)
        ) {
            item {
                Text("Código do Produto", style = MaterialTheme.typography.titleMedium, modifier = Modifier.fillMaxWidth())
                Text(productCode, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(16.dp))
            }

            item {
                Text("Foto do Produto", style = MaterialTheme.typography.titleMedium, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Box(modifier = Modifier.size(120.dp).clip(RoundedCornerShape(12.dp)).border(1.dp, Color.Gray, RoundedCornerShape(12.dp)), contentAlignment = Alignment.Center) {
                        if (selectedImageUrl != null) {
                            AsyncImage(model = selectedImageUrl, contentDescription = "Foto do Produto", modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                        } else {
                            Icon(imageVector = Icons.Default.PhotoCamera, contentDescription = "Adicionar Foto", modifier = Modifier.size(48.dp), tint = Color.Gray)
                        }
                    }
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(onClick = { val newUri = createImageUri(context); selectedImageUrl = newUri; cameraLauncher.launch(newUri) }, modifier = Modifier.fillMaxWidth()) { Text("Tirar Foto") }
                        OutlinedButton(onClick = { photoPickerLauncher.launch("image/*") }, modifier = Modifier.fillMaxWidth()) { Text("Galeria") }
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }

            item {
                Text("Nome do Produto *", style = MaterialTheme.typography.titleMedium, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = name, onValueChange = { name = it }, placeholder = { Text("Ex.: Camiseta") }, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(16.dp))
            }

            item {
                Text("Código de Barras", style = MaterialTheme.typography.titleMedium, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = sku, onValueChange = { sku = it }, placeholder = { Text("Ex.: 154986354563") }, modifier = Modifier.fillMaxWidth(), trailingIcon = { IconButton(onClick = { if (cameraPermissionState.status.isGranted) { navController.navigate("barcodeScanner") } else { cameraPermissionState.launchPermissionRequest() } }) { Icon(Icons.Default.QrCodeScanner, contentDescription = "Escanear Código") } })
                Spacer(modifier = Modifier.height(16.dp))
            }

            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Categoria *", style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                    IconButton(onClick = { showAddCategoryDialog = true }) { Icon(Icons.Default.Add, contentDescription = "Adicionar Categoria") }
                }
                ExposedDropdownMenuBox(expanded = isCategoryExpanded, onExpandedChange = { isCategoryExpanded = it }) {
                    OutlinedTextField(value = selectedCategory, onValueChange = {}, readOnly = true, placeholder = { Text("Selecione uma categoria") }, trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isCategoryExpanded) }, modifier = Modifier.fillMaxWidth().menuAnchor())
                    ExposedDropdownMenu(expanded = isCategoryExpanded, onDismissRequest = { isCategoryExpanded = false }) {
                        categories.forEach { category ->
                            DropdownMenuItem(text = { Text(category.name) }, onClick = { selectedCategory = category.name; isCategoryExpanded = false })
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Marca *", style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                    IconButton(onClick = { showAddBrandDialog = true }) { Icon(Icons.Default.Add, contentDescription = "Adicionar Marca") }
                }
                ExposedDropdownMenuBox(expanded = isBrandExpanded, onExpandedChange = { isBrandExpanded = it }) {
                    OutlinedTextField(value = selectedBrand, onValueChange = {}, readOnly = true, placeholder = { Text("Selecione uma marca") }, trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isBrandExpanded) }, modifier = Modifier.fillMaxWidth().menuAnchor())
                    ExposedDropdownMenu(expanded = isBrandExpanded, onDismissRequest = { isBrandExpanded = false }) {
                        brands.forEach { brand ->
                            DropdownMenuItem(text = { Text(brand.name) }, onClick = { selectedBrand = brand.name; isBrandExpanded = false })
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            item {
                Text("Observação", style = MaterialTheme.typography.titleMedium, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = observation, onValueChange = { observation = it }, placeholder = { Text("Ex.: Produto frágil, armazenar com cuidado.") }, modifier = Modifier.fillMaxWidth().height(120.dp))
                Spacer(modifier = Modifier.height(16.dp))
            }

            item {
                Text("Quantidade", style = MaterialTheme.typography.titleMedium, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = quantity, onValueChange = { newValue -> if (newValue.length <= 9 && newValue.all { it.isDigit() }) { quantity = newValue } }, placeholder = { Text("Ex.: 500") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(16.dp))
            }

            item {
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Column(Modifier.weight(1f)) {
                        Text("Preço de Custo", style = MaterialTheme.typography.titleMedium)
                        OutlinedTextField(value = costPrice, onValueChange = { newValue -> if (newValue.length <= 12 && newValue.all { it.isDigit() }) { costPrice = newValue } }, placeholder = { Text("R$ 0,00") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), visualTransformation = CurrencyVisualTransformation(), modifier = Modifier.fillMaxWidth())
                    }
                    Column(Modifier.weight(1f)) {
                        Text("Preço de Venda", style = MaterialTheme.typography.titleMedium)
                        OutlinedTextField(value = salePrice, onValueChange = { newValue -> if (newValue.length <= 12 && newValue.all { it.isDigit() }) { salePrice = newValue } }, placeholder = { Text("R$ 0,00") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), visualTransformation = CurrencyVisualTransformation(), modifier = Modifier.fillMaxWidth())
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
    // DIÁLOGO PARA ADICIONAR NOVA CATEGORIA
    if (showAddCategoryDialog) {
        var newCategoryName by rememberSaveable { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showAddCategoryDialog = false },
            title = { Text("Adicionar Nova Categoria") },
            text = {
                OutlinedTextField(
                    value = newCategoryName,
                    onValueChange = { newCategoryName = it },
                    label = { Text("Nome da Categoria") }
                )
            },
            confirmButton = {
                Button(onClick = {
                    viewModel.insertCategory(newCategoryName)
                    showAddCategoryDialog = false
                }) { Text("Adicionar") }
            },
            dismissButton = {
                TextButton(onClick = { showAddCategoryDialog = false }) { Text("Cancelar") }
            }
        )
    }

    // DIÁLOGO PARA ADICIONAR NOVA MARCA
    if (showAddBrandDialog) {
        var newBrandName by rememberSaveable { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showAddBrandDialog = false },
            title = { Text("Adicionar Nova Marca") },
            text = {
                OutlinedTextField(
                    value = newBrandName,
                    onValueChange = { newBrandName = it },
                    label = { Text("Nome da Marca") }
                )
            },
            confirmButton = {
                Button(onClick = {
                    viewModel.insertBrand(newBrandName)
                    showAddBrandDialog = false
                }) { Text("Adicionar") }
            },
            dismissButton = {
                TextButton(onClick = { showAddBrandDialog = false }) { Text("Cancelar") }
            }
        )
    }
}