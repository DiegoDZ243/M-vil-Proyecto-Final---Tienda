package com.example.proyectofinal.ui.menu

import androidx.lifecycle.ViewModel
import com.example.proyectofinal.data.model.Producto
import com.example.proyectofinal.data.model.ProductoCarrito
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class MenuViewModel: ViewModel() {
    private val _uiState = MutableStateFlow(MenuUiState())
    val uiState: StateFlow<MenuUiState> = _uiState.asStateFlow()


    fun agregarAlCarrito(producto: Producto) {
        _uiState.update { currentState ->
            val carritoActualizado = currentState.shoppingCart.toMutableList()
            val indexExistente = carritoActualizado.indexOfFirst { it.id == producto.id }

            if (indexExistente != -1) {
                val itemActual = carritoActualizado[indexExistente]
                carritoActualizado[indexExistente] = itemActual.copy(cantidad = itemActual.cantidad + 1)
            } else {
                carritoActualizado.add(
                    ProductoCarrito(
                        id = producto.id,
                        nombre = producto.nombre,
                        precio = producto.precio,
                        imagen = producto.imagen,
                        cantidad = 1
                    )
                )
            }

            val nuevoTotal = carritoActualizado.size

            // 3. Emitir el nuevo estado
            currentState.copy(
                shoppingCart = carritoActualizado,
                shoppingCartTotal = nuevoTotal
            )
        }
    }


}