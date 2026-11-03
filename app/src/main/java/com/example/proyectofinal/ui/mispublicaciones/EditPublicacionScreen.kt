package com.example.proyectofinal.ui.vender

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.proyectofinal.ui.AppViewModelProvider
import com.example.proyectofinal.ui.mispublicaciones.PublicacionesViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProductScreen(
    viewModelPublicacion: PublicacionesViewModel = viewModel(factory = AppViewModelProvider.Factory),
    productId: Int?,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModelPublicacion.uiState.collectAsState()

    val productoAEditar = uiState.productos.find { it.id == productId }

    var nombre by remember { mutableStateOf("") }
    var descripcion by remember { mutableStateOf("") }
    var precio by remember { mutableStateOf("") }
    var categoria by remember { mutableStateOf("") }
    var stock by remember { mutableStateOf("") }

    var imagenUrlServidor by remember { mutableStateOf("") }
    var nuevaImagenUri by remember { mutableStateOf<Uri?>(null) }
    var isUpdating by remember { mutableStateOf(false) }

    LaunchedEffect(productoAEditar) {
        productoAEditar?.let { producto ->
            nombre = producto.nombre
            descripcion = producto.descripcion
            precio = producto.precio.toString()
            categoria = producto.categoria
            stock = producto.stock.toString()
            imagenUrlServidor = producto.imagen
        }
    }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            if (uri != null) {
                nuevaImagenUri = uri
            }
        }
    )

    if (isUpdating) {
        Dialog(onDismissRequest = { }) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp
            ) {
                Row(
                    modifier = Modifier.padding(24.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(text = "Actualizando publicación...")
                }
            }
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Editar producto") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Regresar")
                    }
                }
            )
        },
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(150.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .clickable {
                        photoPickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                //Decidimos qué imagen mostrar: la nueva Uri, o la URL del servidor si no ha seleccionado ninguna
                val imagenAMostrar = nuevaImagenUri ?: imagenUrlServidor

                if (imagenAMostrar.toString().isNotBlank()) {
                    AsyncImage(
                        model = imagenAMostrar,
                        contentDescription = "Previsualización de producto",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Text("Tocar para imagen", style = MaterialTheme.typography.bodySmall)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = nombre,
                onValueChange = { nombre = it },
                label = { Text("Nombre del producto") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = descripcion,
                onValueChange = { descripcion = it },
                label = { Text("Descripción") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                maxLines = 5
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = precio,
                onValueChange = { precio = it },
                label = { Text("Precio") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = categoria,
                onValueChange = { categoria = it },
                label = { Text("Categoría") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = stock,
                onValueChange = { stock = it },
                label = { Text("Stock") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    val precioDouble = precio.toDoubleOrNull() ?: 0.0
                    val stockInt = stock.toIntOrNull() ?: 0

                    isUpdating = true
                    // Le pasamos el producto original, los textos actuales de la UI
                    // y la Uri de la NUEVA imagen (que será null si no tocó la foto)
                    viewModelPublicacion.editarPublicacion(
                        productoActual = productoAEditar,
                        nuevoNombre = nombre,
                        nuevaDescripcion = descripcion,
                        nuevoPrecio = precioDouble,
                        nuevaCategoria = categoria,
                        nuevoStock = stockInt,
                        nuevaImagenUri = nuevaImagenUri,
                        onComplete={
                            isUpdating=false
                            onNavigateBack()
                        }
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = nombre.isNotBlank()
            ) {
                Text("Actualizar")
            }
        }
    }
}