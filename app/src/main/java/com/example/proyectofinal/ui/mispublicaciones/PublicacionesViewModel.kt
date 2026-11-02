package com.example.proyectofinal.ui.mispublicaciones

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.inventory.data.ProductsRepository
import com.example.proyectofinal.data.model.Producto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class PublicacionesViewModel(
    private val productsRepository: ProductsRepository,
    private val usernameActual: String
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        PublicacionesUiState(
            productos = emptyList(),
            isLoading = true
        )
    )

    val uiState: StateFlow<PublicacionesUiState> = _uiState.asStateFlow()

    init {
        cargarPublicaciones()
    }

    fun cargarPublicaciones() {
        viewModelScope.launch {
            try {
                productsRepository.getAllProductsStream().collect { productos ->
                    val misProductos = productos.filter { it.vendedorId == usernameActual }
                    _uiState.update { it.copy(productos = misProductos, isLoading = false) }
                }
            } catch (e: Exception) {
                Log.e("PublicacionesViewModel", "Error al cargar publicaciones: ${e.message}")
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun editarPublicacion(producto: Producto) {
        viewModelScope.launch {
            try {
                productsRepository.updateProduct(producto)
            } catch (e: Exception) {
                Log.e("PublicacionesViewModel", "Error al editar publicación: ${e.message}")
            }
        }
    }

    fun desactivarPublicacion(producto: Producto) {
        viewModelScope.launch {
            try {
                val productoActualizado = producto.copy(activo = false)
                productsRepository.updateProduct(productoActualizado)
            } catch (e: Exception) {
                Log.e("PublicacionesViewModel", "Error al desactivar publicación: ${e.message}")
            }
        }
    }

    fun activarPublicacion(producto: Producto) {
        viewModelScope.launch {
            try {
                val productoActualizado = producto.copy(activo = true)
                productsRepository.updateProduct(productoActualizado)
            } catch (e: Exception) {
                Log.e("PublicacionesViewModel", "Error al activar publicación: ${e.message}")
            }
        }
    }
}
