package com.example.proyectofinal

import android.app.Application
import com.cloudinary.android.MediaManager
import com.example.proyectofinal.data.AppContainer
import com.example.proyectofinal.data.DefaultAppContainer
import com.google.firebase.FirebaseApp
import com.google.firebase.database.FirebaseDatabase

class TiendaApplication : Application() {

    lateinit var container: AppContainer

    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
        try {
            FirebaseDatabase.getInstance().setPersistenceEnabled(true)
        } catch (e: Exception) {
            // Ignorar si FirebaseDatabase ya fue instanciado previamente
        }

        container = DefaultAppContainer(this)

        val config = mapOf("cloud_name" to "haprc4vu")
        MediaManager.init(this, config)
    }
}
