package com.example.proyectofinal.data.model.service

import com.example.proyectofinal.data.model.ProductoCarrito
import com.example.proyectofinal.data.model.Usuario
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

interface AccountService {
    val currentUserId: String
    val isUserLoggedIn: Boolean
    val currentUser: Flow<Usuario?>

    suspend fun authenticate(email: String, password: String)
    suspend fun signUp(usuario: Usuario, password: String)
    suspend fun signOut()
    suspend fun guardarCarrito(uid: String, carrito: List<ProductoCarrito>)
}

class AccountServiceImpl(
    private val auth: FirebaseAuth,
    private val usersRef: DatabaseReference
) : AccountService {

    override val currentUserId: String
        get() = auth.currentUser?.uid.orEmpty()

    override val isUserLoggedIn: Boolean
        get() = auth.currentUser != null

    override val currentUser: Flow<Usuario?> = callbackFlow {
        var dbListener: ValueEventListener? = null
        var currentUid: String? = null

        val authListener = FirebaseAuth.AuthStateListener { fbAuth: FirebaseAuth ->
            val uid = fbAuth.currentUser?.uid
            if (uid != currentUid) {
                currentUid?.let { oldUid ->
                    dbListener?.let { listener ->
                        usersRef.child(oldUid).removeEventListener(listener)
                    }
                }
                currentUid = uid
                if (uid != null) {
                    val listener = object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            val usuario = snapshot.getValue(Usuario::class.java)
                            trySend(usuario)
                        }

                        override fun onCancelled(error: DatabaseError) {
                            trySend(null)
                        }
                    }
                    dbListener = listener
                    usersRef.child(uid).addValueEventListener(listener)
                } else {
                    trySend(null)
                }
            }
        }

        auth.addAuthStateListener(authListener)

        awaitClose {
            auth.removeAuthStateListener(authListener)
            val activeUid = currentUid
            val activeListener = dbListener
            if (activeUid != null && activeListener != null) {
                usersRef.child(activeUid).removeEventListener(activeListener)
            }
        }
    }

    override suspend fun authenticate(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password).await()
    }

    override suspend fun signUp(usuario: Usuario, password: String) {
        val result = auth.createUserWithEmailAndPassword(usuario.correo, password).await()
        val uid = result.user?.uid ?: throw IllegalStateException("No se pudo obtener el UID del usuario registrado")
        val usuarioConUid = usuario.copy(uid = uid)
        usersRef.child(uid).setValue(usuarioConUid).await()
    }

    override suspend fun signOut() {
        auth.signOut()
    }

    override suspend fun guardarCarrito(uid: String, carrito: List<ProductoCarrito>) {
        usersRef.child(uid).child("carrito").setValue(carrito).await()
    }
}
