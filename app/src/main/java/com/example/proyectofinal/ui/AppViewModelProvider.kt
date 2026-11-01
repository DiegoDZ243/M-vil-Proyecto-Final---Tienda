package com.example.proyectofinal.ui

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.lifecycle.viewmodel.initializer
import com.example.proyectofinal.TiendaApplication

object AppViewModelProvider{
    val Factory=viewModelFactory{
        initializer{
            TiendaViewModel(
                tiendaApplication().container.productsRepository
            )
        }
    }
}

fun CreationExtras.tiendaApplication(): TiendaApplication =
    (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as TiendaApplication)