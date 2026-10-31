package com.example.proyectofinal.ui.carrito

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.proyectofinal.data.model.ProductoCarrito
import com.example.proyectofinal.ui.menu.TiendaViewModel
import androidx.lifecycle.viewmodel.compose.viewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShoppingCartScreen(
    viewModel: TiendaViewModel = viewModel(),
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val cartItems = uiState.shoppingCart

    val precioTotal = cartItems.sumOf {
        it.producto.precio * it.cantidad
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Carrito de compras") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Regresar")
                    }
                }
            )
        },
        bottomBar = {
            if (cartItems.isNotEmpty()) {
                CartBottomBar(
                    total = precioTotal,
                    onCheckout = { viewModel.crearPedido() },
                    onClearCart = { viewModel.vaciarCarrito() }
                )
            }
        },
        modifier = modifier
    ) { innerPadding ->
        if (cartItems.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "Tu carrito está vacío", style = MaterialTheme.typography.titleLarge)
                    TextButton(onClick = onNavigateBack) { Text("Volver a la tienda") }
                }
            }
        } else {
            LazyColumn(
                contentPadding = innerPadding,
                modifier = Modifier.fillMaxSize()
            ) {
                items(cartItems) { item ->
                    CartItem(
                        item = item,
                        onIncrease = { viewModel.cambiarCantidad(item.producto.id, 1) },
                        onDecrease = { viewModel.cambiarCantidad(item.producto.id, -1) }
                    )
                }
            }
        }
    }
}

@Composable
fun CartItem(
    item: ProductoCarrito,
    modifier: Modifier = Modifier,
    onIncrease: () -> Unit,
    onDecrease: () -> Unit
) {
    Card(
        modifier = modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = item.producto.imagen),
                contentDescription = null,
                modifier = Modifier.size(80.dp).clip(MaterialTheme.shapes.small),
                contentScale = ContentScale.Crop
            )
            Column(modifier = Modifier.weight(1f).padding(start = 16.dp)) {
                Text(text = stringResource(id = item.producto.nombre), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(text = "$%.2f".format(item.producto.precio), style = MaterialTheme.typography.bodyLarge)
                Text(text = "Subtotal: $%.2f".format(item.producto.precio * item.cantidad), style = MaterialTheme.typography.bodyMedium)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onDecrease) { Icon(Icons.Default.Remove, contentDescription = null) }
                    Text(text = item.cantidad.toString(), style = MaterialTheme.typography.titleMedium)
                    IconButton(onClick = onIncrease) { Icon(Icons.Default.Add, contentDescription = null) }
                }
            }
        }
    }
}

@Composable
fun CartBottomBar(
    total: Double,
    onCheckout: () -> Unit,
    onClearCart: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth().padding(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "Total:", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text(text = "$%.2f".format(total), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        }
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onCheckout, modifier = Modifier.fillMaxWidth()) { Text("Proceder al pago") }
        TextButton(onClick = onClearCart, modifier = Modifier.fillMaxWidth()) {
            Icon(Icons.Default.Delete, contentDescription = null)
            Spacer(modifier = Modifier.size(8.dp))
            Text(text = "Vaciar carrito", color = MaterialTheme.colorScheme.error)
        }
    }
}
