package com.example.proyectofinal.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Store
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.proyectofinal.R

@Composable
fun BarraLateral(
    username: String
) {
    ModalDrawerSheet(
        modifier = Modifier.width(280.dp)
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

            Spacer(
                modifier = Modifier.size(12.dp)
            )

            Text(
                text = username,
                style = MaterialTheme.typography.titleLarge
            )
        }

        NavigationDrawerItem(
            label = {
                Text("Comenzar a vender")
            },
            selected = false,
            onClick = {},
            icon = {
                Icon(
                    imageVector = Icons.Default.Store,
                    contentDescription = null
                )
            },
            modifier = Modifier.padding(horizontal = 12.dp)
        )

        NavigationDrawerItem(
            label = {
                Text("Mis pedidos")
            },
            selected = false,
            onClick = {},
            icon = {
                Icon(
                    imageVector = Icons.Default.ShoppingBag,
                    contentDescription = null
                )
            },
            modifier = Modifier.padding(horizontal = 12.dp)
        )

        NavigationDrawerItem(
            label = {
                Text("Ajustes")
            },
            selected = false,
            onClick = {},
            icon = {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = null
                )
            },
            modifier = Modifier.padding(horizontal = 12.dp)
        )
    }
}