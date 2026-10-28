package com.example.proyectofinal.ui.menu

import androidx.lifecycle.ViewModel
import com.example.proyectofinal.data.model.Producto
import com.example.proyectofinal.data.model.ProductoCarrito
import com.example.proyectofinal.data.model.productos
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class TiendaViewModel: ViewModel() {
    private val _uiState = MutableStateFlow(
        TiendaUiState(
            username = "Usuario",
            productos = productos
        )
    )
    val uiState: StateFlow<TiendaUiState> = _uiState.asStateFlow()

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
}