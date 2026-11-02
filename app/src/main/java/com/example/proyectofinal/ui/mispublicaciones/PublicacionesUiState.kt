package com.example.proyectofinal.ui.mispublicaciones

import com.example.proyectofinal.data.model.Producto

data class PublicacionesUiState(
    val productos: List<Producto> = emptyList(),
    val isLoading: Boolean = true
)
