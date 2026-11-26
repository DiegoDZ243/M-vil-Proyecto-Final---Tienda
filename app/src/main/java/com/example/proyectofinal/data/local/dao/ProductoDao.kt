package com.example.proyectofinal.data.local.dao

import androidx.room.*
import com.example.proyectofinal.data.local.entities.ProductoEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductoDao {
    @Query("SELECT * FROM productos WHERE eliminadoPendiente = 0")
    fun getAllProducts(): Flow<List<ProductoEntity>>

    @Query("SELECT * FROM productos WHERE id = :id AND eliminadoPendiente = 0")
    fun getProductById(id: Int): Flow<ProductoEntity?>

    @Query("SELECT * FROM productos WHERE pendienteSincronizar = 1")
    suspend fun getPendingProducts(): List<ProductoEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(producto: ProductoEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(productos: List<ProductoEntity>)

    @Query("UPDATE productos SET pendienteSincronizar = 0 WHERE id = :id")
    suspend fun markAsSynced(id: Int)

    @Query("DELETE FROM productos WHERE id = :id")
    suspend fun deleteById(id: Int)

    // Limpia en Room los productos que ya no existen en Firebase (y no están pendientes)
    @Query("DELETE FROM productos WHERE pendienteSincronizar = 0 AND id NOT IN (:idsActivosRemotos)")
    suspend fun deleteRemovedFromRemote(idsActivosRemotos: List<Int>)
}