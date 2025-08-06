package com.example.stoki.ui.productselection

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.stoki.R
import com.example.stoki.data.MovementType
import com.example.stoki.data.Product
import com.example.stoki.ui.addproduct.CurrencyVisualTransformation
import com.example.stoki.ui.productlist.ProductListViewModel
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductSelectionScreen(
    navController: NavController,
    viewModel: ProductListViewModel
) {
    val allProducts by viewModel.allProducts.collectAsState()
    var selectedProduct by remember { mutableStateOf<Product?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Selecionar Produto") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(allProducts) { product ->
                SelectableProductItem(
                    product = product,
                    onClick = { selectedProduct = product }
                )
            }
        }
    }

    if (selectedProduct != null) {
        TransactionItemDialog(
            product = selectedProduct!!,
            viewModel = viewModel,
            onDismiss = { selectedProduct = null },
            onConfirm = { product, quantity, price ->
                viewModel.addProductToTransaction(product, quantity, price)
                selectedProduct = null
                navController.popBackStack()
            }
        )
    }
}

@Composable
fun TransactionItemDialog(
    product: Product,
    viewModel: ProductListViewModel,
    onDismiss: () -> Unit,
    onConfirm: (Product, Int, Double) -> Unit
) {
    val transactionType by viewModel.transactionType.collectAsState()
    val originalPrice = if (transactionType == MovementType.SALE) product.salePrice else product.costPrice

    var quantity by rememberSaveable { mutableStateOf("1") }
    var price by rememberSaveable { mutableStateOf((originalPrice * 100).toLong().toString()) }
    var discount by rememberSaveable { mutableStateOf("0.00") }

    var isQuantityError by remember { mutableStateOf(false) }
    var quantityErrorMessage by remember { mutableStateOf("") }
    val quantityAsInt = quantity.toIntOrNull() ?: 0

    LaunchedEffect(discount) {
        val discountValue = discount.replace(",", ".").toDoubleOrNull() ?: 0.0
        if (discountValue in 0.0..100.0) {
            val newPrice = originalPrice * (1 - (discountValue / 100.0))
            price = (newPrice * 100).toLong().toString()
        }
    }

    LaunchedEffect(price) {
        val priceValue = (price.toDoubleOrNull() ?: 0.0) / 100.0
        if (originalPrice > 0) {
            val newDiscount = (1 - (priceValue / originalPrice)) * 100
            if (newDiscount >= 0) {
                discount = String.format(Locale.US, "%.2f", newDiscount)
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Adicionar ${product.name}") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = quantity,
                    onValueChange = {
                        val newQty = it.toIntOrNull() ?: 0
                        if (transactionType == MovementType.SALE) {
                            if (newQty > product.quantity) {
                                isQuantityError = true
                                quantityErrorMessage = "Estoque insuficiente (${product.quantity} disp.)"
                            } else {
                                isQuantityError = false
                                quantityErrorMessage = ""
                            }
                        }
                        if (it.all { char -> char.isDigit() }) quantity = it
                    },
                    label = { Text("Quantidade") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    isError = isQuantityError,
                    supportingText = { if (isQuantityError) Text(quantityErrorMessage) }
                )
                OutlinedTextField(
                    value = price,
                    onValueChange = { if (it.all { char -> char.isDigit() }) price = it },
                    label = { Text("Preço Final (Unitário)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    visualTransformation = CurrencyVisualTransformation()
                )
                OutlinedTextField(
                    value = discount,
                    onValueChange = { discount = it },
                    label = { Text("Desconto (%)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val finalPrice = (price.toDoubleOrNull() ?: 0.0) / 100.0
                    onConfirm(product, quantityAsInt, finalPrice)
                },
                enabled = quantityAsInt > 0 && !isQuantityError
            ) { Text("Confirmar") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}

@Composable
fun SelectableProductItem(product: Product, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = product.imageUrl,
                contentDescription = product.name,
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop,
                error = painterResource(id = R.drawable.ic_launcher_background),
                placeholder = painterResource(id = R.drawable.ic_launcher_background)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(text = product.name, fontWeight = FontWeight.Bold)
                Text(
                    text = "${product.quantity} em estoque",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}