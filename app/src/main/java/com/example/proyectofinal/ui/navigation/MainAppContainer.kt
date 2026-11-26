package com.example.proyectofinal.ui.navigation

import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.proyectofinal.ui.AppViewModelProvider
import com.example.proyectofinal.ui.TiendaViewModel
import com.example.proyectofinal.ui.auth.LoginScreen
import com.example.proyectofinal.ui.auth.RegisterScreen
import com.example.proyectofinal.ui.carrito.CheckoutScreen
import com.example.proyectofinal.ui.carrito.ShoppingCartScreen
import com.example.proyectofinal.ui.detalle.ProductDetailScreen
import com.example.proyectofinal.ui.menu.BarraLateral
import com.example.proyectofinal.ui.menu.MenuScreen
import com.example.proyectofinal.ui.mispublicaciones.MisPublicacionesScreen
import com.example.proyectofinal.ui.pedidos.OrdersScreen
import com.example.proyectofinal.ui.vender.CreateProductScreen
import com.example.proyectofinal.ui.vender.EditProductScreen
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

@Composable
fun MainAppContainer() {
    val navController = rememberNavController()
    val viewModel: TiendaViewModel = viewModel(factory = AppViewModelProvider.Factory)
    val uiState by viewModel.uiState.collectAsState()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val isLoggedIn = FirebaseAuth.getInstance().currentUser != null
    val startDestination = if (isLoggedIn) "menu" else "login"
    val gesturesEnabled = currentRoute != "login" && currentRoute != "register"

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = gesturesEnabled,
        drawerContent = {
            BarraLateral(
                username = uiState.username,
                onItemClick = { item ->
                    when (item) {
                        "Vender" -> navController.navigate("create_product")
                        "MisPublicaciones" -> navController.navigate("mis_publicaciones")
                        "Pedidos" -> navController.navigate("orders")
                        "Ajustes" -> { }
                        "CerrarSesion" -> {
                            viewModel.signOut {
                                navController.navigate("login") {
                                    popUpTo(0) { inclusive = true }
                                }
                            }
                        }
                    }
                    scope.launch { drawerState.close() }
                }
            )
        }
    ) {
        NavHost(
            navController = navController,
            startDestination = startDestination
        ) {
            composable("menu") {
                MenuScreen(
                    viewModel = viewModel,
                    onCartClick = {
                        navController.navigate("cart")
                    },
                    onOpenMenu = {
                        scope.launch { drawerState.open() }
                    },
                    onProductClick = { productId ->
                        navController.navigate("product_detail/$productId")
                    }
                )
            }
            composable("cart") {
                ShoppingCartScreen(
                    viewModel = viewModel,
                    onNavigateBack = {
                        navController.popBackStack()
                    },
                    onProceedToCheckout = {
                        navController.navigate("checkout")
                    }
                )
            }
            composable("checkout") {
                CheckoutScreen(
                    viewModel = viewModel,
                    onNavigateBack = {
                        navController.popBackStack()
                    },
                    onVerPedidos = {
                        navController.navigate("orders") {
                            popUpTo("menu")
                        }
                    },
                    onSeguirComprando = {
                        navController.popBackStack("menu", inclusive = false)
                    }
                )
            }
            composable("create_product") {
                CreateProductScreen(
                    viewModel = viewModel,
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }
            composable("edit_product/{productId}") { backStackEntry ->
                val productId = backStackEntry.arguments?.getString("productId")?.toIntOrNull()
                EditProductScreen(
                    onNavigateBack = {
                        navController.popBackStack()
                    },
                    productId = productId
                )
            }
            composable("orders") {
                OrdersScreen(
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }
            composable("mis_publicaciones") {
                MisPublicacionesScreen(
                    onNavigateBack = {
                        navController.popBackStack()
                    },
                    onNavigateToEdit = { productId ->
                        navController.navigate("edit_product/$productId")
                    },
                    onNavigateToDetail = { productId ->
                        navController.navigate("product_detail/$productId")
                    }
                )
            }
            composable("product_detail/{productId}") { backStackEntry ->
                val productId = backStackEntry.arguments?.getString("productId")?.toIntOrNull() ?: 0
                ProductDetailScreen(
                    productId = productId,
                    viewModel = viewModel,
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }
            composable("login") {
                LoginScreen(
                    onNavigateToRegister = {
                        navController.navigate("register")
                    },
                    onLoginSuccess = {
                        navController.navigate("menu") {
                            popUpTo("login") { inclusive = true }
                        }
                    }
                )
            }
            composable("register") {
                RegisterScreen(
                    onNavigateToLogin = {
                        navController.popBackStack()
                    },
                    onRegisterSuccess = {
                        navController.navigate("menu") {
                            popUpTo("login") { inclusive = true }
                        }
                    }
                )
            }
        }
    }
}
