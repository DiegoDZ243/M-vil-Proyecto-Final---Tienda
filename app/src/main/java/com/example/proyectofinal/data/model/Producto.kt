package com.example.proyectofinal.data.model

import androidx.compose.ui.res.stringResource
import com.example.proyectofinal.R

data class Producto(
    val id: Int,
    val nombre: Int,
    val descripcion: String,
    val precio: Double,
    val categoria: String,
    val stock: Int,
    val imagen: Int
)

val productos = listOf(
    Producto(1, R.string.producto_1, "Playera negra de algodón premium", 299.99, "Ropa", 50, R.drawable.playera_negra),
    Producto(2, R.string.producto_2, "Sudadera gris con capucha", 599.99, "Ropa", 30, R.drawable.sudadera_gris),
    Producto(3, R.string.producto_3, "Pantalón de mezclilla clásico", 749.99, "Ropa", 25, R.drawable.pantalon_mezclilla),
    Producto(4, R.string.producto_4, "Gorra deportiva ajustable", 249.99, "Accesorios", 100, R.drawable.gorra_deportiva),
    Producto(5, R.string.producto_5, "Tenis blancos deportivos", 999.99, "Calzado", 20, R.drawable.tenis_blancos)
)