package com.example.proyectofinal.data.repositories.orders

import com.example.proyectofinal.data.model.Pedido
import com.example.proyectofinal.data.model.Producto
import kotlinx.coroutines.flow.Flow

interface OrdersRepository {
    fun getAllOrdersStream(): Flow<List<Pedido>>
    fun getOrderStream(id: Int): Flow<Pedido?>
    suspend fun insertOrder(order: Pedido)
    suspend fun deleteOrder(order: Pedido)
    suspend fun updateOrder(order: Pedido)
}