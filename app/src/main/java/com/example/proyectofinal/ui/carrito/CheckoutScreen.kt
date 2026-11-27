package com.example.proyectofinal.ui.carrito

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.proyectofinal.R
import com.example.proyectofinal.data.model.MetodoPago
import com.example.proyectofinal.ui.TiendaViewModel

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
            title = { Text(stringResource(R.string.pedido_exitoso)) },
            text = { Text("#$pedidoId") },
            confirmButton = { TextButton(onClick = onVerPedidos) { Text(stringResource(R.string.ver_mis_pedidos)) } },
            dismissButton = { TextButton(onClick = onSeguirComprando) { Text(stringResource(R.string.seguir_comprando)) } }
        )
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(R.string.checkout_titulo)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack, enabled = pedidoConfirmadoId == null) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = null)
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
                        Text(stringResource(R.string.carrito_vacio), style = MaterialTheme.typography.titleLarge)
                        TextButton(onClick = onSeguirComprando) { Text(stringResource(R.string.seguir_comprando)) }
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
                Text(
                    text = stringResource(R.string.direccion_envio),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(8.dp))
                OutlinedButton(
                    onClick = { direccion = uiState.direccionPerfil },
                    enabled = uiState.direccionPerfil.isNotBlank(),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Usar mi dirección de perfil")
                }
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = direccion,
                    onValueChange = { if (it.length <= 150) direccion = it },
                    label = { Text(stringResource(R.string.direccion_envio)) },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    maxLines = 4
                )

                Spacer(Modifier.height(20.dp))

                Text(
                    text = stringResource(R.string.metodo_pago),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
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

                Text(
                    text = stringResource(R.string.resumen_pedido),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
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
                            Text(
                                text = stringResource(R.string.total),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
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
                        Text(stringResource(R.string.confirmar_compra))
                    }
                }
            }
        }
    }
}