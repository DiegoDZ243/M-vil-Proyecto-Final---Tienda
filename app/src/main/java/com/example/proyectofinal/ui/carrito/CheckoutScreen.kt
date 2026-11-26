package com.example.proyectofinal.ui.carrito

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.proyectofinal.data.model.MetodoPago
import com.example.proyectofinal.ui.TiendaViewModel

/**
 * RF-10: pantalla de checkout (dirección, método de pago y resumen).
 * RF-12: al terminar la compra se muestra un diálogo de confirmación.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckoutScreen(
    viewModel: TiendaViewModel = viewModel(),
    onNavigateBack: () -> Unit,
    onVerPedidos: () -> Unit,
    onSeguirComprando: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val cartItems = uiState.shoppingCart
    val total = cartItems.sumOf { it.producto.precio * it.cantidad }
    val snackbarHostState = remember { SnackbarHostState() }

    var direccion by rememberSaveable { mutableStateOf("") }
    var metodoPago by rememberSaveable { mutableStateOf(MetodoPago.TARJETA) }
    var procesando by remember { mutableStateOf(false) }
    var pedidoConfirmadoId by rememberSaveable { mutableStateOf<Int?>(null) }

    LaunchedEffect(uiState.carritoError) {
        uiState.carritoError?.let { error ->
            snackbarHostState.showSnackbar(message = error, actionLabel = "OK")
            viewModel.limpiarErrorCarrito()
        }
    }

    // RF-12: mensaje de confirmación de compra exitosa
    pedidoConfirmadoId?.let { pedidoId ->
        AlertDialog(
            onDismissRequest = { },
            icon = {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            },
            title = { Text("¡Compra realizada!") },
            text = { Text("Tu pedido #$pedidoId se registró correctamente. Puedes darle seguimiento en \"Mis pedidos\".") },
            confirmButton = { TextButton(onClick = onVerPedidos) { Text("Ver mis pedidos") } },
            dismissButton = { TextButton(onClick = onSeguirComprando) { Text("Seguir comprando") } }
        )
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Finalizar compra") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack, enabled = pedidoConfirmadoId == null) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Regresar")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        modifier = modifier
    ) { innerPadding ->
        if (cartItems.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                if (pedidoConfirmadoId == null) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Tu carrito está vacío", style = MaterialTheme.typography.titleLarge)
                        TextButton(onClick = onSeguirComprando) { Text("Volver a la tienda") }
                    }
                }
            }
            return@Scaffold
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                modifier = Modifier
                    .widthIn(max = 600.dp)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
            // --- Dirección de envío ---
            Text("Dirección de envío", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = direccion,
                onValueChange = { if (it.length <= 150) direccion = it },
                label = { Text("Calle, número, colonia, ciudad y C.P.") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2,
                maxLines = 4
            )

            Spacer(Modifier.height(20.dp))

            // --- Método de pago ---
            Text("Método de pago", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(4.dp))
            Column(Modifier.selectableGroup()) {
                MetodoPago.opciones.forEach { opcion ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .selectable(
                                selected = metodoPago == opcion,
                                onClick = { metodoPago = opcion },
                                role = Role.RadioButton
                            )
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(selected = metodoPago == opcion, onClick = null)
                        Spacer(Modifier.width(12.dp))
                        Text(opcion, style = MaterialTheme.typography.bodyLarge)
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            // --- Resumen del pedido ---
            Text("Resumen del pedido", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp)) {
                    cartItems.forEach { item ->
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "${item.cantidad} x ${item.producto.nombre}",
                                modifier = Modifier.weight(1f),
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                text = "$%.2f".format(item.producto.precio * item.cantidad),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                    HorizontalDivider(Modifier.padding(vertical = 8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Envío", style = MaterialTheme.typography.bodyMedium)
                        Text("Gratis", style = MaterialTheme.typography.bodyMedium)
                    }
                    Spacer(Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Total", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        Text(
                            text = "$%.2f".format(total),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            Button(
                onClick = {
                    procesando = true
                    viewModel.crearPedido(direccion.trim(), metodoPago) { id ->
                        procesando = false
                        if (id != null) pedidoConfirmadoId = id
                    }
                },
                enabled = direccion.isNotBlank() && !procesando,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (procesando) {
                    CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp)
                } else {
                    Text("Confirmar pedido")
                }
            }
            }
        }
    }
}
