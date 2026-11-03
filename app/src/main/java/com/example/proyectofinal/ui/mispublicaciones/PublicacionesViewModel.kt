package com.example.proyectofinal.ui.mispublicaciones

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.inventory.data.ProductsRepository
import com.example.proyectofinal.data.model.Producto
import com.example.proyectofinal.ui.menu.TiendaUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class PublicacionesViewModel(
    private val productsRepository: ProductsRepository,
    private val usernameActual: String
) : ViewModel() {


    val uiState: StateFlow<PublicacionesUiState> = productsRepository.getAllProductsStream()
        .map { productos ->
            val misProductos = productos.filter { it.vendedorId == usernameActual }
            PublicacionesUiState(
                productos = misProductos,
                isLoading = false
            )
        }
        .catch { e ->
            Log.e("PublicacionesViewModel", "Error al cargar publicaciones: ${e.message}")
            emit(PublicacionesUiState(productos = emptyList(), isLoading = false))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000), // TIMEOUT_MILLIS
            initialValue = PublicacionesUiState(isLoading = true)
        )


    fun editarPublicacion(
        productoActual: Producto?,
        nuevoNombre: String,
        nuevaDescripcion: String,
        nuevoPrecio: Double,
        nuevaCategoria: String,
        nuevoStock: Int,
        nuevaImagenUri: Uri?,
        onComplete: () -> Unit
    ) {
        viewModelScope.launch {
            try {
                if(productoActual!=null) {

                    var imagenUrlFinal = productoActual.imagen

                    if (nuevaImagenUri != null) {
                        imagenUrlFinal = productsRepository.uploadProductImage(nuevaImagenUri)
                    }
                    val productoActualizado = productoActual.copy(
                        nombre = nuevoNombre,
                        descripcion = nuevaDescripcion,
                        precio = nuevoPrecio,
                        categoria = nuevaCategoria,
                        stock = nuevoStock,
                        imagen = imagenUrlFinal
                    )
                    productsRepository.updateProduct(productoActualizado)
                    onComplete()
                }

            } catch (e: Exception) {
                Log.e("TiendaViewModel", "Error al editar publicación: ${e.message}")
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
