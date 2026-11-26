package com.example.proyectofinal.ui.auth

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AuthViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    fun login(usuarioOcorreo: String, password: String) {
        val errorValidacion = validarLogin(usuarioOcorreo, password)
        if (errorValidacion != null) {
            _uiState.update { it.copy(error = errorValidacion) }
            return
        }

        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true, error = null) }
                Log.d("AuthViewModel", "Login intent: usuario=$usuarioOcorreo (auth pendiente)")

                // Integrar FirebaseAuth en login()

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = "Auth no implementado todavía"
                    )
                }
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Error inesperado en login: ${e.message}")
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = "Ocurrió un error inesperado. Intenta de nuevo."
                    )
                }
            }
        }
    }

    fun register(
        nombre: String,
        correo: String,
        password: String,
        confirmarPassword: String,
        calle: String,
        numero: String,
        colonia: String,
        codigoPostal: String
    ) {
        val errorValidacion = validarRegistro(
            nombre = nombre,
            correo = correo,
            password = password,
            confirmarPassword = confirmarPassword,
            calle = calle,
            numero = numero,
            colonia = colonia,
            codigoPostal = codigoPostal
        )
        if (errorValidacion != null) {
            _uiState.update { it.copy(error = errorValidacion) }
            return
        }

        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true, error = null) }
                Log.d("AuthViewModel", "Register intent: correo=$correo")

                // Integrar FirebaseAuth en register()

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = ""
                    )
                }
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Error inesperado en register: ${e.message}")
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = "Ocurrió un error inesperado. Intenta de nuevo."
                    )
                }
            }
        }
    }

    fun limpiarError() {
        _uiState.update { it.copy(error = null) }
    }

    private fun validarLogin(usuarioOcorreo: String, password: String): String? = when {
        usuarioOcorreo.isBlank() -> "Ingresa tu usuario o correo"
        password.isBlank() -> "Ingresa tu contraseña"
        else -> null
    }

    private fun validarRegistro(
        nombre: String,
        correo: String,
        password: String,
        confirmarPassword: String,
        calle: String,
        numero: String,
        colonia: String,
        codigoPostal: String
    ): String? {
        val correoLimpio = correo.trim()
        val codigoPostalLimpio = codigoPostal.trim()

        return when {
            nombre.isBlank() -> "Ingresa tu nombre completo"
            !correoLimpio.contains("@") || !correoLimpio.contains(".") ->
                "Ingresa un correo electrónico válido"
            password.length < 6 -> "La contraseña debe tener al menos 6 caracteres"
            password != confirmarPassword -> "Las contraseñas no coinciden"
            calle.isBlank() -> "Ingresa tu calle"
            numero.isBlank() -> "Ingresa tu número exterior"
            colonia.isBlank() -> "Ingresa tu colonia"
            codigoPostalLimpio.length != 5 || !codigoPostalLimpio.all { it.isDigit() } ->
                "El código postal debe tener 5 dígitos"
            else -> null
        }
    }
}
