package com.example.proyectofinal.data

import android.content.Context
import com.example.inventory.data.ProductsRepository
import com.example.proyectofinal.data.local.AppDatabase
import com.example.proyectofinal.data.local.repositories.RoomProductsRepository
import com.example.proyectofinal.data.model.service.AccountService
import com.example.proyectofinal.data.model.service.AccountServiceImpl
import com.example.proyectofinal.data.network.NetworkMonitor
import com.example.proyectofinal.data.repositories.orders.FirebaseOrdersRepository
import com.example.proyectofinal.data.repositories.orders.OrdersRepository
import com.example.proyectofinal.data.repositories.products.FirebaseProductsRepository
import com.example.proyectofinal.data.repositories.products.OfflineFirstProductsRepository
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.database.database
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

interface AppContainer {
    val productsRepository: ProductsRepository
    val ordersRepository: OrdersRepository
    val accountService: AccountService
    val networkMonitor: NetworkMonitor
}

class DefaultAppContainer(private val context: Context) : AppContainer {
    private val firebaseDatabase = Firebase.database
    private val auth = Firebase.auth

    // Instancias locales para Room y monitoreo de red
    private val localDatabase by lazy { AppDatabase.getDatabase(context) }
    override val networkMonitor by lazy { NetworkMonitor(context) }
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val productsDatabaseReference by lazy {
        firebaseDatabase.getReference("productos")
    }

    private val ordersDatabseReference by lazy {
        firebaseDatabase.getReference("pedidos")
    }

    private val usersDatabaseReference by lazy {
        firebaseDatabase.getReference("usuarios")
    }

    private val firebaseProductsRepository by lazy {
        FirebaseProductsRepository(productsDatabaseReference)
    }

    private val roomProductsRepository by lazy {
        RoomProductsRepository(localDatabase.productoDao())
    }

    override val productsRepository: ProductsRepository by lazy {
        OfflineFirstProductsRepository(
            localRepository = roomProductsRepository,
            remoteRepository = firebaseProductsRepository,
            networkMonitor = networkMonitor,
            context = context
        ).also { offlineRepo ->
            // Sincroniza automáticamente los cambios pendientes en cuanto regrese el internet
            applicationScope.launch {
                networkMonitor.isOnlineFlow.collect { isOnline ->
                    if (isOnline) {
                        offlineRepo.sincronizarPendientes()
                    }
                }
            }
        }
    }

    override val ordersRepository: OrdersRepository by lazy {
        FirebaseOrdersRepository(ordersDatabseReference)
    }

    override val accountService: AccountService by lazy {
        AccountServiceImpl(auth, usersDatabaseReference)
    }
}