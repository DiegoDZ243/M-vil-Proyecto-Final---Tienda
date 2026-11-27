package com.example.proyectofinal.data.repositories.products

import android.net.Uri
import android.util.Log
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import com.example.inventory.data.ProductsRepository
import com.example.proyectofinal.data.model.Producto
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.tasks.await
import java.util.UUID
import kotlin.coroutines.resume

class FirebaseProductsRepository(
    private val databaseRef: DatabaseReference
) : ProductsRepository {

    override fun getAllProductsStream(): Flow<List<Producto>> = callbackFlow {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
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

        databaseRef.child(id.toString()).addValueEventListener(listener)
        awaitClose { databaseRef.child(id.toString()).removeEventListener(listener) }
    }

    override suspend fun insertProduct(product: Producto) {
        databaseRef.child(product.id.toString()).setValue(product).await()
    }

    override suspend fun deleteProduct(product: Producto) {
        databaseRef.child(product.id.toString()).removeValue().await()
    }

    override suspend fun updateProduct(product: Producto) {
        databaseRef.child(product.id.toString()).setValue(product).await()
    }

    override suspend fun uploadProductImage(imageUri: Uri): String {
        return suspendCancellableCoroutine { continuation ->
            val uniqueFileName = UUID.randomUUID().toString()

            val uploadPayload: Any = if (imageUri.scheme == "file" && !imageUri.path.isNullOrEmpty()) {
                imageUri.path!!
            } else {
                imageUri
            }

            val request = when (uploadPayload) {
                is String -> MediaManager.get().upload(uploadPayload)
                is Uri -> MediaManager.get().upload(uploadPayload)
                else -> MediaManager.get().upload(imageUri)
            }

            request.unsigned("ml_default")
                .option("public_id", uniqueFileName)
                .callback(object : UploadCallback {
                    override fun onStart(requestId: String?) {}
                    override fun onProgress(requestId: String?, bytes: Long, totalBytes: Long) {}

                    override fun onSuccess(requestId: String?, resultData: Map<*, *>?) {
                        val url = resultData?.get("secure_url") as? String
                            ?: resultData?.get("url") as? String
                            ?: ""
                        if (continuation.isActive) {
                            continuation.resume(url)
                        }
                    }

                    override fun onError(requestId: String?, error: ErrorInfo?) {
                        Log.e("FirebaseProductsRepo", "Error al subir imagen a Cloudinary: ${error?.description}")
                        if (continuation.isActive) {
                            continuation.resume("")
                        }
                    }

                    override fun onReschedule(requestId: String?, error: ErrorInfo?) {
                        if (continuation.isActive) {
                            continuation.resume("")
                        }
                    }
                })
                .dispatch()
        }
    }
}
