package com.example.proyectofinal.ui.pedidos

import com.example.proyectofinal.data.model.Pedido

data class OrdenesUiState(
    val pedidos: List<Pedido> = emptyList(),
    val numPedidos: Int = 0,
    val isLoading: Boolean = true,
    val usuarioActual: String = "",
    val mensaje: String? = null,
    val pedidoSeleccionadoId: Int? = 0,
    val mostrandoLista: Boolean = true
) {
    val pedidoSeleccionado: Pedido?
        get() = pedidos.find { it.id == pedidoSeleccionadoId }
            ?: pedidos.firstOrNull()
}