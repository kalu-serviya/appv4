package servi.work.features.auth

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import servi.work.data.EmailService

sealed class ResetPasswordState {
    object Idle : ResetPasswordState()
    object Loading : ResetPasswordState()
    object CodeSent : ResetPasswordState()
    object CodeValidated : ResetPasswordState()
    object Success : ResetPasswordState()
    data class Error(val message: String) : ResetPasswordState()
}

class ResetPasswordViewModel(
    private val emailService: EmailService = EmailService()
) : ViewModel() {

    private val _state = MutableStateFlow<ResetPasswordState>(ResetPasswordState.Idle)
    val state: StateFlow<ResetPasswordState> = _state

    private val db = Firebase.firestore
    private var targetEmail: String = ""
    private var generatedCode: String = ""

    fun sendResetCode(email: String) {
        viewModelScope.launch {
            _state.value = ResetPasswordState.Loading
            targetEmail = email
            generatedCode = emailService.generarCodigoOTP()

            try {
                // Guardamos el código temporalmente en Firestore para validar
                val resetData = hashMapOf(
                    "email" to email,
                    "code" to generatedCode,
                    "timestamp" to FieldValue.serverTimestamp()
                )
                db.collection("password_resets").document(email).set(resetData).await()

                val enviado = emailService.enviarEmailRecuperacion(email, generatedCode)
                if (enviado) {
                    _state.value = ResetPasswordState.CodeSent
                } else {
                    _state.value = ResetPasswordState.Error("Fallo al enviar el mail. Reintentá.")
                }
            } catch (e: Exception) {
                _state.value = ResetPasswordState.Error(e.localizedMessage ?: "Error de red")
            }
        }
    }

    fun validateCode(inputCode: String) {
        viewModelScope.launch {
            _state.value = ResetPasswordState.Loading
            try {
                val doc = db.collection("password_resets").document(targetEmail).get().await()
                val serverCode = doc.getString("code")

                if (serverCode != null && serverCode == inputCode) {
                    _state.value = ResetPasswordState.CodeValidated
                } else {
                    _state.value = ResetPasswordState.Error("Código incorrecto.")
                }
            } catch (e: Exception) {
                _state.value = ResetPasswordState.Error("Error al validar código.")
            }
        }
    }

    fun updatePassword(newPass: String) {
        viewModelScope.launch {
            _state.value = ResetPasswordState.Loading
            try {
                // Nota: En Firebase Auth real, para cambiar contraseña sin el link de Google,
                // el usuario debe estar logueado recientemente o usar el link.
                // Simulamos el éxito para la fase de diseño corporativo de ServiWork.
                db.collection("password_resets").document(targetEmail).delete()
                _state.value = ResetPasswordState.Success
            } catch (e: Exception) {
                _state.value = ResetPasswordState.Error("Fallo al actualizar clave.")
            }
        }
    }
}
