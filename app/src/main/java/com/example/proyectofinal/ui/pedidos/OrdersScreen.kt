package com.example.proyectofinal.ui.pedidos

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
    isExpandedWidth: Boolean,
    viewModel: OrdenesViewModel = viewModel(factory = AppViewModelProvider.Factory),
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val pedidos = uiState.pedidos
    val snackbarHostState = remember { SnackbarHostState() }

    val esPantallaDividida = isExpandedWidth
    val mostrandoDetallePantallaCompleta = !esPantallaDividida && !uiState.mostrandoLista

    var pedidoACancelar by remember { mutableStateOf<Pedido?>(null) }
    var calificando by remember { mutableStateOf<Pair<Pedido, ProductoCarrito>?>(null) }

    BackHandler(enabled = mostrandoDetallePantallaCompleta) {
        viewModel.regresarALista()
    }

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
                }) {
                    Text(
                        stringResource(R.string.cancelar_pedido),
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { pedidoACancelar = null }) {
                    Text(stringResource(R.string.cancelar))
                }
            }
        )
    }

    calificando?.let { (pedido, item) ->
        var estrellas by remember { mutableIntStateOf(0) }
        AlertDialog(
            onDismissRequest = { calificando = null },
            title = { Text(stringResource(R.string.calificar_producto)) },
            text = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
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
            dismissButton = {
                TextButton(onClick = { calificando = null }) {
                    Text(stringResource(R.string.cancelar))
                }
            }
        )
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    val titulo = if (mostrandoDetallePantallaCompleta && uiState.pedidoSeleccionado != null) {
                        "${stringResource(R.string.pedido_num)}${uiState.pedidoSeleccionado?.id}"
                    } else {
                        stringResource(R.string.ordenes_titulo)
                    }
                    Text(titulo)
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            if (mostrandoDetallePantallaCompleta) {
                                viewModel.regresarALista()
                            } else {
                                onNavigateBack()
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = null
                        )
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        modifier = modifier
    ) { innerPadding ->
        when {
            uiState.isLoading -> {
                Box(
                    Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            pedidos.isEmpty() -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = stringResource(R.string.ordenes_titulo),
                            style = MaterialTheme.typography.titleLarge
                        )
                        TextButton(onClick = onNavigateBack) {
                            Text(stringResource(R.string.seguir_comprando))
                        }
                    }
                }
            }

            esPantallaDividida -> {
                OrdersListAndDetailContent(
                    pedidos = pedidos,
                    pedidoSeleccionado = uiState.pedidoSeleccionado,
                    onPedidoClick = { viewModel.seleccionarPedido(it) },
                    onCancelar = { pedidoACancelar = it },
                    onFinalizar = { viewModel.finalizarPedido(it) },
                    onCalificar = { pedido, item -> calificando = pedido to item },
                    contentPadding = innerPadding,
                    modifier = Modifier.fillMaxSize()
                )
            }

            mostrandoDetallePantallaCompleta -> {
                uiState.pedidoSeleccionado?.let { pedido ->
                    LazyColumn(
                        contentPadding = innerPadding,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        item {
                            OrderDetailCard(
                                pedido = pedido,
                                onCancelar = { pedidoACancelar = pedido },
                                onFinalizar = { viewModel.finalizarPedido(pedido) },
                                onCalificar = { item -> calificando = pedido to item },
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                            )
                        }
                    }
                }
            }

            else -> {
                LazyColumn(
                    contentPadding = innerPadding,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp)
                ) {
                    items(pedidos, key = { it.id }) { pedido ->
                        OrderSummaryCard(
                            pedido = pedido,
                            seleccionado = false,
                            onClick = { viewModel.seleccionarPedido(pedido) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun OrdersListAndDetailContent(
    pedidos: List<Pedido>,
    pedidoSeleccionado: Pedido?,
    onPedidoClick: (Pedido) -> Unit,
    onCancelar: (Pedido) -> Unit,
    onFinalizar: (Pedido) -> Unit,
    onCalificar: (Pedido, ProductoCarrito) -> Unit,
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        LazyColumn(
            contentPadding = contentPadding,
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.weight(1f)
        ) {
            items(pedidos, key = { it.id }) { pedido ->
                OrderSummaryCard(
                    pedido = pedido,
                    seleccionado = pedidoSeleccionado?.id == pedido.id,
                    onClick = { onPedidoClick(pedido) }
                )
            }
        }

        LazyColumn(
            contentPadding = contentPadding,
            modifier = Modifier.weight(1f)
        ) {
            item {
                pedidoSeleccionado?.let { pedido ->
                    OrderDetailCard(
                        pedido = pedido,
                        onCancelar = { onCancelar(pedido) },
                        onFinalizar = { onFinalizar(pedido) },
                        onCalificar = { item -> onCalificar(pedido, item) }
                    )
                }
            }
        }
    }
}

@Composable
fun OrderSummaryCard(
    pedido: Pedido,
    seleccionado: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dateFormat = remember { SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()) }
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
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = if (seleccionado) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.secondaryContainer
            }
        ),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
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
            Spacer(Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = dateFormat.format(Date(pedido.fecha)),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "$%.2f".format(pedido.total),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun OrderDetailCard(
    pedido: Pedido,
    onCancelar: () -> Unit,
    onFinalizar: () -> Unit,
    onCalificar: (ProductoCarrito) -> Unit,
    modifier: Modifier = Modifier
) {
    val dateFormat = remember { SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()) }
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
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        modifier = modifier.fillMaxWidth()
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
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
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

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
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