package servi.work.features.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import servi.work.core.data.model.Usuario
import servi.work.core.data.repository.FirestoreRepository
import servi.work.core.network.FirebaseProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class DashboardState {
    object Loading : DashboardState()
    data class Success(val usuario: Usuario) : DashboardState()
    data class Error(val message: String) : DashboardState()
}

class DashboardViewModel(
    private val repository: FirestoreRepository = FirestoreRepository()
) : ViewModel() {

    private val _state = MutableStateFlow<DashboardState>(DashboardState.Loading)
    val state: StateFlow<DashboardState> = _state

    init {
        loadUserProfile()
    }

    private fun loadUserProfile() {
        viewModelScope.launch {
            _state.value = DashboardState.Loading
            try {
                val uid = FirebaseProvider.auth.currentUser?.uid
                if (uid != null) {
                    val usuario = repository.getUsuario(uid)
                    if (usuario != null) {
                        _state.value = DashboardState.Success(usuario)
                    } else {
                        _state.value = DashboardState.Error("Perfil de ServiWork no encontrado")
                    }
                } else {
                    _state.value = DashboardState.Error("Usuario no autenticado en ServiWork")
                }
            } catch (e: Exception) {
                _state.value = DashboardState.Error(e.localizedMessage ?: "Error al cargar perfil")
            }
        }
    }

    fun toggleOnlineStatus(isOnline: Boolean) {
        viewModelScope.launch {
            val uid = FirebaseProvider.auth.currentUser?.uid
            if (uid != null) {
                try {
                    repository.updateProviderStatus(uid, isOnline)
                    val currentState = _state.value
                    if (currentState is DashboardState.Success) {
                        val updatedUsuario = currentState.usuario.copy(
                            // Nota: Se asume que el modelo Usuario tiene provider_data
                            // provider_data = currentState.usuario.provider_data?.copy(is_online = isOnline)
                        )
                        _state.value = DashboardState.Success(updatedUsuario)
                    }
                } catch (e: Exception) {
                    // Manejo de error
                }
            }
        }
    }
}
