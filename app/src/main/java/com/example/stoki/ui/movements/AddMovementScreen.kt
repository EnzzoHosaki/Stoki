package com.example.stoki.ui.movements

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.stoki.data.MovementType
import com.example.stoki.data.TransactionItem
import com.example.stoki.ui.productlist.ProductListViewModel
import coil.compose.AsyncImage
import com.example.stoki.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddMovementScreen(
    navController: NavController,
    viewModel: ProductListViewModel
) {
    val transactionItems by viewModel.transactionItems.collectAsState()
    val transactionType by viewModel.transactionType.collectAsState()

    val totalValue = remember(transactionItems, transactionType) {
        transactionItems.sumOf { item ->
            val price = if (transactionType == MovementType.SALE) item.product.salePrice else item.product.costPrice
            price * item.quantity
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Nova Transação") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.Close, contentDescription = "Fechar")
                    }
                }
            )
        },
        bottomBar = {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Total (${transactionItems.size} itens)", style = MaterialTheme.typography.bodyLarge)
                    Text("R$ ${"%.2f".format(totalValue)}", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = {
                        viewModel.completeTransaction {
                            navController.popBackStack()
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    enabled = transactionItems.isNotEmpty()
                ) {
                    Text("Concluir")
                }
            }
        }
    ) { paddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues).padding(horizontal = 16.dp)) {
            SegmentedButton(
                selectedType = transactionType,
                onTypeSelected = { viewModel.setTransactionType(it) }
            )

            Text("Itens", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(vertical = 16.dp))

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(transactionItems) { item ->
                    TransactionItemRow(item = item)
                }
                item {
                    TextButton(onClick = { navController.navigate("productSelection") }) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Adicionar Produto")
                    }
                }
            }
        }
    }
}

@Composable
fun TransactionItemRow(item: TransactionItem) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        AsyncImage(
            model = item.product.imageUrl,
            contentDescription = item.product.name,
            modifier = Modifier.size(56.dp).clip(RoundedCornerShape(8.dp)),
            contentScale = ContentScale.Crop,
            error = painterResource(id = R.drawable.ic_launcher_background),
            placeholder = painterResource(id = R.drawable.ic_launcher_background)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(item.product.name, fontWeight = FontWeight.Bold)
            Text(
                "${item.product.quantity} em estoque",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Text("${item.quantity} Unidades", fontSize = 16.sp)
    }
}

@Composable
fun SegmentedButton(
    selectedType: MovementType,
    onTypeSelected: (MovementType) -> Unit
) {
    Card(shape = RoundedCornerShape(50)) {
        Row(modifier = Modifier.fillMaxWidth()) {
            Button(
                onClick = { onTypeSelected(MovementType.SALE) },
                shape = RoundedCornerShape(0),
                colors = if (selectedType == MovementType.SALE) ButtonDefaults.buttonColors() else ButtonDefaults.outlinedButtonColors(),
                modifier = Modifier.weight(1f)
            ) { Text("Venda") }
            Button(
                onClick = { onTypeSelected(MovementType.PURCHASE) },
                shape = RoundedCornerShape(0),
                colors = if (selectedType == MovementType.PURCHASE) ButtonDefaults.buttonColors() else ButtonDefaults.outlinedButtonColors(),
                modifier = Modifier.weight(1f)
            ) { Text("Compra") }
        }
    }
}