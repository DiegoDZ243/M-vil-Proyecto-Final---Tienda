package com.example.proyectofinal.ui.menu

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Card
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
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
import com.example.proyectofinal.data.model.productos
import com.example.proyectofinal.ui.BarraLateral
import com.example.proyectofinal.ui.navigation.NavigationDestination
import kotlinx.coroutines.launch

object MenuDestination : NavigationDestination {
    override val route = "home"
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MenuScreen(
    viewModel: MenuViewModel,
    navigateToShoppingCart : () -> Unit
) {
    val drawerState = rememberDrawerState(
        initialValue = DrawerValue.Closed
    )

    val scope = rememberCoroutineScope()
    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            BarraLateral(
                username = "Usuario"
            )
        }
    ) {

        Scaffold(
            topBar = {
                MenuScreenTopAppBar(
                    onMenuClick = {
                        scope.launch {
                            drawerState.open()
                        }
                    },
                    navigateToShoppingCart,
                    viewModel,
                )
            }
        ) { innerPadding ->

            LazyColumn(
                contentPadding = innerPadding
            ) {
                items(productos) { producto ->
                    ProductItem(
                        product = producto,
                        onAgregarCarrito = { p -> viewModel.agregarAlCarrito(p) },
                        modifier = Modifier.padding(
                            dimensionResource(R.dimen.padding_small)
                        )
                    )
                }
            }
        }
    }
}

//Barra superior del menú principal
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MenuScreenTopAppBar(
    onMenuClick: () -> Unit,
    onClickShoppingCart:()->Unit,
    viewModel: MenuViewModel,
    modifier: Modifier = Modifier.Companion
) {
    CenterAlignedTopAppBar(

        navigationIcon = {
            IconButton(
                onClick = onMenuClick
            ) {
                Icon(
                    imageVector = Icons.Default.Menu,
                    contentDescription = "Abrir menú"
                )
            }
        },

        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    modifier = Modifier
                        .size(64.dp)
                        .padding(8.dp),
                    painter = painterResource(R.drawable.tiend_logo),
                    contentDescription = null
                )

                Text(
                    text = stringResource(R.string.app_name),
                    style = MaterialTheme.typography.titleLarge
                )
            }
        },

        // Botón del carrito (Posteriormente agregar la
        // funcionalidad de mostrar cuantos productos hay en el carrito)
        actions = {
            IconButton(onClick = onClickShoppingCart) {
                BadgedBox(
                    badge = {
                        // Solo mostramos la burbuja si hay más de 0 productos
                        if (viewModel.uiState.collectAsState().value.shoppingCartTotal > 0) {
                            Badge {
                                Text(text = viewModel.uiState.collectAsState().value.shoppingCartTotal.toString())
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

//Composable que contiene la información del producto (precio)
@Composable
fun ProductInformation(
    @StringRes productName: Int,
    productPrice: Double,
    modifier: Modifier = Modifier.Companion
) {
    Column(
        modifier = modifier
    ) {
        Text(
            text = stringResource(productName),
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(
                top = dimensionResource(R.dimen.padding_small)
            )
        )

        Text(
            text = "$%.2f".format(productPrice),
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

//Composable que contiene y da formato a la imagen
@Composable
fun ProductImage(
    @DrawableRes productImage: Int,
    modifier: Modifier = Modifier.Companion
) {
    Image(
        modifier = modifier
            .fillMaxWidth()
            .size(180.dp)
            .clip(MaterialTheme.shapes.small),
        contentScale = ContentScale.Fit,
        painter = painterResource(productImage),
        contentDescription = null
    )
}

@Composable
fun ProductItem(
    product: Producto,
    modifier: Modifier = Modifier.Companion,
    onAgregarCarrito: (Producto) -> Unit
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {

            ProductImage(
                product.imagen,
                modifier = Modifier
                    .fillMaxWidth()
            )

            ProductInformation(
                productName = product.nombre,
                productPrice = product.precio,
                modifier = Modifier.padding(top = 8.dp)
            )

            Text(
                text = "Envío gratis",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 6.dp)
            )

            IconButton(
                onClick = { onAgregarCarrito(product) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Agregar al carrito")
            }
        }
    }
}