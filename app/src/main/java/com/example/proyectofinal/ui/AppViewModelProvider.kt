package com.example.proyectofinal.ui

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.lifecycle.viewmodel.initializer
import com.example.proyectofinal.TiendaApplication
import com.example.proyectofinal.ui.mispublicaciones.PublicacionesViewModel
import com.example.proyectofinal.ui.pedidos.OrdenesViewModel

object AppViewModelProvider{
    val Factory=viewModelFactory{
        initializer{
            TiendaViewModel(
                tiendaApplication().container.productsRepository,
                tiendaApplication().container.ordersRepository
            )
        }

        initializer{
            PublicacionesViewModel(
                tiendaApplication().container.productsRepository,
                "Usuario"
            )
        }

        initializer {
            OrdenesViewModel(
                tiendaApplication().container.ordersRepository
            )
        }
    }
}

fun CreationExtras.tiendaApplication(): TiendaApplication =
    (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as TiendaApplication)