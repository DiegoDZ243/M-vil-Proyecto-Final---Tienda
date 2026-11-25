package com.example.proyectofinal.ui.mispublicaciones

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.proyectofinal.R
import com.example.proyectofinal.ui.AppViewModelProvider
import com.example.proyectofinal.ui.components.RatingBar
import com.example.proyectofinal.data.model.Producto

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
                title = { Text("Mis publicaciones") },
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
                    Text(text = "No tienes publicaciones", style = MaterialTheme.typography.titleLarge)
                    TextButton(onClick = onNavigateBack) { Text("Ir a la tienda") }
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

    // RF-18: confirmación antes de eliminar definitivamente
    productoAEliminar?.let { producto ->
        AlertDialog(
            onDismissRequest = { productoAEliminar = null },
            title = { Text("Eliminar publicación") },
            text = { Text("¿Seguro que quieres eliminar \"${producto.nombre}\"? Esta acción no se puede deshacer.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.eliminarPublicacion(producto) { ok ->
                        scope.launch {
                            snackbarHostState.showSnackbar(
                                if (ok) "Publicación eliminada" else "No se pudo eliminar la publicación"
                            )
                        }
                    }
                    productoAEliminar = null
                }) { Text("Eliminar", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = { TextButton(onClick = { productoAEliminar = null }) { Text("Cancelar") } }
        )
    }

    if (showDesactivarDialog && productoADesactivar != null) {
        AlertDialog(
            onDismissRequest = {
                showDesactivarDialog = false
                productoADesactivar = null
            },
            title = { Text("Desactivar publicación") },
            text = { Text("¿Estás seguro de que quieres desactivar esta publicación? No será visible para otros usuarios.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.desactivarPublicacion(productoADesactivar!!)
                        showDesactivarDialog = false
                        productoADesactivar = null
                    }
                ) {
                    Text("Desactivar")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showDesactivarDialog = false
                        productoADesactivar = null
                    }
                ) {
                    Text("Cancelar")
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

            // RF-19: calificación de 1 a 5
            RatingBar(
                calificacion = producto.calificacion,
                numReviews = producto.numReviews
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Stock: ${producto.stock}",
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = if (producto.activo) "Estado: Activo" else "Estado: Inactivo",
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
                    Text("Editar")
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
                    Text(if (producto.activo) "Desactivar" else "Activar")
                }

                Spacer(modifier = Modifier.width(8.dp))

                OutlinedButton(
                    onClick = onVerDetalle,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Ver detalle")
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
                Text("Eliminar publicación", color = MaterialTheme.colorScheme.error)
            }
        }
    }
}
