package com.example.proyectofinal.ui

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class TiendaViewModel: ViewModel() {
    private val _uiState = MutableStateFlow(TiendaUiState())
    val uiState: StateFlow<TiendaUiState> = _uiState.asStateFlow()



}