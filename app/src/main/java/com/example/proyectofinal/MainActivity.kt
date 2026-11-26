package com.example.proyectofinal

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.compose.setContent

sealed class Routes(val ruta: String) {
    object Carrito : Routes("carrito")
    object Vender : Routes("vender")
    object Pedidos : Routes("pedidos")
    object Ajustes : Routes("ajustes")
}

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TiendaApp()
        }
    }
}
