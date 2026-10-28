package com.example.proyectofinal

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.proyectofinal.ui.navigation.TiendaNavHost

@Composable
fun TiendaApp(navController: NavHostController= rememberNavController()){
    TiendaNavHost(navController=navController)
}