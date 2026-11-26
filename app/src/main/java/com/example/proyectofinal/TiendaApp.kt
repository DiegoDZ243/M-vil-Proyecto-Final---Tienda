package com.example.proyectofinal

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.proyectofinal.ui.components.OfflineBanner
import com.example.proyectofinal.ui.navigation.TiendaNavHost

@Composable
fun TiendaApp(navController: NavHostController = rememberNavController()) {
    val context = LocalContext.current
    val networkMonitor = remember {
        (context.applicationContext as TiendaApplication).container.networkMonitor
    }

    val isOnline by networkMonitor.isOnlineFlow.collectAsState(initial = networkMonitor.isConnected())

    Column(modifier = Modifier.fillMaxSize()) {
        OfflineBanner(
            isOffline = !isOnline,
            modifier = Modifier.statusBarsPadding()
        )

        TiendaNavHost(
            navController = navController,
            modifier = Modifier.weight(1f)
        )
    }
}