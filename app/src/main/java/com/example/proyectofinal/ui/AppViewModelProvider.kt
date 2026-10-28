package com.example.proyectofinal.ui

import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.lifecycle.viewmodel.initializer
import com.example.proyectofinal.ui.menu.MenuViewModel

object AppViewModelProvider{
    val Factory=viewModelFactory{
        initializer{
            MenuViewModel()
        }
    }
}