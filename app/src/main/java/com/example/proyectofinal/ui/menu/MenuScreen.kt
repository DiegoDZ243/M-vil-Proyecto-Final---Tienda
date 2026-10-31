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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.proyectofinal.R
import com.example.proyectofinal.data.model.Producto

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MenuScreen(
    viewModel: TiendaViewModel,
    onCartClick: () -> Unit,
    onOpenMenu: () -> Unit,
    onProductClick: (Int) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            MenuScreenTopAppBar(
                cartCount = uiState.shoppingCart.sumOf { it.cantidad },
                onMenuClick = onOpenMenu,
                onCartClick = onCartClick
            )
        }
    ) { innerPadding ->
        LazyColumn(
            contentPadding = innerPadding
        ) {
            items(uiState.productos) { producto ->
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
                Icon(imageVector = Icons.Default.ShoppingCart, contentDescription = "Carrito")
                if (cartCount > 0) {
                    Text(text = cartCount.toString(), modifier = Modifier.padding(start = 4.dp), style = MaterialTheme.typography.labelSmall)
                }
            }
        },
        modifier = modifier
    )
}

@Composable
fun ProductInformation(
    @StringRes productName: Int,
    productPrice: Double,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(text = stringResource(productName), style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(top = dimensionResource(R.dimen.padding_small)))
        Text(text = "$%.2f".format(productPrice), style = MaterialTheme.typography.bodyLarge)
    }
}

@Composable
fun ProductImage(
    @DrawableRes productImage: Int,
    modifier: Modifier = Modifier
) {
    Image(
        modifier = modifier.fillMaxWidth().size(180.dp).clip(MaterialTheme.shapes.small),
        contentScale = ContentScale.Crop,
        painter = painterResource(productImage),
        contentDescription = null
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
            ProductImage(product.imagen, modifier = Modifier.fillMaxWidth().clip(MaterialTheme.shapes.medium))
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