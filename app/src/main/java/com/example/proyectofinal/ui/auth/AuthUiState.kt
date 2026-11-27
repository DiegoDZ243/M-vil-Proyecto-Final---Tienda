package com.example.proyectofinal.ui.auth

data class AuthUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSuccess: Boolean = false,
    val mensaje: String? = null
)
