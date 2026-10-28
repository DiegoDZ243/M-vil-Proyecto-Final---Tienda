package com.example.proyectofinal.ui.menu

import com.example.proyectofinal.data.model.Producto
import com.example.proyectofinal.data.model.ProductoCarrito

data class TiendaUiState(
    val username: String = "",
    val shoppingCart: List<ProductoCarrito> = emptyList(),
    val productos: List<Producto> = emptyList()
)