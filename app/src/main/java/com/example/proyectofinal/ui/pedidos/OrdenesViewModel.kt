package com.example.proyectofinal.ui.pedidos

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.inventory.data.ProductsRepository
import com.example.proyectofinal.data.model.EstadoPedido
import com.example.proyectofinal.data.model.Pedido
import com.example.proyectofinal.data.model.estaDentroDeVentanaCancelacion
import com.example.proyectofinal.data.model.service.AccountService
import com.example.proyectofinal.data.repositories.orders.OrdersRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class OrdenesViewModel(
    private val ordersRepository: OrdersRepository,
    private val productsRepository: ProductsRepository,
    private val accountService: AccountService
) : ViewModel() {

    private val estadoUi = MutableStateFlow(OrdenesUiState())

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<OrdenesUiState> = accountService.currentUser
        .flatMapLatest { usuario ->
            val activeUid = usuario?.uid.orEmpty().ifEmpty { accountService.currentUserId }
            combine(
                ordersRepository.getOrdersByUserStream(activeUid)
                    .catch { e ->
                        Log.e("OrdenesViewModel", "Error al leer pedidos: ${e.message}")
                        emit(emptyList())
                    },
                estadoUi
            ) { ordenesFirebase, currentState ->
                val ordenados = ordenesFirebase.sortedByDescending { it.fecha }
                currentState.copy(
                    usuarioActual = activeUid,
                    pedidos = ordenados,
                    numPedidos = ordenados.size,
                    isLoading = false
                )
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = OrdenesUiState()
        )

    fun limpiarMensaje() {
        estadoUi.update { it.copy(mensaje = null) }
    }

    private fun mostrar(mensaje: String) {
        estadoUi.update { it.copy(mensaje = mensaje) }
    }

    fun cancelarPedido(pedido: Pedido) {
        viewModelScope.launch {
            try {
                if (pedido.estado != EstadoPedido.PENDIENTE) {
                    mostrar("Solo se pueden cancelar pedidos pendientes")
                    return@launch
                }
                if (!pedido.estaDentroDeVentanaCancelacion()) {
                    mostrar("No se puede cancelar: el pedido se realizó hace más de 6 horas")
                    return@launch
                }

                ordersRepository.updateOrder(pedido.copy(estado = EstadoPedido.CANCELADO))

                pedido.items.forEach { item ->
                    val productoActual = productsRepository.getProductStream(item.producto.id).first()
                    if (productoActual != null) {
                        productsRepository.updateProduct(
                            productoActual.copy(stock = productoActual.stock + item.cantidad)
                        )
                    }
                }
                mostrar("Pedido #${pedido.id} cancelado")
            } catch (e: Exception) {
                Log.e("OrdenesViewModel", "Error al cancelar pedido: ${e.message}")
                mostrar("No se pudo cancelar el pedido. Intenta de nuevo.")
            }
        }
    }

    fun finalizarPedido(pedido: Pedido) {
        viewModelScope.launch {
            try {
                if (pedido.estado != EstadoPedido.PENDIENTE) return@launch
                ordersRepository.updateOrder(pedido.copy(estado = EstadoPedido.FINALIZADO))
                mostrar("Pedido #${pedido.id} finalizado. Ya puedes calificar tus productos")
            } catch (e: Exception) {
                Log.e("OrdenesViewModel", "Error al finalizar pedido: ${e.message}")
                mostrar("No se pudo actualizar el pedido. Intenta de nuevo.")
            }
        }
    }

    fun calificarProducto(pedido: Pedido, productoId: Int, estrellas: Int) {
        viewModelScope.launch {
            try {
                if (pedido.estado != EstadoPedido.FINALIZADO || estrellas !in 1..5) return@launch
                val item = pedido.items.find { it.producto.id == productoId } ?: return@launch
                if (item.calificacion != 0) {
                    mostrar("Ya calificaste este producto")
                    return@launch
                }

                val productoActual = productsRepository.getProductStream(productoId).first()
                if (productoActual != null) {
                    val nuevoTotal = productoActual.numReviews + 1
                    val nuevoPromedio =
                        (productoActual.calificacion * productoActual.numReviews + estrellas) / nuevoTotal
                    productsRepository.updateProduct(
                        productoActual.copy(calificacion = nuevoPromedio, numReviews = nuevoTotal)
                    )
                }

                val itemsActualizados = pedido.items.map {
                    if (it.producto.id == productoId) it.copy(calificacion = estrellas) else it
                }
                ordersRepository.updateOrder(pedido.copy(items = itemsActualizados))
                mostrar("¡Gracias por calificar \"${item.producto.nombre}\"!")
            } catch (e: Exception) {
                Log.e("OrdenesViewModel", "Error al calificar: ${e.message}")
                mostrar("No se pudo guardar tu calificación. Intenta de nuevo.")
            }
        }
    }

    fun seleccionarPedido(pedido: Pedido) {
        estadoUi.update {
            it.copy(
                pedidoSeleccionadoId = pedido.id, // o pedido.id si es Int
                mostrandoLista = false
            )
        }
    }
    fun regresarALista() {
        estadoUi.update {
            it.copy(
                mostrandoLista = true
            )
        }
    }
}
