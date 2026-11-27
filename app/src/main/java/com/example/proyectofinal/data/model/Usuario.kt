package com.example.proyectofinal.data.model

data class Usuario(
    val uid: String = "",
    val nombre: String = "",
    val correo: String = "",
    val calle: String = "",
    val numero: String = "",
    val colonia: String = "",
    val codigoPostal: String = "",
    val carrito: List<ProductoCarrito> = emptyList()
)
