package com.example.proyectofinal.ui.vender

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.proyectofinal.R
import com.example.proyectofinal.ui.AppViewModelProvider
import com.example.proyectofinal.ui.mispublicaciones.PublicacionesViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProductScreen(
    isExpandedWidth: Boolean,
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

    val onPickImage = {
        photoPickerLauncher.launch(
            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
        )
    }

    val onGuardarCambios = {
        val precioDouble = precio.toDoubleOrNull() ?: 0.0
        val stockInt = stock.toIntOrNull() ?: 0

        isUpdating = true
        viewModelPublicacion.editarPublicacion(
            productoActual = productoAEditar,
            nuevoNombre = nombre,
            nuevaDescripcion = descripcion,
            nuevoPrecio = precioDouble,
            nuevaCategoria = categoria,
            nuevoStock = stockInt,
            nuevaImagenUri = nuevaImagenUri,
            onComplete = {
                isUpdating = false
                onNavigateBack()
            }
        )
    }

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
                    Text(text = stringResource(R.string.actualizando_publicacion))
                }
            }
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(R.string.editar_producto)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.regresar)
                        )
                    }
                }
            )
        },
        modifier = modifier
    ) { innerPadding ->
        val imagenAMostrar = nuevaImagenUri ?: imagenUrlServidor

        if (isExpandedWidth) {
            // DISEÑO TABLET / PANTALLA ANCHA: 2 Columnas (Vista previa + Formulario)
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // Columna izquierda: Imagen + Vista previa en vivo
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    ProductImageSelector(
                        imagenAMostrar = imagenAMostrar,
                        onPickImage = onPickImage,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(240.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    ProductLivePreviewCard(
                        nombre = nombre,
                        descripcion = descripcion,
                        precio = precio,
                        categoria = categoria,
                        stock = stock
                    )
                }

                // Columna derecha: Campos del formulario
                Column(
                    modifier = Modifier
                        .weight(1.2f)
                        .verticalScroll(rememberScrollState())
                ) {
                    EditProductFormFields(
                        nombre = nombre,
                        onNombreChange = { nombre = it },
                        descripcion = descripcion,
                        onDescripcionChange = { descripcion = it },
                        precio = precio,
                        onPrecioChange = { precio = it },
                        categoria = categoria,
                        onCategoriaChange = { categoria = it },
                        stock = stock,
                        onStockChange = { stock = it },
                        agruparPrecioYStock = true,
                        onGuardar = onGuardarCambios
                    )
                }
            }
        } else {
            // DISEÑO MÓVIL: 1 Columna con Scroll
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                ProductImageSelector(
                    imagenAMostrar = imagenAMostrar,
                    onPickImage = onPickImage,
                    modifier = Modifier.size(160.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                EditProductFormFields(
                    nombre = nombre,
                    onNombreChange = { nombre = it },
                    descripcion = descripcion,
                    onDescripcionChange = { descripcion = it },
                    precio = precio,
                    onPrecioChange = { precio = it },
                    categoria = categoria,
                    onCategoriaChange = { categoria = it },
                    stock = stock,
                    onStockChange = { stock = it },
                    agruparPrecioYStock = false,
                    onGuardar = onGuardarCambios
                )
            }
        }
    }
}

@Composable
internal fun ProductImageSelector(
    imagenAMostrar: Any,
    onPickImage: () -> Unit,
    textoPlaceholder: String = stringResource(R.string.tocar_para_imagen),
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .clickable(onClick = onPickImage),
        contentAlignment = Alignment.Center
    ) {
        if (imagenAMostrar.toString().isNotBlank()) {
            AsyncImage(
                model = imagenAMostrar,
                contentDescription = stringResource(R.string.previsualizacion_producto),
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        } else {
            Text(
                text = textoPlaceholder,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
internal fun ProductLivePreviewCard(
    nombre: String,
    descripcion: String,
    precio: String,
    categoria: String,
    stock: String,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = stringResource(R.string.previsualizacion_producto),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = nombre.ifBlank { stringResource(R.string.nombre_producto) },
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            val precioFormateado = precio.toDoubleOrNull()?.let { "$%.2f".format(it) } ?: "$0.00"
            Text(
                text = precioFormateado,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
            if (categoria.isNotBlank() || stock.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = listOfNotNull(
                        categoria.takeIf { it.isNotBlank() },
                        stock.toIntOrNull()?.let { stringResource(R.string.stock_count, it) }
                    ).joinToString(" • "),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (descripcion.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = descripcion,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
internal fun EditProductFormFields(
    nombre: String,
    onNombreChange: (String) -> Unit,
    descripcion: String,
    onDescripcionChange: (String) -> Unit,
    precio: String,
    onPrecioChange: (String) -> Unit,
    categoria: String,
    onCategoriaChange: (String) -> Unit,
    stock: String,
    onStockChange: (String) -> Unit,
    agruparPrecioYStock: Boolean,
    textoBoton: String = stringResource(R.string.actualizar),
    onGuardar: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = nombre,
            onValueChange = onNombreChange,
            label = { Text(stringResource(R.string.nombre_producto)) },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = descripcion,
            onValueChange = onDescripcionChange,
            label = { Text(stringResource(R.string.descripcion)) },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3,
            maxLines = 5
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (agruparPrecioYStock) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = precio,
                    onValueChange = onPrecioChange,
                    label = { Text(stringResource(R.string.precio)) },
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = stock,
                    onValueChange = onStockChange,
                    label = { Text(stringResource(R.string.stock)) },
                    modifier = Modifier.weight(1f)
                )
            }
        } else {
            OutlinedTextField(
                value = precio,
                onValueChange = onPrecioChange,
                label = { Text(stringResource(R.string.precio)) },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = stock,
                onValueChange = onStockChange,
                label = { Text(stringResource(R.string.stock)) },
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = categoria,
            onValueChange = onCategoriaChange,
            label = { Text(stringResource(R.string.categoria)) },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onGuardar,
            modifier = Modifier.fillMaxWidth(),
            enabled = nombre.isNotBlank()
        ) {
            Text(textoBoton)
        }
    }
}