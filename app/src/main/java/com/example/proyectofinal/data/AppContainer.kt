package com.example.proyectofinal.data

import android.content.Context
import com.example.inventory.data.ProductsRepository
import com.example.proyectofinal.data.repositories.orders.FirebaseOrdersRepository
import com.example.proyectofinal.data.repositories.orders.OrdersRepository
import com.example.proyectofinal.data.repositories.products.FirebaseProductsRepository
import com.google.firebase.Firebase
import com.google.firebase.database.database
import com.google.firebase.storage.storage

interface AppContainer {
    val productsRepository: ProductsRepository
    val ordersRepository: OrdersRepository
}

class DefaultAppContainer(private val context: Context) : AppContainer {
    private val database = Firebase.database

    // Accede a la referencia del nodo "productos" de forma limpia
    private val productsDatabaseReference by lazy {
        database.getReference("productos")
    }

    private val ordersDatabseReference by lazy {
        database.getReference("pedidos")
    }

    override val productsRepository: ProductsRepository by lazy {
        FirebaseProductsRepository(productsDatabaseReference)
    }

    override val ordersRepository: OrdersRepository by lazy{
        FirebaseOrdersRepository(ordersDatabseReference)
    }
}
