package com.example.proyectofinal.ui.menu

import com.example.proyectofinal.data.model.Producto
import com.example.proyectofinal.data.model.ProductoCarrito
import com.example.proyectofinal.data.model.Pedido

data class TiendaUiState(
    val username: String = "",
    val shoppingCart: List<ProductoCarrito> = emptyList(),
    val productos: List<Producto> = emptyList(),
    val productosVendidos: List<Producto> = emptyList(),
    val pedidos: List<Pedido> = emptyList()
)