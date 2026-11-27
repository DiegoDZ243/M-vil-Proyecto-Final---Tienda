package com.example.proyectofinal.data.repositories.products

import android.content.Context
import android.net.Uri
import android.util.Log
import com.example.inventory.data.ProductsRepository
import com.example.proyectofinal.data.local.entities.toDomain
import com.example.proyectofinal.data.local.repositories.RoomProductsRepository
import com.example.proyectofinal.data.model.Producto
import com.example.proyectofinal.data.network.NetworkMonitor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

class OfflineFirstProductsRepository(
    private val localRepository: RoomProductsRepository,
    private val remoteRepository: FirebaseProductsRepository,
    private val networkMonitor: NetworkMonitor,
    private val context: Context
) : ProductsRepository {

    override fun getAllProductsStream(): Flow<List<Producto>> = channelFlow {
        launch {
            remoteRepository.getAllProductsStream()
                .catch { e -> Log.e("OfflineFirstProducts", "Error al escuchar remotos: ${e.message}") }
                .collect { productosFirebase ->
                    localRepository.syncFromRemote(productosFirebase)
                }
        }

        localRepository.getAllProductsStream().collect { productosLocales ->
            send(productosLocales)
        }
    }

    override fun getProductStream(id: Int): Flow<Producto?> =
        localRepository.getProductStream(id)

    override suspend fun insertProduct(product: Producto) {
        if (networkMonitor.isConnected()) {
            localRepository.saveProduct(product, pendienteSincronizar = false)
            try {
                remoteRepository.insertProduct(product)
            } catch (e: Exception) {
                localRepository.saveProduct(product, pendienteSincronizar = true)
            }
        } else {
            localRepository.saveProduct(product, pendienteSincronizar = true)
        }
    }

    override suspend fun updateProduct(product: Producto) {
        insertProduct(product)
    }

    override suspend fun deleteProduct(product: Producto) {
        if (networkMonitor.isConnected()) {
            localRepository.deleteLocalById(product.id)
            try {
                remoteRepository.deleteProduct(product)
            } catch (e: Exception) {
                localRepository.saveProduct(
                    product,
                    pendienteSincronizar = true,
                    eliminadoPendiente = true
                )
            }
        } else {
            localRepository.saveProduct(
                product,
                pendienteSincronizar = true,
                eliminadoPendiente = true
            )
        }
    }

    override suspend fun uploadProductImage(imagenUri: Uri): String {
        return if (networkMonitor.isConnected()) {
            remoteRepository.uploadProductImage(imagenUri)
        } else {
            copiarImagenAAlmacenamientoLocal(imagenUri)
        }
    }

    private suspend fun copiarImagenAAlmacenamientoLocal(uri: Uri): String =
        withContext(Dispatchers.IO) {
            val directorio = File(context.filesDir, "imagenes_pendientes").apply {
                if (!exists()) mkdirs()
            }
            val archivoDestino = File(directorio, "img_${System.currentTimeMillis()}.jpg")

            context.contentResolver.openInputStream(uri)?.use { input ->
                FileOutputStream(archivoDestino).use { output ->
                    input.copyTo(output)
                }
            }
            Uri.fromFile(archivoDestino).toString()
        }

    suspend fun sincronizarPendientes() {
        if (!networkMonitor.isConnected()) return

        val pendientes = localRepository.getPendingEntities()
        pendientes.forEach { entity ->
            try {
                if (entity.eliminadoPendiente) {
                    remoteRepository.deleteProduct(entity.toDomain())
                    localRepository.deleteLocalById(entity.id)
                } else {
                    var productoParaSubir = entity.toDomain()

                    val imagenActual = productoParaSubir.imagen
                    if (imagenActual.startsWith("file:") || imagenActual.startsWith("/")) {
                        val filePath = Uri.parse(imagenActual).path ?: imagenActual
                        val archivoLocal = File(filePath)

                        if (archivoLocal.exists()) {
                            val uriLocal = Uri.fromFile(archivoLocal)
                            val urlNube = remoteRepository.uploadProductImage(uriLocal)

                            if (urlNube.isNotBlank() && !urlNube.startsWith("file:") && !urlNube.startsWith("/")) {
                                productoParaSubir = productoParaSubir.copy(imagen = urlNube)
                                localRepository.saveProduct(productoParaSubir, pendienteSincronizar = true)
                                archivoLocal.delete()
                            } else {
                                Log.e("OfflineFirstProducts", "Falló la subida de imagen a Cloudinary para el producto #${productoParaSubir.id}")
                                return@forEach
                            }
                        }
                    }

                    remoteRepository.insertProduct(productoParaSubir)
                    localRepository.markAsSynced(productoParaSubir.id)
                }
            } catch (e: Exception) {
                Log.e("OfflineFirstProducts", "Error al sincronizar producto #${entity.id}: ${e.message}")
            }
        }
    }
}
