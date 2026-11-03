package com.example.proyectofinal

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.proyectofinal.data.model.ProductoCarrito
import com.example.proyectofinal.ui.menu.TiendaViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartScreen(
    viewModel: TiendaViewModel = viewModel(),
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val cartItems = uiState.shoppingCart
    val total = cartItems.sumOf { it.producto.precio * it.cantidad }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Mi Carrito") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(16.dp)
        ) {
            if (cartItems.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("El carrito está vacío", style = MaterialTheme.typography.bodyLarge)
                }
            } else {
                LazyColumn(modifier = Modifier.weight(1f)) {
                    items(cartItems) { item ->
                        CartItemRow(
                            item = item,
                            onIncrease = { viewModel.cambiarCantidad(item.producto.id, 1) },
                            onDecrease = { viewModel.cambiarCantidad(item.producto.id, -1) }
                        )
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Total:", style = MaterialTheme.typography.headlineSmall)
                    Text("$%.2f".format(total), style = MaterialTheme.typography.headlineSmall)
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Button(
                    onClick = { /* Implementar Checkout */ },
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Text("Finalizar Compra", modifier = Modifier.padding(8.dp))
                }
                
                TextButton(
                    onClick = { viewModel.vaciarCarrito() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Vaciar Carrito", color = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}

@Composable
fun CartItemRow(
    item: ProductoCarrito,
    onIncrease: () -> Unit,
    onDecrease: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = "Producto #${item.producto.id}", style = MaterialTheme.typography.titleMedium)
                Text(text = "$%.2f".format(item.producto.precio), style = MaterialTheme.typography.bodyMedium)
            }
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onDecrease) { Text("-") }
                Text(
                    text = item.cantidad.toString(),
                    modifier = Modifier.padding(horizontal = 8.dp),
                    style = MaterialTheme.typography.bodyLarge
                )
                IconButton(onClick = onIncrease) { Text("+") }
            }
        }
    }
}
