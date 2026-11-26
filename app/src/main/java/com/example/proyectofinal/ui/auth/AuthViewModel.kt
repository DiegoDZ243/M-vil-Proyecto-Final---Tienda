package com.example.proyectofinal.ui.auth

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.proyectofinal.data.model.Usuario
import com.example.proyectofinal.data.model.service.AccountService
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AuthViewModel(
    private val accountService: AccountService
) : ViewModel() {

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
                _uiState.update { it.copy(isLoading = true, error = null, isSuccess = false) }
                accountService.authenticate(usuarioOcorreo.trim(), password)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isSuccess = true,
                        error = null
                    )
                }
            } catch (e: FirebaseAuthInvalidUserException) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = "No existe ninguna cuenta registrada con este correo electrónico."
                    )
                }
            } catch (e: FirebaseAuthInvalidCredentialsException) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = "Contraseña o correo electrónico incorrectos."
                    )
                }
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Error en login: ${e.message}", e)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = e.localizedMessage ?: "Ocurrió un error inesperado al iniciar sesión."
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

        val nuevoUsuario = Usuario(
            nombre = nombre.trim(),
            correo = correo.trim(),
            calle = calle.trim(),
            numero = numero.trim(),
            colonia = colonia.trim(),
            codigoPostal = codigoPostal.trim()
        )

        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true, error = null, isSuccess = false) }
                accountService.signUp(nuevoUsuario, password)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isSuccess = true,
                        error = null
                    )
                }
            } catch (e: FirebaseAuthUserCollisionException) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = "Este correo electrónico ya está registrado."
                    )
                }
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Error en registro: ${e.message}", e)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = e.localizedMessage ?: "Ocurrió un error inesperado al registrarte."
                    )
                }
            }
        }
    }

    fun resetSuccess() {
        _uiState.update { it.copy(isSuccess = false) }
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
