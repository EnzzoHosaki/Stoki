package com.example.stoki.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.stoki.data.CategoryStock
import com.example.stoki.ui.productlist.ProductListViewModel
import com.example.stoki.ui.home.HomeViewModel

@Composable
fun HomeScreen(viewModel: HomeViewModel) {
    val totalStockValue by viewModel.totalStockValue.collectAsState()
    val lowStockCount by viewModel.lowStockCount.collectAsState()
    val stockByCategory by viewModel.stockByCategory.collectAsState()
    val totalRevenue by viewModel.totalRevenue.collectAsState()
    val topSellingProduct by viewModel.topSellingProduct.collectAsState()
    val topRevenueProduct by viewModel.topRevenueProduct.collectAsState()

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                StatCard(
                    title = "Valor do Estoque",
                    value = "R$ ${"%.2f".format(totalStockValue)}",
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "Estoque Baixo",
                    value = lowStockCount.toString(),
                    modifier = Modifier.weight(1f)
                )
            }
        }

        if (stockByCategory.isNotEmpty()) {
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            "Estoque por Categoria",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        SimpleBarChart(
                            data = stockByCategory,
                            modifier = Modifier.fillMaxWidth().height(200.dp)
                        )
                    }
                }
            }
        }

        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Resumo Financeiro", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    InfoRow("Receita Total de Vendas:", "R$ ${"%.2f".format(totalRevenue)}")
                }
            }
        }

        if (topSellingProduct != null || topRevenueProduct != null) {
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Destaques de Vendas", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

                        if (topSellingProduct?.productId == topRevenueProduct?.productId && topSellingProduct != null) {
                            InfoRow("Produto Destaque:", topSellingProduct!!.productName)
                            InfoRow("Total Vendido:", "${topSellingProduct!!.totalQuantity} un")
                            InfoRow("Receita Gerada:", "R$ ${"%.2f".format(topRevenueProduct!!.totalAmount)}")
                        } else {
                            topSellingProduct?.let {
                                InfoRow("Mais Vendido (Unidades):", "${it.productName} (${it.totalQuantity} un)")
                            }
                            topRevenueProduct?.let {
                                InfoRow("Maior Receita:", "${it.productName} (R$ ${"%.2f".format(it.totalAmount)})")
                            }
                        }
                    }
                }
            }
        }

    }
}

@Composable
fun SimpleBarChart(
    data: List<CategoryStock>,
    modifier: Modifier = Modifier
) {
    val maxValue = data.maxOfOrNull { it.totalQuantity }?.toFloat() ?: 1f

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.Bottom
    ) {
        data.forEach { categoryStock ->
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Bottom,
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.BottomCenter
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .fillMaxHeight(categoryStock.totalQuantity / maxValue)
                            .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                            .background(MaterialTheme.colorScheme.primary)
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = categoryStock.category,
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun InfoRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(text = label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(text = value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
fun StatCard(
    title: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Card(modifier = modifier) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        }
    }
}