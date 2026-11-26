package com.example.proyectofinal.ui.mispublicaciones

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.proyectofinal.R
import com.example.proyectofinal.data.model.Producto
import com.example.proyectofinal.ui.AppViewModelProvider
import com.example.proyectofinal.ui.components.RatingBar
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MisPublicacionesScreen(
    viewModel: PublicacionesViewModel = viewModel(factory = AppViewModelProvider.Factory),
    onNavigateBack: () -> Unit,
    onNavigateToEdit: (Int) -> Unit,
    onNavigateToDetail: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    var showDesactivarDialog by remember { mutableStateOf(false) }
    var productoADesactivar by remember { mutableStateOf<Producto?>(null) }
    var productoAEliminar by remember { mutableStateOf<Producto?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(R.string.mis_publicaciones)) },
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
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(50.dp)
                )
            }
        } else if (uiState.productos.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = stringResource(R.string.sin_publicaciones), style = MaterialTheme.typography.titleLarge)
                    TextButton(onClick = onNavigateBack) { Text(stringResource(R.string.ir_a_tienda)) }
                }
            }
        } else {
            LazyColumn(
                contentPadding = innerPadding,
                modifier = Modifier.fillMaxSize()
            ) {
                items(uiState.productos) { producto ->
                    PublicacionItem(
                        producto = producto,
                        onEdit = { onNavigateToEdit(producto.id) },
                        onToggleActivo = {
                            if (producto.activo) {
                                productoADesactivar = producto
                                showDesactivarDialog = true
                            } else {
                                viewModel.activarPublicacion(producto)
                            }
                        },
                        onVerDetalle = { onNavigateToDetail(producto.id) },
                        onEliminar = { productoAEliminar = producto }
                    )
                }
            }
        }
    }

    productoAEliminar?.let { producto ->
        AlertDialog(
            onDismissRequest = { productoAEliminar = null },
            title = { Text(stringResource(R.string.eliminar_publicacion)) },
            text = { Text(stringResource(R.string.confirmar_eliminar)) },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.eliminarPublicacion(producto) { ok ->
                        scope.launch {
                            snackbarHostState.showSnackbar(
                                if (ok) "Publicación eliminada" else "No se pudo eliminar"
                            )
                        }
                    }
                    productoAEliminar = null
                }) { Text(stringResource(R.string.eliminar), color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = { TextButton(onClick = { productoAEliminar = null }) { Text(stringResource(R.string.cancelar)) } }
        )
    }

    if (showDesactivarDialog && productoADesactivar != null) {
        AlertDialog(
            onDismissRequest = {
                showDesactivarDialog = false
                productoADesactivar = null
            },
            title = { Text(stringResource(R.string.desactivar_publicacion)) },
            text = { Text(stringResource(R.string.confirmar_desactivar)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.desactivarPublicacion(productoADesactivar!!)
                        showDesactivarDialog = false
                        productoADesactivar = null
                    }
                ) {
                    Text(stringResource(R.string.desactivar))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showDesactivarDialog = false
                        productoADesactivar = null
                    }
                ) {
                    Text(stringResource(R.string.cancelar))
                }
            }
        )
    }
}

@Composable
fun PublicacionItem(
    producto: Producto,
    modifier: Modifier = Modifier,
    onEdit: () -> Unit,
    onToggleActivo: () -> Unit,
    onVerDetalle: () -> Unit,
    onEliminar: () -> Unit
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            AsyncImage(
                model = producto.imagen,
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = producto.nombre,
                style = MaterialTheme.typography.titleLarge
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "$%.2f".format(producto.precio),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(4.dp))

            RatingBar(
                calificacion = producto.calificacion,
                numReviews = producto.numReviews
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = stringResource(R.string.stock_count, producto.stock),
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = if (producto.activo) stringResource(R.string.estado_activo) else stringResource(R.string.estado_inactivo),
                style = MaterialTheme.typography.bodyLarge,
                color = if (producto.activo) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                OutlinedButton(
                    onClick = onEdit,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(stringResource(R.string.editar))
                }

                Spacer(modifier = Modifier.width(8.dp))

                OutlinedButton(
                    onClick = onToggleActivo,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        if (producto.activo) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(if (producto.activo) stringResource(R.string.desactivar) else stringResource(R.string.activar))
                }

                Spacer(modifier = Modifier.width(8.dp))

                OutlinedButton(
                    onClick = onVerDetalle,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(stringResource(R.string.ver_detalle))
                }
            }

            TextButton(
                onClick = onEliminar,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(stringResource(R.string.eliminar_publicacion), color = MaterialTheme.colorScheme.error)
            }
        }
    }
}
