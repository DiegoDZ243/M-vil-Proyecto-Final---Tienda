package com.example.proyectofinal.ui.vender

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.proyectofinal.R
import com.example.proyectofinal.ui.TiendaViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateProductScreen(
    isExpandedWidth: Boolean,
    viewModel: TiendaViewModel = viewModel(),
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var nombre by remember { mutableStateOf("") }
    var descripcion by remember { mutableStateOf("") }
    var precio by remember { mutableStateOf("") }
    var categoria by remember { mutableStateOf("") }
    var stock by remember { mutableStateOf("") }
    var imageUri by remember { mutableStateOf<Uri?>(null) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri -> imageUri = uri }
    )

    val onPickImage = {
        photoPickerLauncher.launch(
            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
        )
    }

    val onGuardarProducto = {
        val p = precio.toDoubleOrNull() ?: 0.0
        val s = stock.toIntOrNull() ?: 0
        viewModel.crearProducto(nombre, descripcion, p, categoria, s, imageUri)
        onNavigateBack()
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(R.string.crear_producto)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = null
                        )
                    }
                }
            )
        },
        modifier = modifier
    ) { innerPadding ->
        val imagenAMostrar: Any = imageUri ?: ""
        val textoSubir = stringResource(R.string.subir_imagen)
        val textoGuardar = stringResource(R.string.guardar)

        if (isExpandedWidth) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    ProductImageSelector(
                        imagenAMostrar = imagenAMostrar,
                        onPickImage = onPickImage,
                        textoPlaceholder = textoSubir,
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
                        textoBoton = textoGuardar,
                        onGuardar = onGuardarProducto
                    )
                }
            }
        } else {
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
                    textoPlaceholder = textoSubir,
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
                    textoBoton = textoGuardar,
                    onGuardar = onGuardarProducto
                )
            }
        }
    }
}