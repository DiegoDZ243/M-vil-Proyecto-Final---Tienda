package com.example.proyectofinal.ui.menu

import com.example.proyectofinal.data.model.Producto
import com.example.proyectofinal.data.model.ProductoCarrito

data class MenuUiState(
    val username: String = "",
    val shoppingCart: List<ProductoCarrito> = emptyList(),
    val shoppingCartTotal:Int = 0,
    val productos: List<Producto> = emptyList()
)