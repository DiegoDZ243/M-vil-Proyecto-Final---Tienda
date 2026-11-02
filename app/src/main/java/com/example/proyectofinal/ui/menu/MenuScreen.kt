package com.example.proyectofinal.ui.menu

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.material3.SnackbarHost
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.proyectofinal.R
import com.example.proyectofinal.data.model.Producto
import com.example.proyectofinal.ui.TiendaViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MenuScreen(
    viewModel: TiendaViewModel,
    onCartClick: () -> Unit,
    onOpenMenu: () -> Unit,
    onProductClick: (Int) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.carritoError) {
        if (uiState.carritoError != null) {
            snackbarHostState.showSnackbar(
                message = uiState.carritoError!!,
                actionLabel = "OK"
            )
            viewModel.limpiarErrorCarrito()
        }
    }

    Scaffold(
        topBar = {
            MenuScreenTopAppBar(
                cartCount = uiState.shoppingCart.sumOf { it.cantidad },
                onMenuClick = onOpenMenu,
                onCartClick = onCartClick
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        if(uiState.isLoading){
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ){
                CircularProgressIndicator(
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(50.dp)
                )
            }
        }else {
            LazyColumn(
                contentPadding = innerPadding
            ) {
                items(uiState.productos.filter { it.activo }) { producto ->
                    ProductItem(
                        product = producto,
                        modifier = Modifier.padding(dimensionResource(R.dimen.padding_small)),
                        onAgregarCarrito = { viewModel.agregarAlCarrito(producto) },
                        onProductClick = { onProductClick(producto.id) }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MenuScreenTopAppBar(
    onMenuClick: () -> Unit,
    onCartClick: () -> Unit,
    cartCount: Int,
    modifier: Modifier = Modifier
) {
    CenterAlignedTopAppBar(
        navigationIcon = {
            IconButton(onClick = onMenuClick) {
                Icon(imageVector = Icons.Default.Menu, contentDescription = "Abrir menú")
            }
        },
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(
                    modifier = Modifier.size(64.dp).padding(8.dp),
                    painter = painterResource(R.drawable.tiend_logo),
                    contentDescription = null
                )
                Text(text = stringResource(R.string.app_name), style = MaterialTheme.typography.titleLarge)
            }
        },
        actions = {
            IconButton(onClick = onCartClick) {
                BadgedBox(
                    badge = {
                        // Solo mostramos la burbuja si hay más de 0 productos
                        if (cartCount > 0) {
                            Badge {
                                Text(text = cartCount.toString())
                            }
                        }
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.ShoppingCart,
                        contentDescription = "Carrito"
                    )
                }
            }
        },
        modifier = modifier
    )
}

@Composable
fun ProductInformation(
    productName: String,
    productPrice: Double,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(text = productName, style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(top = dimensionResource(R.dimen.padding_small)))
        Text(text = "$%.2f".format(productPrice), style = MaterialTheme.typography.bodyLarge)
    }
}

@Composable
fun ProductImage(
    productImage: String,
    modifier: Modifier = Modifier
) {
    AsyncImage(
        model = productImage, // Se usa 'model' en lugar de 'painter' con AsyncImage
        contentDescription = null,
        modifier = modifier
            .fillMaxWidth()
            .size(180.dp)
            .clip(MaterialTheme.shapes.small),
        contentScale = ContentScale.Fit
    )
}

@Composable
fun ProductItem(
    product: Producto,
    modifier: Modifier = Modifier,
    onAgregarCarrito: () -> Unit,
    onProductClick: () -> Unit
) {
    Card(
        modifier = modifier.fillMaxWidth().padding(vertical = 8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        onClick = onProductClick
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            ProductImage(product.imagen,
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .clip(MaterialTheme.shapes.medium))
            Spacer(modifier = Modifier.height(12.dp))
            ProductInformation(productName = product.nombre, productPrice = product.precio, modifier = Modifier.fillMaxWidth())
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "Envío gratis", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.secondary)
                Button(
                    onClick = onAgregarCarrito,
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    shape = MaterialTheme.shapes.small
                ) {
                    Text("Añadir", style = MaterialTheme.typography.labelLarge)
                }
            }
        }
    }
}