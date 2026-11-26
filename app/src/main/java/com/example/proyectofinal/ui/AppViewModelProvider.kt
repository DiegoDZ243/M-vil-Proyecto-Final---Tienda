package com.example.proyectofinal.ui

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.proyectofinal.TiendaApplication
import com.example.proyectofinal.ui.auth.AuthViewModel
import com.example.proyectofinal.ui.mispublicaciones.PublicacionesViewModel
import com.example.proyectofinal.ui.pedidos.OrdenesViewModel

object AppViewModelProvider {
    val Factory = viewModelFactory {
        initializer {
            TiendaViewModel(
                tiendaApplication().container.productsRepository,
                tiendaApplication().container.ordersRepository,
                tiendaApplication().container.accountService
            )
        }

        initializer {
            val accountService = tiendaApplication().container.accountService
            val currentUserId = accountService.currentUserId.ifEmpty { "Usuario" }
            PublicacionesViewModel(
                tiendaApplication().container.productsRepository,
                currentUserId
            )
        }

        initializer {
            val accountService = tiendaApplication().container.accountService
            val currentUserId = accountService.currentUserId.ifEmpty { "Usuario" }
            OrdenesViewModel(
                tiendaApplication().container.ordersRepository,
                tiendaApplication().container.productsRepository,
                currentUserId
            )
        }

        initializer {
            AuthViewModel(
                tiendaApplication().container.accountService
            )
        }
    }
}

fun CreationExtras.tiendaApplication(): TiendaApplication =
    (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as TiendaApplication)
