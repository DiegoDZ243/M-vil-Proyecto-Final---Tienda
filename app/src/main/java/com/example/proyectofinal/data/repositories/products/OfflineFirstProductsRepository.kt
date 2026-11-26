package com.example.proyectofinal.data.repositories.products

import android.content.Context
import android.net.Uri
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
    private val remoteRepository: FirebaseProductsRepository, // Tu repo actual de Firebase
    private val networkMonitor: NetworkMonitor,
    private val context: Context
) : ProductsRepository {

    override fun getAllProductsStream(): Flow<List<Producto>> = channelFlow {
        // 1. En segundo plano, escucha Firebase y actualiza Room cuando haya red
        launch {
            remoteRepository.getAllProductsStream()
                .catch { /* Ignora errores de red si está offline */ }
                .collect { productosFirebase ->
                    localRepository.syncFromRemote(productosFirebase)
                }
        }

        // 2. Emite siempre desde Room (respuesta inmediata con o sin internet)
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
        // Misma lógica que insertProduct (REPLACE en Room y setValue en Firebase)
        insertProduct(product)
    }

    override suspend fun deleteProduct(product: Producto) {
        if (networkMonitor.isConnected()) {
            localRepository.deleteLocalById(product.id)
            try {
                remoteRepository.deleteProduct(product)
            } catch (e: Exception) {
                // Si falló, lo dejamos oculto en Room para borrarlo en Firebase luego
                localRepository.saveProduct(
                    product,
                    pendienteSincronizar = true,
                    eliminadoPendiente = true
                )
            }
        } else {
            // Sin internet: lo ocultamos en Room y lo marcamos para borrarlo al reconectar
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
            // Copia la foto a la carpeta privada de la app para que Android no revoque el permiso de lectura
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
            // Devuelve la ruta interna con formato "file:///..."
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

                    // Si la imagen es un archivo local pendiente, súbela primero a Cloudinary/Firebase
                    if (productoParaSubir.imagen.startsWith("file://")) {
                        val uriLocal = Uri.parse(productoParaSubir.imagen)
                        val urlNube = remoteRepository.uploadProductImage(uriLocal)

                        productoParaSubir = productoParaSubir.copy(imagen = urlNube)
                        localRepository.saveProduct(productoParaSubir, pendienteSincronizar = true)

                        // Borra el archivo temporal local ya que está en la nube
                        uriLocal.path?.let { File(it).delete() }
                    }

                    remoteRepository.insertProduct(productoParaSubir)
                    localRepository.markAsSynced(productoParaSubir.id)
                }
            } catch (e: Exception) {
                // Si vuelve a fallar la red, se queda pendiente para el siguiente intento
            }
        }
    }
}