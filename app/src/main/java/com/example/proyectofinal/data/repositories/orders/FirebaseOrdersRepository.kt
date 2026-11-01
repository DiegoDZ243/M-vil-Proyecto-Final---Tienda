package com.example.proyectofinal.data.repositories.orders

import com.example.proyectofinal.data.model.Pedido
import com.example.proyectofinal.data.model.Producto
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class FirebaseOrdersRepository(
    private val databaseRef: DatabaseReference
) : OrdersRepository{
    override fun getAllOrdersStream(): Flow<List<Pedido>> = callbackFlow {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                // Mapeamos los nodos de Firebase a la lista de Productos
                val items = snapshot.children.mapNotNull {
                    it.getValue(Pedido::class.java)
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

    override fun getOrderStream(id: Int): Flow<Pedido?> = callbackFlow {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val item = snapshot.getValue(Pedido::class.java)
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

    override suspend fun insertOrder(order: Pedido) {
        // Guardamos el producto usando su ID autogenerado como llave en Firebase
        databaseRef.child(order.id.toString()).setValue(order).await()
    }

    override suspend fun deleteOrder(product: Producto) {
        databaseRef.child(product.id.toString()).removeValue().await()
    }

    override suspend fun updateOrder(product: Producto) {
        // En Firebase, hacer setValue a un nodo que ya existe lo sobrescribe (lo actualiza)
        databaseRef.child(product.id.toString()).setValue(product).await()
    }
}
