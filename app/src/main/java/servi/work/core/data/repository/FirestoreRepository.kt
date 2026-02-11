package servi.work.core.data.repository

import servi.work.core.data.model.Usuario
import servi.work.core.network.FirebaseProvider
import kotlinx.coroutines.tasks.await

interface IFirestoreRepository {
    suspend fun saveUsuario(usuario: Usuario)
    suspend fun getUsuario(uid: String): Usuario?
    suspend fun updateProviderStatus(uid: String, isOnline: Boolean)
}

class FirestoreRepository : IFirestoreRepository {
    private val firestore = FirebaseProvider.firestore

    override suspend fun saveUsuario(usuario: Usuario) {
        firestore.collection("usuarios")
            .document(usuario.uid)
            .set(usuario)
            .await()
    }

    override suspend fun getUsuario(uid: String): Usuario? {
        return try {
            val document = firestore.collection("usuarios")
                .document(uid)
                .get()
                .await()
            document.toObject(Usuario::class.java)
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun updateProviderStatus(uid: String, isOnline: Boolean) {
        firestore.collection("usuarios")
            .document(uid)
            .update("provider_data.is_online", isOnline)
            .await()
    }
}
