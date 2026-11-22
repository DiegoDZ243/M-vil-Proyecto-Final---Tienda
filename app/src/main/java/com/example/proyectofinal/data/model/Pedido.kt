package com.example.proyectofinal.data.model

import java.util.Date

data class Pedido(
    val id: Int = 0,
    val fecha: Date = Date(),
    val estado: String = "",
    val usuarioId: String="",
    val total: Double = 0.0,
    val items: List<ProductoCarrito> = emptyList()
)
