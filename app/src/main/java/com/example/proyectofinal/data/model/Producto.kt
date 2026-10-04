package com.example.proyectofinal.data.model

import androidx.compose.ui.res.stringResource
import com.example.proyectofinal.R

data class Producto(
    val id: Int,
    val nombre: Int,
    val precio: Double,
    val imagen: Int
)

val productos = listOf(
    Producto(1, R.string.producto_1, 299.99, R.drawable.playera_negra),
    Producto(2, R.string.producto_2, 599.99, R.drawable.sudadera_gris),
    Producto(3, R.string.producto_3, 749.99, R.drawable.pantalon_mezclilla),
    Producto(4, R.string.producto_4, 249.99, R.drawable.gorra_deportiva),
    Producto(5, R.string.producto_5, 999.99, R.drawable.tenis_blancos)
)