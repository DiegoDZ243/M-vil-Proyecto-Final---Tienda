package com.example.proyectofinal.data

import android.content.Context
import com.example.inventory.data.ProductsRepository
import com.example.proyectofinal.data.repositories.products.FirebaseProductsRepository
import com.google.firebase.Firebase
import com.google.firebase.database.database

interface AppContainer {
    val productsRepository: ProductsRepository
}

class DefaultAppContainer(private val context: Context) : AppContainer {

    // Obtiene automáticamente la instancia de tu base de datos por defecto
    private val database = Firebase.database

    // Accede a la referencia del nodo "productos" de forma limpia
    private val databaseReference by lazy {
        database.getReference("productos")
    }

    override val productsRepository: ProductsRepository by lazy {
        FirebaseProductsRepository(databaseReference)
    }
}
