package com.example.proyectofinal.ui.auth

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.proyectofinal.ui.AppViewModelProvider
import kotlinx.coroutines.launch

@Composable
fun RegisterScreen(
    viewModel: AuthViewModel = viewModel(factory = AppViewModelProvider.Factory),
    onNavigateToLogin: () -> Unit,
    onRegisterSuccess: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var nombre by remember { mutableStateOf("") }
    var correo by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmarPassword by remember { mutableStateOf("") }
    var calle by remember { mutableStateOf("") }
    var numero by remember { mutableStateOf("") }
    var colonia by remember { mutableStateOf("") }
    var codigoPostal by remember { mutableStateOf("") }

    LaunchedEffect(uiState.error) {
        uiState.error?.let { error ->
            snackbarHostState.showSnackbar(message = error, actionLabel = "OK")
            viewModel.limpiarError()
        }
    }

    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            viewModel.resetSuccess()
            onRegisterSuccess()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Crear cuenta",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text = "Completa tus datos para registrarte",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.height(24.dp))

            AuthTextField(
                value = nombre,
                onValueChange = { nombre = it },
                label = "Nombre completo"
            )

            Spacer(Modifier.height(12.dp))

            AuthTextField(
                value = correo,
                onValueChange = { correo = it },
                label = "Correo electrónico",
                keyboardType = KeyboardType.Email
            )

            Spacer(Modifier.height(12.dp))

            AuthTextField(
                value = password,
                onValueChange = { password = it },
                label = "Contraseña",
                isPassword = true
            )

            Spacer(Modifier.height(12.dp))

            AuthTextField(
                value = confirmarPassword,
                onValueChange = { confirmarPassword = it },
                label = "Confirmar contraseña",
                isPassword = true
            )

            Spacer(Modifier.height(12.dp))

            AuthTextField(
                value = calle,
                onValueChange = { calle = it },
                label = "Calle"
            )

            Spacer(Modifier.height(12.dp))

            AuthTextField(
                value = numero,
                onValueChange = { numero = it },
                label = "Número exterior",
                keyboardType = KeyboardType.Number
            )

            Spacer(Modifier.height(12.dp))

            AuthTextField(
                value = colonia,
                onValueChange = { colonia = it },
                label = "Colonia"
            )

            Spacer(Modifier.height(12.dp))

            AuthTextField(
                value = codigoPostal,
                onValueChange = { codigoPostal = it },
                label = "Código postal",
                keyboardType = KeyboardType.Number,
                maxLength = 5
            )

            Spacer(Modifier.height(24.dp))

            Button(
                onClick = {
                    val mensajeError = mensajeErrorRegistro(
                        nombre = nombre,
                        correo = correo,
                        password = password,
                        confirmarPassword = confirmarPassword,
                        calle = calle,
                        numero = numero,
                        colonia = colonia,
                        codigoPostal = codigoPostal
                    )
                    if (mensajeError != null) {
                        scope.launch {
                            snackbarHostState.showSnackbar(message = mensajeError, actionLabel = "OK")
                        }
                    } else {
                        viewModel.register(
                            nombre = nombre.trim(),
                            correo = correo.trim(),
                            password = password,
                            confirmarPassword = confirmarPassword,
                            calle = calle.trim(),
                            numero = numero.trim(),
                            colonia = colonia.trim(),
                            codigoPostal = codigoPostal.trim()
                        )
                    }
                },
                enabled = !uiState.isLoading,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Crear cuenta")
                }
            }

            Spacer(Modifier.height(8.dp))

            TextButton(onClick = onNavigateToLogin) {
                Text("¿Ya tienes cuenta? Inicia sesión")
            }
        }
    }
}

private fun mensajeErrorRegistro(
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
