package com.example.proyectofinal.data.repositories.products

import android.net.Uri
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import com.example.inventory.data.ProductsRepository
import com.example.proyectofinal.data.model.Producto
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.tasks.await
import java.util.UUID
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class FirebaseProductsRepository(
    private val databaseRef: DatabaseReference
) : ProductsRepository {

    override fun getAllProductsStream(): Flow<List<Producto>> = callbackFlow {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                // Mapeamos los nodos de Firebase a la lista de Productos
                val items = snapshot.children.mapNotNull {
                    it.getValue(Producto::class.java)
                }
                trySend(items)
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }

        databaseRef.addValueEventListener(listener)

        // Se ejecuta cuando el Flow deja de recolectarse
        awaitClose { databaseRef.removeEventListener(listener) }
    }

    override fun getProductStream(id: Int): Flow<Producto?> = callbackFlow {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val item = snapshot.getValue(Producto::class.java)
                trySend(item)
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }

        // Buscamos un hijo específico por su ID
        databaseRef.child(id.toString()).addValueEventListener(listener)
        awaitClose { databaseRef.child(id.toString()).removeEventListener(listener) }
    }

    override suspend fun insertProduct(product: Producto) {
        // Guardamos el producto usando su ID autogenerado como llave en Firebase
        databaseRef.child(product.id.toString()).setValue(product).await()
    }

    override suspend fun deleteProduct(product: Producto) {
        databaseRef.child(product.id.toString()).removeValue().await()
    }

    override suspend fun updateProduct(product: Producto) {
        // En Firebase, hacer setValue a un nodo que ya existe lo sobrescribe (lo actualiza)
        databaseRef.child(product.id.toString()).setValue(product).await()
    }

    override suspend fun uploadProductImage(imageUri: Uri): String {
        return suspendCancellableCoroutine { continuation ->
            val uniqueFileName=UUID.randomUUID().toString()

            MediaManager.get().upload(imageUri)
                .unsigned("ml_default") // Requisito de seguridad
                .option("public_id",uniqueFileName)
                .callback(object : UploadCallback {
                    override fun onStart(requestId: String?) {}
                    override fun onProgress(requestId: String?, bytes: Long, totalBytes: Long) {}

                    override fun onSuccess(requestId: String?, resultData: Map<*, *>?) {
                        // Extraemos la URL segura (https) del mapa de resultados
                        val url = resultData?.get("secure_url") as? String ?: ""
                        continuation.resume(url)
                    }

                    override fun onError(requestId: String?, error: ErrorInfo?) {
                        continuation.resumeWithException(Exception(error?.description))
                    }

                    override fun onReschedule(requestId: String?, error: ErrorInfo?) {}
                })
                .dispatch()
        }
    }

}