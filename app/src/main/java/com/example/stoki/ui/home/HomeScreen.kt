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

@Composable
fun HomeScreen(viewModel: ProductListViewModel) {
    val totalStockValue by viewModel.totalStockValue.collectAsState()
    val lowStockCount by viewModel.lowStockCount.collectAsState()
    val stockByCategory by viewModel.stockByCategory.collectAsState()

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Cartões de Estatísticas
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                StatCard(
                    title = "Valor Total do Estoque",
                    value = "R$ ${"%.2f".format(totalStockValue)}",
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "Itens Estoque Baixo",
                    value = lowStockCount.toString(),
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Gráfico de Estoque por Categoria
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
                        // NOSSO NOVO GRÁFICO CUSTOMIZADO
                        SimpleBarChart(
                            data = stockByCategory,
                            modifier = Modifier.fillMaxWidth().height(200.dp)
                        )
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
    val maxValue = data.maxOfOrNull { it.totalQuantity } ?: 1

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.Bottom
    ) {
        data.forEach { categoryStock ->
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Bottom,
                modifier = Modifier.weight(1f)
            ) {
                // A Barra
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.6f) // Largura da barra
                        .fillMaxHeight((categoryStock.totalQuantity.toFloat() / maxValue.toFloat()))
                        .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                        .background(MaterialTheme.colorScheme.primary)
                )
                Spacer(modifier = Modifier.height(4.dp))
                // O Label da Categoria
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