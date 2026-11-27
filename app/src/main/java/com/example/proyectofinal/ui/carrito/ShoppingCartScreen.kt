package com.example.proyectofinal.ui.carrito

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.material3.SnackbarHost
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.proyectofinal.data.model.ProductoCarrito
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.proyectofinal.ui.TiendaViewModel
import com.example.proyectofinal.ui.detalle.ProductDetailContent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShoppingCartScreen(
    viewModel: TiendaViewModel = viewModel(),
    onNavigateBack: () -> Unit,
    onProceedToCheckout: () -> Unit,
    // Se avisa al contenedor para ocultar/mostrar la barra lateral (solo cambia en tablets)
    onOcultarBarraLateral: (Boolean) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val cartItems = uiState.shoppingCart
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.carritoError) {
        if (uiState.carritoError != null) {
            snackbarHostState.showSnackbar(
                message = uiState.carritoError!!,
                actionLabel = "OK"
            )
            viewModel.limpiarErrorCarrito()
        }
    }

    // RF-11: confirmaciones de éxito (producto eliminado, carrito vaciado)
    LaunchedEffect(uiState.mensaje) {
        uiState.mensaje?.let { mensaje ->
            snackbarHostState.showSnackbar(message = mensaje, duration = SnackbarDuration.Short)
            viewModel.limpiarMensaje()
        }
    }

    val precioTotal = cartItems.sumOf {
        it.producto.precio * it.cantidad
    }

    // Tablet = ancho mínimo de pantalla >= 600dp (no depende de la orientación)
    val esTablet = LocalConfiguration.current.smallestScreenWidthDp >= 600
    var itemSeleccionadoId by rememberSaveable { mutableStateOf<Int?>(null) }
    val itemSeleccionado = cartItems.find { it.producto.id == itemSeleccionadoId }

    // En tablet: al elegir un artículo se oculta la barra lateral y el detalle ocupa ese espacio
    val ocultarBarra = esTablet && itemSeleccionado != null
    LaunchedEffect(ocultarBarra) { onOcultarBarraLateral(ocultarBarra) }
    // Al salir del carrito la barra lateral siempre vuelve
    DisposableEffect(Unit) { onDispose { onOcultarBarraLateral(false) } }

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
            // En tablet la barra de total va dentro del panel izquierdo
            if (cartItems.isNotEmpty() && !esTablet) {
                CartBottomBar(
                    total = precioTotal,
                    onCheckout = onProceedToCheckout,
                    onClearCart = { viewModel.vaciarCarrito() }
                )
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
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
        } else if (esTablet) {
            // Diseño maestro-detalle: carrito a la izquierda, detalle del artículo a la derecha
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                Column(modifier = Modifier.weight(0.45f).fillMaxHeight()) {
                    LazyColumn(modifier = Modifier.weight(1f)) {
                        items(items = cartItems, key = { it.producto.id }) { item ->
                            CartItem(
                                item = item,
                                selected = item.producto.id == itemSeleccionadoId,
                                onClick = { itemSeleccionadoId = item.producto.id },
                                onIncrease = { viewModel.cambiarCantidad(item.producto.id, 1) },
                                onDecrease = { viewModel.cambiarCantidad(item.producto.id, -1) },
                                onRemove = {
                                    viewModel.cambiarCantidad(item.producto.id, -item.cantidad)
                                }
                            )
                        }
                    }
                    CartBottomBar(
                        total = precioTotal,
                        onCheckout = onProceedToCheckout,
                        onClearCart = { viewModel.vaciarCarrito() }
                    )
                }

                VerticalDivider()

                Box(modifier = Modifier.weight(0.55f).fillMaxHeight()) {
                    if (itemSeleccionado != null) {
                        // Se toma el producto actualizado de la tienda (stock, calificación) si existe
                        val productoDetalle =
                            uiState.productos.find { it.id == itemSeleccionado.producto.id }
                                ?: itemSeleccionado.producto
                        ProductDetailContent(
                            producto = productoDetalle,
                            onAgregarAlCarrito = { viewModel.agregarAlCarrito(productoDetalle) },
                            modifier = Modifier.fillMaxSize()
                        )
                        // Cerrar el detalle: vuelve a mostrarse la barra lateral
                        IconButton(
                            onClick = { itemSeleccionadoId = null },
                            modifier = Modifier.align(Alignment.TopEnd).padding(4.dp)
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Cerrar detalle")
                        }
                    } else {
                        Box(
                            modifier = Modifier.fillMaxSize().padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Selecciona un artículo del carrito para ver su detalle",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        } else {
            LazyColumn(
                contentPadding = innerPadding,
                modifier = Modifier.fillMaxSize()
            ) {
                items(
                    items = cartItems,
                    key = { it.producto.id }
                ) { item ->
                    CartItem(
                        item = item,
                        onIncrease = { viewModel.cambiarCantidad(item.producto.id, 1) },
                        onDecrease = { viewModel.cambiarCantidad(item.producto.id, -1) },
                        onRemove = {
                            viewModel.cambiarCantidad(item.producto.id, -item.cantidad)
                        }
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
    selected: Boolean = false,
    onClick: (() -> Unit)? = null,
    onIncrease: () -> Unit,
    onDecrease: () -> Unit,
    onRemove: () -> Unit // Nuevo parámetro
) {
    Card(
        colors = if (selected) {
            CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
        } else {
            CardDefaults.cardColors()
        },
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = item.producto.imagen,
                contentDescription = null,
                modifier = Modifier
                    .size(100.dp)
                    .clip(MaterialTheme.shapes.small),
                contentScale = ContentScale.Crop
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        text = item.producto.nombre,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f) // Evita que un nombre largo empuje el ícono fuera de la pantalla
                    )
                    IconButton(
                        onClick = onRemove,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Eliminar ${item.producto.nombre}",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }

                Text(
                    text = "$%.2f".format(item.producto.precio),
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = "Subtotal: $%.2f".format(item.producto.precio * item.cantidad),
                    style = MaterialTheme.typography.bodyMedium
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onDecrease) {
                        Icon(Icons.Default.Remove, contentDescription = "Disminuir cantidad")
                    }
                    Text(
                        text = item.cantidad.toString(),
                        style = MaterialTheme.typography.titleMedium
                    )
                    IconButton(onClick = onIncrease) {
                        Icon(Icons.Default.Add, contentDescription = "Aumentar cantidad")
                    }
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