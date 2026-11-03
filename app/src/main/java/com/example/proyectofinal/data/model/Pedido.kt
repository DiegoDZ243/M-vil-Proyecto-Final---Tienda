package com.example.proyectofinal.data.model

import java.util.Date

data class Pedido(
    val id: Int,
    val fecha: Date,
    val estado: String,
    val total: Double,
    val items: List<ProductoCarrito>
)
