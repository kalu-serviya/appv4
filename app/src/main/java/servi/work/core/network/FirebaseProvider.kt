package servi.work.core.network

import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage

object FirebaseProvider {
    val auth get() = Firebase.auth
    val firestore get() = Firebase.firestore
    val storage get() = Firebase.storage
}
