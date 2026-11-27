package com.example.proyectofinal.ui.mispublicaciones

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.inventory.data.ProductsRepository
import com.example.proyectofinal.data.model.Producto
import com.example.proyectofinal.data.model.service.AccountService
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class PublicacionesViewModel(
    private val productsRepository: ProductsRepository,
    private val accountService: AccountService
) : ViewModel() {

    private val estadoSeleccion = MutableStateFlow(PublicacionesUiState())

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<PublicacionesUiState> = accountService.currentUser
        .flatMapLatest { usuario ->
            val activeUid = usuario?.uid.orEmpty().ifEmpty { accountService.currentUserId }
            combine(
                productsRepository.getAllProductsStream(),
                estadoSeleccion
            ) { productos, currentState ->
                val misProductos = productos.filter {
                    it.vendedorId == activeUid || (activeUid.isEmpty() && it.vendedorId == "Usuario")
                }
                currentState.copy(
                    productos = misProductos,
                    isLoading = false
                )
            }
        }
        .catch { e ->
            Log.e("PublicacionesViewModel", "Error al cargar publicaciones: ${e.message}")
            emit(PublicacionesUiState(productos = emptyList(), isLoading = false))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = PublicacionesUiState(isLoading = true)
        )

    fun seleccionarProducto(producto: Producto) {
        estadoSeleccion.update {
            it.copy(
                productoSeleccionadoId = producto.id,
                mostrandoLista = false
            )
        }
    }

    fun regresarALista() {
        estadoSeleccion.update {
            it.copy(mostrandoLista = true)
        }
    }

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
                if (productoActual != null) {
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

    fun eliminarPublicacion(producto: Producto, onComplete: (Boolean) -> Unit = {}) {
        viewModelScope.launch {
            try {
                productsRepository.deleteProduct(producto)
                onComplete(true)
            } catch (e: Exception) {
                Log.e("PublicacionesViewModel", "Error al eliminar publicación: ${e.message}")
                onComplete(false)
            }
        }
    }
}