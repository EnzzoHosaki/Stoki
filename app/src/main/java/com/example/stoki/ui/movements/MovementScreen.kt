package com.example.stoki.ui.movements

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.stoki.data.MovementType
import com.example.stoki.data.ProductMovement
import com.example.stoki.ui.productlist.ProductListViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun MovementScreen(
    navController: NavController,
    viewModel: ProductListViewModel
) {
    val movements by viewModel.movements.collectAsState()

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    viewModel.clearTransaction()
                    navController.navigate("addMovement")
                },
                shape = CircleShape
            ) {
                Icon(Icons.Default.Add, "Nova Movimentação")
            }
        }
    ) { paddingValues ->
        if (movements.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                Text("Nenhuma movimentação registrada.")
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(movements) { movement ->
                    MovementListItem(movement = movement)
                }
            }
        }
    }
}

@Composable
fun MovementListItem(movement: ProductMovement) {
    val isSale = movement.type == MovementType.SALE
    val icon = if (isSale) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward
    val iconColor = if (isSale) Color(0xFF4CAF50) else MaterialTheme.colorScheme.error
    val amountPrefix = if (isSale) "+" else "-"

    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = movement.type.name,
                tint = iconColor,
                modifier = Modifier.size(40.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = movement.productName, fontWeight = FontWeight.Bold)
                Text(
                    text = SimpleDateFormat("dd/MM/yyyy 'às' HH:mm", Locale.getDefault()).format(movement.date),
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
            Text(
                text = "$amountPrefix R$ ${"%.2f".format(movement.amount)}",
                fontWeight = FontWeight.Bold,
                color = iconColor
            )
        }
    }
}