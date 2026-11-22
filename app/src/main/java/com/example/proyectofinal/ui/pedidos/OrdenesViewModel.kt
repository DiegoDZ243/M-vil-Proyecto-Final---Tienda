package com.example.proyectofinal.ui.pedidos

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.proyectofinal.data.repositories.orders.OrdersRepository
import com.example.proyectofinal.ui.menu.TiendaUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

class OrdenesViewModel(
    private val ordersRepository: OrdersRepository,
    private val usuarioActual:String
): ViewModel() {

    private val estadoUi = MutableStateFlow(
        OrdenesUiState(
            pedidos = emptyList(),
            numPedidos = 0,
            isLoading = true,
            usuarioActual=usuarioActual
        )
    )

    val uiState:StateFlow<OrdenesUiState> = combine(
        ordersRepository.getOrdersByUserStream(usuarioActual),
        estadoUi
    ) { ordenesFirebase, currentState ->

        currentState.copy(
            pedidos = ordenesFirebase,
            numPedidos = ordenesFirebase.size,
            isLoading = false
        )

    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = OrdenesUiState()
    )

}