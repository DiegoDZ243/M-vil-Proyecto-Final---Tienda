package com.example.proyectofinal.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.inventory.data.ProductsRepository
import com.example.proyectofinal.data.model.Pedido
import com.example.proyectofinal.data.model.Producto
import com.example.proyectofinal.data.model.ProductoCarrito
import com.example.proyectofinal.ui.menu.TiendaUiState
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Date

class TiendaViewModel(
    private val productsRepository: ProductsRepository // 1. Recibimos el repositorio
) : ViewModel() {
    private val _uiState = MutableStateFlow(
        TiendaUiState(
            username = "Usuario",
            shoppingCart = emptyList(),
            productosVendidos = emptyList(),
            pedidos = emptyList()
        )
    )

    // 2. Estado expuesto a la UI (Combina Firebase + Local)
    val uiState: StateFlow<TiendaUiState> = combine(
        productsRepository.getAllProductsStream(), // Flujo de Firebase
        _uiState                                   // Flujo local
    ) { productosFirebase, currentState ->
        // Actualiza el estado con los productos nuevos de la base de datos
        currentState.copy(
            productos = productosFirebase
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000), // TIMEOUT_MILLIS
        initialValue = TiendaUiState()
    )


    fun agregarAlCarrito(producto: Producto) {
        _uiState.update { currentState ->
            val currentCart = currentState.shoppingCart.toMutableList()
            val itemInCart = currentCart.find { it.producto.id == producto.id }

            if (itemInCart != null) {
                val index = currentCart.indexOf(itemInCart)
                currentCart[index] = itemInCart.copy(cantidad = itemInCart.cantidad + 1)
            } else {
                currentCart.add(ProductoCarrito(producto = producto, cantidad = 1))
            }

            currentState.copy(shoppingCart = currentCart)
        }
    }

    fun cambiarCantidad(productoId: Int, delta: Int) {
        _uiState.update { currentState ->
            val currentCart = currentState.shoppingCart.toMutableList()
            val index = currentCart.indexOfFirst { it.producto.id == productoId }

            if (index != -1) {
                val item = currentCart[index]
                val nuevaCantidad = item.cantidad + delta
                if (nuevaCantidad <= 0) {
                    currentCart.removeAt(index)
                } else {
                    currentCart[index] = item.copy(cantidad = nuevaCantidad)
                }
            }
            currentState.copy(shoppingCart = currentCart)
        }
    }

    fun vaciarCarrito() {
        _uiState.update { it.copy(shoppingCart = emptyList()) }
    }

    fun crearProducto(
        nombre: String,
        descripcion: String,
        precio: Double,
        categoria: String,
        stock: Int,
        imagen: Int
    ) {
        // Se necesita una corrutina para emplear las funciones de tipo suspend de Firebase
        viewModelScope.launch {
            try {
                // Se genera el ID para el nuevo producto
                val currentState = uiState.value
                val nuevoId = (currentState.productos.maxOfOrNull { it.id } ?: 0) + 1

                val nuevoProducto = Producto(
                    id = nuevoId,
                    nombre = nombre,
                    descripcion = descripcion,
                    precio = precio,
                    categoria = categoria,
                    stock = stock,
                    imagen = imagen
                )

                // Se guarda en la base de datos con ayuda de repository
                productsRepository.insertProduct(nuevoProducto)

                // Con esto se actualiza la UI
                _uiState.update { state ->
                    state.copy(
                        productos = state.productos + nuevoProducto
                    )
                }
            } catch (e: Exception) {
                // En caso de que haya una excepción
                Log.e("TiendaViewModel", "Error al guardar el producto en Firebase: ${e.message}")
            }
        }
    }

    fun crearPedido() {
        _uiState.update { currentState ->
            if (currentState.shoppingCart.isEmpty()) {
                return@update currentState
            }

            val nuevoId = (currentState.pedidos.maxOfOrNull { it.id } ?: 0) + 1
            val total = currentState.shoppingCart.sumOf { it.producto.precio * it.cantidad }
            val nuevoPedido = Pedido(
                id = nuevoId,
                fecha = Date(),
                estado = "Pendiente",
                total = total,
                items = currentState.shoppingCart.toList()
            )

            currentState.copy(
                pedidos = currentState.pedidos + nuevoPedido,
                shoppingCart = emptyList()
            )
        }
    }
}