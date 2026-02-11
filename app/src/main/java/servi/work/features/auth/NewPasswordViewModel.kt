package servi.work.features.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class NewPasswordState {
    object Idle : NewPasswordState()
    object Loading : NewPasswordState()
    object Success : NewPasswordState()
    data class Error(val message: String) : NewPasswordState()
}

class NewPasswordViewModel : ViewModel() {
    private val _state = MutableStateFlow<NewPasswordState>(NewPasswordState.Idle)
    val state: StateFlow<NewPasswordState> = _state

    fun updatePassword(newPassword: String) {
        viewModelScope.launch {
            _state.value = NewPasswordState.Loading
            
            // SIMULACIÓN DE SEGURIDAD PARA FASE DE DISEÑO DE SERVIWORK
            delay(1500L) 
            
            _state.value = NewPasswordState.Success
        }
    }
}
