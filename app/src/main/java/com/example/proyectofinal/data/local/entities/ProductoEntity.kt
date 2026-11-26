package com.example.proyectofinal.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.proyectofinal.data.model.Producto

@Entity(tableName = "productos")
data class ProductoEntity(
    @PrimaryKey val id: Int,
    val nombre: String,
    val descripcion: String,
    val precio: Double,
    val categoria: String,
    val stock: Int,
    val imagen: String,
    val activo: Boolean,
    val calificacion: Double,
    val numReviews: Int,
    val vendedorId: String,
    val pendienteSincronizar: Boolean = false,
    val eliminadoPendiente: Boolean = false
)

// Mappers: Entity <-> Modelo de dominio
fun ProductoEntity.toDomain(): Producto = Producto(
    id = id,
    nombre = nombre,
    descripcion = descripcion,
    precio = precio,
    categoria = categoria,
    stock = stock,
    imagen = imagen,
    activo = activo,
    calificacion = calificacion,
    numReviews = numReviews,
    vendedorId = vendedorId
)

fun Producto.toEntity(
    pendienteSincronizar: Boolean = false,
    eliminadoPendiente: Boolean = false
): ProductoEntity = ProductoEntity(
    id = id,
    nombre = nombre,
    descripcion = descripcion,
    precio = precio,
    categoria = categoria,
    stock = stock,
    imagen = imagen,
    activo = activo,
    calificacion = calificacion,
    numReviews = numReviews,
    vendedorId = vendedorId,
    pendienteSincronizar = pendienteSincronizar,
    eliminadoPendiente = eliminadoPendiente
)