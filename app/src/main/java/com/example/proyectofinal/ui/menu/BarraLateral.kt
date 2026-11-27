package com.example.proyectofinal.ui.menu

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Store
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.proyectofinal.R
import com.example.proyectofinal.ui.components.LanguageSelectorDialog

@Composable
fun BarraLateral(
    username: String,
    onItemClick: (String) -> Unit
) {
    var mostrarDialogoIdiomas by remember { mutableStateOf(false) }

    if (mostrarDialogoIdiomas) {
        LanguageSelectorDialog(
            onDismissRequest = { mostrarDialogoIdiomas = false }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(R.drawable.usuario),
                contentDescription = "Foto de perfil",
                modifier = Modifier.size(90.dp)
            )
            Spacer(modifier = Modifier.size(12.dp))
            Text(
                text = username,
                style = MaterialTheme.typography.titleLarge
            )
        }

        NavigationDrawerItem(
            label = { Text(stringResource(R.string.tienda)) },
            selected = false,
            onClick = { onItemClick("Tienda") },
            icon = { Icon(imageVector = Icons.Default.Storefront, contentDescription = null) },
            modifier = Modifier.padding(horizontal = 12.dp)
        )

        NavigationDrawerItem(
            label = { Text(stringResource(R.string.vender)) },
            selected = false,
            onClick = { onItemClick("Vender") },
            icon = { Icon(imageVector = Icons.Default.Store, contentDescription = null) },
            modifier = Modifier.padding(horizontal = 12.dp)
        )

        NavigationDrawerItem(
            label = { Text(stringResource(R.string.mis_publicaciones)) },
            selected = false,
            onClick = { onItemClick("MisPublicaciones") },
            icon = { Icon(imageVector = Icons.AutoMirrored.Filled.List, contentDescription = null) },
            modifier = Modifier.padding(horizontal = 12.dp)
        )

        NavigationDrawerItem(
            label = { Text(stringResource(R.string.mis_pedidos)) },
            selected = false,
            onClick = { onItemClick("Pedidos") },
            icon = { Icon(imageVector = Icons.Default.ShoppingBag, contentDescription = null) },
            modifier = Modifier.padding(horizontal = 12.dp)
        )

        NavigationDrawerItem(
            label = { Text(stringResource(R.string.ajustes)) },
            selected = false,
            onClick = {
                mostrarDialogoIdiomas = true
                onItemClick("Ajustes")
            },
            icon = { Icon(imageVector = Icons.Default.Settings, contentDescription = null) },
            modifier = Modifier.padding(horizontal = 12.dp)
        )

        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp, horizontal = 12.dp))

        NavigationDrawerItem(
            label = { Text(stringResource(R.string.cerrar_sesion)) },
            selected = false,
            onClick = { onItemClick("CerrarSesion") },
            icon = { Icon(imageVector = Icons.AutoMirrored.Filled.ExitToApp, contentDescription = stringResource(R.string.cerrar_sesion)) },
            modifier = Modifier.padding(horizontal = 12.dp)
        )
    }
}