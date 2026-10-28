package com.example.proyectofinal.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.proyectofinal.ui.AppViewModelProvider
import com.example.proyectofinal.ui.menu.MenuDestination
import com.example.proyectofinal.ui.menu.MenuScreen
import com.example.proyectofinal.ui.menu.MenuViewModel
import com.example.proyectofinal.ui.carrito.ShoppingCartDestination
import com.example.proyectofinal.ui.carrito.ShoppingCartScreen

@Composable
fun TiendaNavHost(
    navController: NavHostController,
    viewModel: MenuViewModel = viewModel(factory = AppViewModelProvider.Factory),
    modifier: Modifier=Modifier
){
    NavHost(
        navController=navController,
        startDestination = MenuDestination.route,
        modifier = modifier
    ){
        composable(route = MenuDestination.route){
            MenuScreen(
                viewModel=viewModel,
                navigateToShoppingCart = {navController.navigate(ShoppingCartDestination.route)}
            )
        }
        composable(route = ShoppingCartDestination.route){
            ShoppingCartScreen(
                viewModel=viewModel,
                onNavigateBack = {navController.navigateUp()})
        }
    }
}