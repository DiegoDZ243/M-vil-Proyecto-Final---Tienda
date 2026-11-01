package com.example.proyectofinal

import android.app.Application
import com.example.proyectofinal.data.AppContainer
import com.example.proyectofinal.data.DefaultAppContainer
import com.google.firebase.FirebaseApp // Asegúrate de importar esto

class TiendaApplication : Application() {

    lateinit var container: AppContainer

    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)

        // Pasamos 'this' como contexto al contenedor
        container = DefaultAppContainer(this)
    }
}