package com.example.proyectofinal.data.local.repositories

import com.example.proyectofinal.data.local.dao.ProductoDao
import com.example.proyectofinal.data.local.entities.ProductoEntity
import com.example.proyectofinal.data.local.entities.toDomain
import com.example.proyectofinal.data.local.entities.toEntity
import com.example.proyectofinal.data.model.Producto
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RoomProductsRepository(private val productoDao: ProductoDao) {

    fun getAllProductsStream(): Flow<List<Producto>> =
        productoDao.getAllProducts().map { list -> list.map { it.toDomain() } }

    fun getProductStream(id: Int): Flow<Producto?> =
        productoDao.getProductById(id).map { it?.toDomain() }

    suspend fun saveProduct(
        producto: Producto,
        pendienteSincronizar: Boolean,
        eliminadoPendiente: Boolean = false
    ) {
        productoDao.insertProduct(
            producto.toEntity(
                pendienteSincronizar = pendienteSincronizar,
                eliminadoPendiente = eliminadoPendiente
            )
        )
    }

    suspend fun syncFromRemote(productosRemotos: List<Producto>) {
        val pendientesIds = productoDao.getPendingProducts().map { it.id }.toSet()

        // Solo sobrescribe los que no tengan cambios locales pendientes por subir
        val paraInsertar = productosRemotos
            .filter { it.id !in pendientesIds }
            .map { it.toEntity(pendienteSincronizar = false, eliminadoPendiente = false) }

        productoDao.insertAll(paraInsertar)

        // Si en Firebase se eliminó un producto desde otro dispositivo, lo borramos de Room
        if (productosRemotos.isNotEmpty()) {
            productoDao.deleteRemovedFromRemote(productosRemotos.map { it.id })
        }
    }

    suspend fun getPendingEntities(): List<ProductoEntity> =
        productoDao.getPendingProducts()

    suspend fun markAsSynced(id: Int) = productoDao.markAsSynced(id)

    suspend fun deleteLocalById(id: Int) = productoDao.deleteById(id)
}