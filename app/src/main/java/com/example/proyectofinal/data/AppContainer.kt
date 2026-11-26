package com.example.proyectofinal.data

import android.content.Context
import com.example.inventory.data.ProductsRepository
import com.example.proyectofinal.data.model.service.AccountService
import com.example.proyectofinal.data.model.service.AccountServiceImpl
import com.example.proyectofinal.data.repositories.orders.FirebaseOrdersRepository
import com.example.proyectofinal.data.repositories.orders.OrdersRepository
import com.example.proyectofinal.data.repositories.products.FirebaseProductsRepository
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.database.database

interface AppContainer {
    val productsRepository: ProductsRepository
    val ordersRepository: OrdersRepository
    val accountService: AccountService
}

class DefaultAppContainer(private val context: Context) : AppContainer {
    private val database = Firebase.database
    private val auth = Firebase.auth

    private val productsDatabaseReference by lazy {
        database.getReference("productos")
    }

    private val ordersDatabseReference by lazy {
        database.getReference("pedidos")
    }

    private val usersDatabaseReference by lazy {
        database.getReference("usuarios")
    }

    override val productsRepository: ProductsRepository by lazy {
        FirebaseProductsRepository(productsDatabaseReference)
    }

    override val ordersRepository: OrdersRepository by lazy {
        FirebaseOrdersRepository(ordersDatabseReference)
    }

    override val accountService: AccountService by lazy {
        AccountServiceImpl(auth, usersDatabaseReference)
    }
}
