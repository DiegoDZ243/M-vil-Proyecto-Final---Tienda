package com.example.proyectofinal.ui.pedidos

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.proyectofinal.data.model.EstadoPedido
import com.example.proyectofinal.data.model.Pedido
import com.example.proyectofinal.data.model.ProductoCarrito
import com.example.proyectofinal.data.model.estaDentroDeVentanaCancelacion
import com.example.proyectofinal.data.model.puedeCancelarse
import com.example.proyectofinal.ui.AppViewModelProvider
import com.example.proyectofinal.ui.components.RatingBar
import com.example.proyectofinal.ui.components.RatingSelector
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrdersScreen(
    viewModel: OrdenesViewModel = viewModel(factory = AppViewModelProvider.Factory),
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val pedidos = uiState.pedidos
    val snackbarHostState = remember { SnackbarHostState() }

    var pedidoACancelar by remember { mutableStateOf<Pedido?>(null) }
    var calificando by remember { mutableStateOf<Pair<Pedido, ProductoCarrito>?>(null) }

    LaunchedEffect(uiState.mensaje) {
        uiState.mensaje?.let { mensaje ->
            snackbarHostState.showSnackbar(message = mensaje, duration = SnackbarDuration.Short)
            viewModel.limpiarMensaje()
        }
    }

    // RF-14: confirmación antes de cancelar
    pedidoACancelar?.let { pedido ->
        AlertDialog(
            onDismissRequest = { pedidoACancelar = null },
            title = { Text("Cancelar pedido #${pedido.id}") },
            text = { Text("¿Seguro que quieres cancelar este pedido? Esta acción no se puede deshacer.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.cancelarPedido(pedido)
                    pedidoACancelar = null
                }) { Text("Cancelar pedido", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = { TextButton(onClick = { pedidoACancelar = null }) { Text("Volver") } }
        )
    }

    // RF-20: diálogo para calificar un producto
    calificando?.let { (pedido, item) ->
        var estrellas by remember { mutableIntStateOf(0) }
        AlertDialog(
            onDismissRequest = { calificando = null },
            title = { Text("Calificar producto") },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Text(item.producto.nombre, style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(12.dp))
                    RatingSelector(valor = estrellas, onValorChange = { estrellas = it })
                }
            },
            confirmButton = {
                TextButton(
                    enabled = estrellas in 1..5,
                    onClick = {
                        viewModel.calificarProducto(pedido, item.producto.id, estrellas)
                        calificando = null
                    }
                ) { Text("Enviar") }
            },
            dismissButton = { TextButton(onClick = { calificando = null }) { Text("Cancelar") } }
        )
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Mis pedidos") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Regresar")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        modifier = modifier
    ) { innerPadding ->
        if (uiState.isLoading) {
            Box(Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (pedidos.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "No tienes pedidos", style = MaterialTheme.typography.titleLarge)
                    TextButton(onClick = onNavigateBack) { Text("Ir a la tienda") }
                }
            }
        } else {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.TopCenter
            ) {
                LazyColumn(
                    contentPadding = innerPadding,
                    modifier = Modifier
                        .widthIn(max = 600.dp)
                        .fillMaxSize()
                ) {
                    items(pedidos, key = { it.id }) { pedido ->
                        OrderItem(
                            pedido = pedido,
                            onCancelar = { pedidoACancelar = pedido },
                            onFinalizar = { viewModel.finalizarPedido(pedido) },
                            onCalificar = { item -> calificando = pedido to item }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun OrderItem(
    pedido: Pedido,
    onCancelar: () -> Unit,
    onFinalizar: () -> Unit,
    onCalificar: (ProductoCarrito) -> Unit,
    modifier: Modifier = Modifier
) {
    val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
    val colorEstado = when (pedido.estado) {
        EstadoPedido.CANCELADO -> MaterialTheme.colorScheme.error
        EstadoPedido.FINALIZADO -> MaterialTheme.colorScheme.tertiary
        else -> MaterialTheme.colorScheme.primary
    }

    Card(
        modifier = modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Pedido #${pedido.id}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = pedido.estado,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = colorEstado
                )
            }

            Spacer(Modifier.height(8.dp))

            Text(
                text = "Fecha: ${dateFormat.format(Date(pedido.fecha))}",
                style = MaterialTheme.typography.bodyMedium
            )
            if (pedido.direccion.isNotBlank()) {
                Text("Envío a: ${pedido.direccion}", style = MaterialTheme.typography.bodyMedium)
            }
            if (pedido.metodoPago.isNotBlank()) {
                Text("Pago: ${pedido.metodoPago}", style = MaterialTheme.typography.bodyMedium)
            }

            Spacer(Modifier.height(8.dp))
            HorizontalDivider()
            Spacer(Modifier.height(8.dp))

            pedido.items.forEach { item ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(Modifier.weight(1f)) {
                        Text(
                            text = "${item.cantidad} x ${item.producto.nombre}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        if (item.calificacion > 0) {
                            RatingBar(
                                calificacion = item.calificacion.toDouble(),
                                numReviews = 1,
                                starSize = 16.dp,
                                mostrarTexto = false
                            )
                        }
                    }
                    // RF-20: solo se puede calificar tras finalizar la compra
                    if (pedido.estado == EstadoPedido.FINALIZADO && item.calificacion == 0) {
                        OutlinedButton(onClick = { onCalificar(item) }) { Text("Calificar") }
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            Text(
                text = "Total: $%.2f".format(pedido.total),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            if (pedido.estado == EstadoPedido.PENDIENTE) {
                Spacer(Modifier.height(12.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    // RF-14 / RF-15: cancelar solo dentro de las primeras 6 horas
                    OutlinedButton(
                        onClick = onCancelar,
                        enabled = pedido.puedeCancelarse(),
                        modifier = Modifier.weight(1f)
                    ) { Text("Cancelar") }
                    Button(onClick = onFinalizar, modifier = Modifier.weight(1f)) {
                        Text("Confirmar recepción")
                    }
                }
                if (!pedido.estaDentroDeVentanaCancelacion()) {
                    Text(
                        text = "No se puede cancelar: pasaron más de 6 horas desde que se realizó el pedido.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }
    }
}
