package servi.work.features.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import servi.work.core.network.FirebaseProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

sealed class LoginState {
    object Idle : LoginState()
    object Loading : LoginState()
    data class Success(val role: String) : LoginState()
    data class Error(val message: String) : LoginState()
}

class LoginViewModel : ViewModel() {
    private val _state = MutableStateFlow<LoginState>(LoginState.Idle)
    val state: StateFlow<LoginState> = _state

    private val auth = FirebaseProvider.auth
    private val firestore = FirebaseProvider.firestore

    fun login(email: String, pass: String) {
        if (email.isEmpty() || pass.isEmpty()) {
            _state.value = LoginState.Error("Por favor, completa todos los campos.")
            return
        }

        viewModelScope.launch {
            _state.value = LoginState.Loading
            try {
                // 1. Intento de inicio de sesión oficial en Firebase
                val result = auth.signInWithEmailAndPassword(email, pass).await()
                val uid = result.user?.uid

                if (uid != null) {
                    // 2. Verificación de perfil en Firestore
                    val doc = firestore.collection("usuarios").document(uid).get().await()
                    
                    if (doc.exists()) {
                        val role = doc.getString("role") ?: "cliente"
                        _state.value = LoginState.Success(role)
                    } else {
                        // Si el documento no existe, igual lo dejamos pasar como cliente base
                        _state.value = LoginState.Success("cliente")
                    }
                } else {
                    _state.value = LoginState.Error("Error de comunicación con el búnker de datos.")
                }
            } catch (e: Exception) {
                _state.value = LoginState.Error("Credenciales incorrectas o problema de red.")
            }
        }
    }
}
