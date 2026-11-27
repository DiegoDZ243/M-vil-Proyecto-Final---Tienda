package com.example.proyectofinal.ui.menu

import com.example.proyectofinal.data.model.Pedido
import com.example.proyectofinal.data.model.Producto
import com.example.proyectofinal.data.model.ProductoCarrito
import com.example.proyectofinal.data.model.Usuario

data class TiendaUiState(
    val username: String = "",
    val usuario: Usuario? = null,
    val shoppingCart: List<ProductoCarrito> = emptyList(),
    val productos: List<Producto> = emptyList(),
    val productosVendidos: List<Producto> = emptyList(),
    val pedidos: List<Pedido> = emptyList(),
    val isLoading: Boolean = true,
    val carritoError: String? = null,
    val mensaje: String? = null,
    val productoEnEdicion: Producto? = null
)
