package com.example.proyectofinal.ui.pedidos

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.proyectofinal.R
import com.example.proyectofinal.data.model.EstadoPedido
import com.example.proyectofinal.data.model.Pedido
import com.example.proyectofinal.data.model.ProductoCarrito
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

    pedidoACancelar?.let { pedido ->
        AlertDialog(
            onDismissRequest = { pedidoACancelar = null },
            title = { Text("${stringResource(R.string.cancelar_pedido)} #${pedido.id}") },
            text = { Text(stringResource(R.string.confirmar_eliminar)) },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.cancelarPedido(pedido)
                    pedidoACancelar = null
                }) { Text(stringResource(R.string.cancelar_pedido), color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = { TextButton(onClick = { pedidoACancelar = null }) { Text(stringResource(R.string.cancelar)) } }
        )
    }

    calificando?.let { (pedido, item) ->
        var estrellas by remember { mutableIntStateOf(0) }
        AlertDialog(
            onDismissRequest = { calificando = null },
            title = { Text(stringResource(R.string.calificar_producto)) },
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
                ) { Text(stringResource(R.string.aceptar)) }
            },
            dismissButton = { TextButton(onClick = { calificando = null }) { Text(stringResource(R.string.cancelar)) } }
        )
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(R.string.ordenes_titulo)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = null)
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
                    Text(text = stringResource(R.string.ordenes_titulo), style = MaterialTheme.typography.titleLarge)
                    TextButton(onClick = onNavigateBack) { Text(stringResource(R.string.seguir_comprando)) }
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

    val estadoTexto = when (pedido.estado) {
        EstadoPedido.PENDIENTE -> stringResource(R.string.estado_pendiente)
        EstadoPedido.FINALIZADO -> stringResource(R.string.estado_finalizado)
        EstadoPedido.CANCELADO -> stringResource(R.string.estado_cancelado)
        else -> pedido.estado
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
                    text = "${stringResource(R.string.pedido_num)}${pedido.id}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = estadoTexto,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = colorEstado
                )
            }

            Spacer(Modifier.height(8.dp))

            Text(
                text = dateFormat.format(Date(pedido.fecha)),
                style = MaterialTheme.typography.bodyMedium
            )
            if (pedido.direccion.isNotBlank()) {
                Text(pedido.direccion, style = MaterialTheme.typography.bodyMedium)
            }
            if (pedido.metodoPago.isNotBlank()) {
                Text(pedido.metodoPago, style = MaterialTheme.typography.bodyMedium)
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
                                mostrarTexto = false,
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }
                    }
                    if (pedido.estado == EstadoPedido.FINALIZADO) {
                        if (item.calificacion == 0) {
                            TextButton(onClick = { onCalificar(item) }) {
                                Text(stringResource(R.string.calificar_producto))
                            }
                        } else {
                            Text(
                                stringResource(R.string.calificado),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.tertiary
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(8.dp))
            HorizontalDivider()
            Spacer(Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${stringResource(R.string.total)}: $%.2f".format(pedido.total),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (pedido.puedeCancelarse()) {
                        OutlinedButton(
                            onClick = onCancelar,
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = MaterialTheme.colorScheme.error
                            )
                        ) {
                            Text(stringResource(R.string.cancelar_pedido))
                        }
                    }
                    if (pedido.estado == EstadoPedido.PENDIENTE) {
                        OutlinedButton(onClick = onFinalizar) {
                            Text(stringResource(R.string.marcar_recibido))
                        }
                    }
                }
            }
        }
    }
}
