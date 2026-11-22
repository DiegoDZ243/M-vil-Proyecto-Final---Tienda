package com.example.proyectofinal.ui.pedidos

import com.example.proyectofinal.data.model.Pedido

data class OrdenesUiState (
    val pedidos: List<Pedido> = emptyList(),
    val numPedidos: Int = 0,
    val isLoading: Boolean = true
)