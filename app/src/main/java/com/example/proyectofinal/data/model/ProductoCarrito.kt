package com.example.proyectofinal.data.model

data class ProductoCarrito(
    val producto: Producto = Producto(),
    val cantidad: Int = 0
)
