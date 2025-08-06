package com.example.stoki.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.stoki.data.Brand
import com.example.stoki.data.Category
import com.example.stoki.ui.productlist.ProductListViewModel
import androidx.compose.material.icons.filled.Add

@Composable
fun SettingsScreen(
    isDarkTheme: Boolean,
    onThemeChange: (Boolean) -> Unit,
    viewModel: ProductListViewModel
) {
    val categories by viewModel.categories.collectAsState()
    val brands by viewModel.brands.collectAsState()

    var showAddCategoryDialog by remember { mutableStateOf(false) }
    var showAddBrandDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Modo Escuro", style = MaterialTheme.typography.bodyLarge)
                Switch(checked = isDarkTheme, onCheckedChange = onThemeChange)
            }
            Divider(modifier = Modifier.padding(top = 16.dp))
        }

        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Gerenciar Categorias", style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                IconButton(onClick = { showAddCategoryDialog = true }) {
                    Icon(Icons.Default.Add, contentDescription = "Adicionar Categoria")
                }
            }
        }
        items(categories) { category ->
            ManagementListItem(
                name = category.name,
                onDelete = { viewModel.deleteCategory(category) }
            )
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Gerenciar Marcas", style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                IconButton(onClick = { showAddBrandDialog = true }) {
                    Icon(Icons.Default.Add, contentDescription = "Adicionar Marca")
                }
            }
        }
        items(brands) { brand ->
            ManagementListItem(
                name = brand.name,
                onDelete = { viewModel.deleteBrand(brand) }
            )
        }
    }
    if (showAddCategoryDialog) {
        var newCategoryName by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showAddCategoryDialog = false },
            title = { Text("Adicionar Nova Categoria") },
            text = {
                OutlinedTextField(
                    value = newCategoryName,
                    onValueChange = { newCategoryName = it },
                    label = { Text("Nome da Categoria") },
                    singleLine = true
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.insertCategory(newCategoryName)
                        showAddCategoryDialog = false
                    },
                    enabled = newCategoryName.isNotBlank()
                ) { Text("Adicionar") }
            },
            dismissButton = {
                TextButton(onClick = { showAddCategoryDialog = false }) { Text("Cancelar") }
            }
        )
    }

    if (showAddBrandDialog) {
        var newBrandName by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showAddBrandDialog = false },
            title = { Text("Adicionar Nova Marca") },
            text = {
                OutlinedTextField(
                    value = newBrandName,
                    onValueChange = { newBrandName = it },
                    label = { Text("Nome da Marca") },
                    singleLine = true
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.insertBrand(newBrandName)
                        showAddBrandDialog = false
                    },
                    enabled = newBrandName.isNotBlank()
                ) { Text("Adicionar") }
            },
            dismissButton = {
                TextButton(onClick = { showAddBrandDialog = false }) { Text("Cancelar") }
            }
        )
    }
}

@Composable
fun ManagementListItem(name: String, onDelete: () -> Unit) {
    var showDeleteDialog by remember { mutableStateOf(false) }

    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = name, modifier = Modifier.weight(1f))
            IconButton(onClick = { showDeleteDialog = true }) {
                Icon(Icons.Default.Delete, contentDescription = "Deletar", tint = MaterialTheme.colorScheme.error)
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Confirmar Exclusão") },
            text = { Text("Você tem certeza que deseja excluir '$name'? Esta ação não pode ser desfeita.") },
            confirmButton = {
                Button(
                    onClick = {
                        onDelete()
                        showDeleteDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) { Text("Excluir") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("Cancelar") }
            }
        )
    }
}