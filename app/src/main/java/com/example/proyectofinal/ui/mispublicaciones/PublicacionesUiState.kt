package com.example.proyectofinal.ui.mispublicaciones

import com.example.proyectofinal.data.model.Producto

data class PublicacionesUiState(
    val productos: List<Producto> = emptyList(),
    val isLoading: Boolean = true,
    val productoSeleccionadoId: Int? = null,
    val mostrandoLista: Boolean = true
) {
    val productoSeleccionado: Producto?
        get() = productos.find { it.id == productoSeleccionadoId }
            ?: productos.firstOrNull()
}