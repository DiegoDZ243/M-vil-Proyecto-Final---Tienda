package com.example.proyectofinal.data.model

data class ProductoCarrito(
    val producto: Producto = Producto(),
    val cantidad: Int = 0,
    // RF-20: calificación (1 a 5) que el usuario dio a este producto en el pedido; 0 = sin calificar
    val calificacion: Int = 0
)
