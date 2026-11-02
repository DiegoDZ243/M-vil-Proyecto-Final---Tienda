package com.example.proyectofinal.ui

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.inventory.data.ProductsRepository
import com.example.proyectofinal.data.model.Pedido
import com.example.proyectofinal.data.model.Producto
import com.example.proyectofinal.data.model.ProductoCarrito
import com.example.proyectofinal.data.repositories.orders.FirebaseOrdersRepository
import com.example.proyectofinal.data.repositories.orders.OrdersRepository
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
    private val productsRepository: ProductsRepository,
    private val ordersRepository: OrdersRepository
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

    // 2. Estado expuesto a la UI (Combina Firebase + Local)
    val uiState: StateFlow<TiendaUiState> = combine(
        productsRepository.getAllProductsStream(), // Flujo de Firebase
        _uiState                                   // Flujo local
    ) { productosFirebase, currentState ->
        // Actualiza el estado con los productos nuevos de la base de datos
        currentState.copy(
            productos = productosFirebase,
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000), // TIMEOUT_MILLIS
        initialValue = TiendaUiState()
    )


    fun agregarAlCarrito(producto: Producto) {
        // Validación de stock antes de agregar al carrito
        val stockActual = _uiState.value.productos.find { it.id == producto.id }?.stock ?: 0
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

            currentState.copy(shoppingCart = currentCart)
        }
    }

    fun cambiarCantidad(productoId: Int, delta: Int) {
        if (delta > 0) {
            // Si incrementamos, reutilizamos agregarAlCarrito para validar stock
            val producto = _uiState.value.productos.find { it.id == productoId }
            if (producto != null) {
                agregarAlCarrito(producto)
            }
        } else {
            // Si decrementamos, permitimos bajar cantidad (sin validación de stock)
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
        imagen: Uri?
    ) {
        // Se necesita una corrutina para emplear las funciones de tipo suspend de Firebase
        viewModelScope.launch {
            try {
                // Se genera el ID para el nuevo producto
                val currentState = uiState.value
                val nuevoId = (currentState.productos.maxOfOrNull { it.id } ?: 0) + 1
                var imagenUrl= ""

                if(imagen!=null) {
                    imagenUrl = productsRepository.uploadProductImage(imagen)
                }
                // TODO: Cuando se implemente FirebaseAuth, reemplazar username por el UID del usuario autenticado
                val nuevoProducto = Producto(
                    id = nuevoId,
                    nombre = nombre,
                    descripcion = descripcion,
                    precio = precio,
                    categoria = categoria,
                    stock = stock,
                    imagen = imagenUrl,
                    vendedorId = _uiState.value.username
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

        viewModelScope.launch {
            try{
                val currentState=uiState.value
                val nuevoId=(currentState.pedidos.maxOfOrNull { it.id } ?: 0) + 1
                val total = currentState.shoppingCart.sumOf { it.producto.precio * it.cantidad }

                val nuevoPedido = Pedido(
                    id = nuevoId,
                    fecha = Date(),
                    estado = "Pendiente",
                    total = total,
                    items = currentState.shoppingCart.toList()
                )
                ordersRepository.insertOrder(nuevoPedido)

                _uiState.update { state ->
                    state.copy(
                        pedidos = state.pedidos + nuevoPedido,
                        shoppingCart = emptyList()
                    )
                }

            }
            catch(e: Exception){
                Log.e("TiendaViewModel", "Error al guardar el pedido en Firebase: ${e.message}")
            }

        }
    }

    fun limpiarErrorCarrito() {
        _uiState.update { it.copy(carritoError = null) }
    }
}