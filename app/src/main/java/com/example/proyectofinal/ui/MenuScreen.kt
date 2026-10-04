package com.example.proyectofinal.ui

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
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Card
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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

@Composable
fun MenuScreen() {
    Scaffold(
        topBar = {
            MenuScreenTopAppBar()
        }
    ) { innerPadding ->

        LazyColumn(
            contentPadding = innerPadding
        ) {
            items(productos) { producto ->

                ProductItem(
                    product = producto,
                    modifier = Modifier.padding(
                        dimensionResource(R.dimen.padding_small)
                    )
                )
            }
        }
    }
}

//Barra superior del menú principal
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MenuScreenTopAppBar(
    modifier: Modifier = Modifier
) {
    CenterAlignedTopAppBar(
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
            IconButton(
                onClick = { }
            ) {
                Icon(
                    imageVector = Icons.Default.ShoppingCart,
                    contentDescription = "Carrito"
                )
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
    modifier: Modifier = Modifier
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
    modifier: Modifier = Modifier
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
    modifier: Modifier = Modifier,
//    onAgregarCarrito: () -> Unit = {}
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
                onClick = {},//onAgregarCarrito
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Agregar al carrito")
            }
        }
    }
}