package com.example.proyectofinal.ui

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.inventory.data.ProductsRepository
import com.example.proyectofinal.data.model.EstadoPedido
import com.example.proyectofinal.data.model.Pedido
import com.example.proyectofinal.data.model.Producto
import com.example.proyectofinal.data.model.ProductoCarrito
import com.example.proyectofinal.data.model.service.AccountService
import com.example.proyectofinal.data.repositories.orders.OrdersRepository
import com.example.proyectofinal.ui.menu.TiendaUiState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class TiendaViewModel(
    private val productsRepository: ProductsRepository,
    private val ordersRepository: OrdersRepository,
    private val accountService: AccountService
) : ViewModel() {
    private val _uiState = MutableStateFlow(
        TiendaUiState(
            username = "Usuario",
            shoppingCart = emptyList(),
            productosVendidos = emptyList(),
            pedidos = emptyList(),
            isLoading = true
        )
    )

    init {
        viewModelScope.launch {
            accountService.currentUser.collect { usuario ->
                val nombreMostrar = usuario?.nombre?.ifBlank { null }
                    ?: usuario?.correo
                    ?: if (accountService.currentUserId.isNotEmpty()) accountService.currentUserId else "Usuario"
                _uiState.update {
                    it.copy(
                        username = nombreMostrar,
                        shoppingCart = usuario?.carrito.orEmpty(),
                        direccionPerfil = if (usuario != null && usuario.calle.isNotBlank()) {
                            "${usuario.calle} #${usuario.numero}, ${usuario.colonia}, C.P. ${usuario.codigoPostal}"
                        } else ""
                    )
                }
            }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<TiendaUiState> = combine(
        productsRepository.getAllProductsStream()
            .catch { e ->
                Log.e("TiendaViewModel", "Error al leer productos: ${e.message}")
                emit(emptyList())
            },
        _uiState
    ) { productosLocales, currentState ->
        val carritoActualizado = currentState.shoppingCart.map { itemCarrito ->
            val productoFresco = productosLocales.find { it.id == itemCarrito.producto.id }
            if (productoFresco != null) {
                itemCarrito.copy(producto = productoFresco)
            } else {
                itemCarrito
            }
        }

        currentState.copy(
            productos = productosLocales,
            shoppingCart = carritoActualizado,
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = TiendaUiState()
    )

    fun agregarAlCarrito(producto: Producto) {
        val stockActual = uiState.value.productos.find { it.id == producto.id }?.stock ?: 0
        val cantidadEnCarrito = _uiState.value.shoppingCart.find { it.producto.id == producto.id }?.cantidad ?: 0
        if (cantidadEnCarrito >= stockActual) {
            _uiState.update { it.copy(carritoError = "No puedes agregar más unidades de las disponibles (stock: $stockActual)") }
            return
        }

        _uiState.update { currentState ->
            val currentCart = currentState.shoppingCart.toMutableList()
            val itemInCart = currentCart.find { it.producto.id == producto.id }

            if (itemInCart != null) {
                val index = currentCart.indexOf(itemInCart)
                currentCart[index] = itemInCart.copy(cantidad = itemInCart.cantidad + 1)
            } else {
                currentCart.add(ProductoCarrito(producto = producto, cantidad = 1))
            }

            currentState.copy(
                shoppingCart = currentCart,
                carritoError = null,
                mensaje = "\"${producto.nombre}\" agregado al carrito"
            )
        }
        persistirCarrito()
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
                    return@update currentState.copy(
                        shoppingCart = currentCart,
                        carritoError = null,
                        mensaje = "\"${item.producto.nombre}\" eliminado del carrito"
                    )
                } else if (nuevaCantidad > item.producto.stock) {
                    return@update currentState.copy(
                        carritoError = "Solo hay ${item.producto.stock} unidades disponibles."
                    )
                } else {
                    currentCart[index] = item.copy(cantidad = nuevaCantidad)
                    return@update currentState.copy(
                        shoppingCart = currentCart,
                        carritoError = null
                    )
                }
            }

            currentState
        }
        persistirCarrito()
    }

    fun vaciarCarrito() {
        _uiState.update { it.copy(shoppingCart = emptyList(), mensaje = "Carrito vaciado") }
        persistirCarrito()
    }

    fun crearProducto(
        nombre: String,
        descripcion: String,
        precio: Double,
        categoria: String,
        stock: Int,
        imagen: Uri?
    ) {
        viewModelScope.launch {
            try {
                val currentState = uiState.value
                val nuevoId = (currentState.productos.maxOfOrNull { it.id } ?: 0) + 1
                var imagenUrl = ""

                if (imagen != null) {
                    imagenUrl = productsRepository.uploadProductImage(imagen)
                }
                val usuarioUid = accountService.currentUserId
                val vendedorIdFinal = if (usuarioUid.isNotEmpty()) usuarioUid else _uiState.value.username

                val nuevoProducto = Producto(
                    id = nuevoId,
                    nombre = nombre,
                    descripcion = descripcion,
                    precio = precio,
                    categoria = categoria,
                    stock = stock,
                    imagen = imagenUrl,
                    vendedorId = vendedorIdFinal
                )

                productsRepository.insertProduct(nuevoProducto)

                _uiState.update { state ->
                    state.copy(
                        productos = state.productos + nuevoProducto,
                        mensaje = "Publicación creada correctamente"
                    )
                }
            } catch (e: Exception) {
                Log.e("TiendaViewModel", "Error al guardar el producto en Firebase: ${e.message}")
            }
        }
    }

    fun crearPedido(
        direccion: String,
        metodoPago: String,
        onResultado: (Int?) -> Unit = {}
    ) {
        viewModelScope.launch {
            try {
                val estadoActual = uiState.value
                val carrito = estadoActual.shoppingCart
                if (carrito.isEmpty()) {
                    onResultado(null)
                    return@launch
                }

                val sinStock = carrito.firstOrNull { it.cantidad > it.producto.stock }
                if (sinStock != null) {
                    _uiState.update {
                        it.copy(carritoError = "\"${sinStock.producto.nombre}\" ya no tiene suficiente stock (disponible: ${sinStock.producto.stock})")
                    }
                    onResultado(null)
                    return@launch
                }

                val pedidosExistentes = ordersRepository.getAllOrdersStream().first()
                val nuevoId = (pedidosExistentes.maxOfOrNull { it.id } ?: 0) + 1
                val total = carrito.sumOf { it.producto.precio * it.cantidad }
                val usuarioUid = accountService.currentUserId
                val usuarioIdFinal = if (usuarioUid.isNotEmpty()) usuarioUid else estadoActual.username

                val nuevoPedido = Pedido(
                    id = nuevoId,
                    fecha = System.currentTimeMillis(),
                    estado = EstadoPedido.PENDIENTE,
                    usuarioId = usuarioIdFinal,
                    total = total,
                    direccion = direccion,
                    metodoPago = metodoPago,
                    items = carrito.toList()
                )
                ordersRepository.insertOrder(nuevoPedido)

                carrito.forEach { item ->
                    val nuevoStock = item.producto.stock - item.cantidad
                    productsRepository.updateProduct(
                        item.producto.copy(stock = if (nuevoStock >= 0) nuevoStock else 0)
                    )
                }

                _uiState.update { state ->
                    state.copy(
                        pedidos = state.pedidos + nuevoPedido,
                        shoppingCart = emptyList()
                    )
                }
                persistirCarrito()
                onResultado(nuevoId)
            } catch (e: Exception) {
                Log.e("TiendaViewModel", "Error al guardar el pedido en Firebase: ${e.message}")
                _uiState.update { it.copy(carritoError = "No se pudo completar la compra. Intenta de nuevo.") }
                onResultado(null)
            }
        }
    }

    fun signOut(onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            try {
                accountService.signOut()
                _uiState.update {
                    it.copy(
                        username = "Usuario",
                        shoppingCart = emptyList(),
                        pedidos = emptyList()
                    )
                }
                onComplete()
            } catch (e: Exception) {
                Log.e("TiendaViewModel", "Error al cerrar sesión: ${e.message}")
            }
        }
    }

    private fun persistirCarrito() {
        val uid = accountService.currentUserId
        if (uid.isBlank()) return
        val carrito = _uiState.value.shoppingCart
        viewModelScope.launch {
            try {
                accountService.guardarCarrito(uid, carrito)
            } catch (e: Exception) {
                Log.e("TiendaViewModel", "Error al persistir carrito: ${e.message}")
            }
        }
    }

    fun limpiarMensaje() {
        _uiState.update { it.copy(mensaje = null) }
    }

    fun limpiarErrorCarrito() {
        _uiState.update { it.copy(carritoError = null) }
    }

    fun cargarProductoParaEdicion(id: Int) {
        viewModelScope.launch {
            try {
                productsRepository.getProductStream(id).collect { producto ->
                    if (producto != null) {
                        _uiState.update { it.copy(productoEnEdicion = producto) }
                    }
                }
            } catch (e: Exception) {
                Log.e("TiendaViewModel", "Error al cargar producto para edición: ${e.message}")
            }
        }
    }

    fun limpiarProductoEnEdicion() {
        _uiState.update { it.copy(productoEnEdicion = null) }
    }
}
