package com.example.proyectofinal.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.rememberDrawerState
import androidx.compose.material3.DrawerValue
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.proyectofinal.ui.menu.BarraLateral
import com.example.proyectofinal.ui.menu.TiendaViewModel
import com.example.proyectofinal.ui.menu.MenuScreen
import com.example.proyectofinal.ui.carrito.ShoppingCartScreen
import kotlinx.coroutines.launch

@Composable
fun MainAppContainer() {
    val navController = rememberNavController()
    val viewModel: TiendaViewModel = viewModel()
    val uiState by viewModel.uiState.collectAsState()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            BarraLateral(
                username = uiState.username,
                onItemClick = { item ->
                    println("Navegando a: $item")
                    scope.launch { drawerState.close() }
                }
            )
        }
    ) {
        NavHost(
            navController = navController,
            startDestination = "menu"
        ) {
            composable("menu") {
                MenuScreen(
                    viewModel = viewModel,
                    onCartClick = {
                        navController.navigate("cart")
                    },
                    onOpenMenu = {
                        scope.launch { drawerState.open() }
                    }
                )
            }
            composable("cart") {
                ShoppingCartScreen(
                    viewModel = viewModel,
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }
        }
    }
}
