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
import androidx.compose.material3.Card
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
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
                    painter = painterResource(R.drawable.tenis_blancos),
                    contentDescription = null
                )

                Text(
                    text = stringResource(R.string.app_name),
                    style = MaterialTheme.typography.titleLarge
                )
            }
        },
        modifier = modifier
    )
}

@Composable
fun ProductImage(
    @DrawableRes productImage: Int,
    modifier: Modifier = Modifier
) {
    Image(
        modifier = modifier
            .size(dimensionResource(R.dimen.image_size))
            .padding(dimensionResource(R.dimen.padding_small))
            .clip(MaterialTheme.shapes.small),
        contentScale = ContentScale.Crop,
        painter = painterResource(productImage),
        contentDescription = null
    )
}

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

@Composable
fun ProductItem(
    product: Producto,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            ProductImage(product.imagen)

            ProductInformation(
                productName = product.nombre,
                productPrice = product.precio
            )
        }
    }
}