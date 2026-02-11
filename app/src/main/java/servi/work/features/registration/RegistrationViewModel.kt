package servi.work.features.registration

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import servi.work.core.data.model.Usuario
import servi.work.core.data.repository.FirestoreRepository
import servi.work.core.network.FirebaseProvider
import servi.work.data.EmailService
import com.google.firebase.firestore.FieldValue
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

sealed class RegistrationState {
    object Idle : RegistrationState()
    object Loading : RegistrationState()
    data class Success(val message: String) : RegistrationState()
    data class OtpSent(val message: String) : RegistrationState()
    data class Error(val error: String) : RegistrationState()
}

class RegistrationViewModel(
    private val repository: FirestoreRepository = FirestoreRepository(),
    private val emailService: EmailService = EmailService()
) : ViewModel() {

    private val _state = MutableStateFlow<RegistrationState>(RegistrationState.Idle)
    val state: StateFlow<RegistrationState> = _state

    private val firestore = FirebaseProvider.firestore
    private val auth = FirebaseProvider.auth

    fun sendOtpCode(email: String, nombre: String) {
        viewModelScope.launch {
            _state.value = RegistrationState.Loading
            try {
                val otpCode = emailService.generarCodigoOTP()
                val otpData = hashMapOf(
                    "email" to email,
                    "code" to otpCode,
                    "timestamp" to FieldValue.serverTimestamp()
                )
                firestore.collection("temp_validations").document(email).set(otpData).await()

                val emailEnviado = emailService.enviarEmailBienvenida(
                    emailDestino = email,
                    nombre = nombre,
                    otp = otpCode
                )

                if (emailEnviado) {
                    _state.value = RegistrationState.OtpSent("Código enviado a $email")
                } else {
                    _state.value = RegistrationState.Error("Fallo al entregar el mail.")
                }
            } catch (e: Exception) {
                _state.value = RegistrationState.Error(translateError(e.message))
            }
        }
    }

    fun signUp(
        dni: String,
        nombre: String,
        apellido: String,
        nacionalidad: String,
        direccion: String,
        emailInput: String,
        telefono: String,
        pass: String,
        inputOtp: String
    ) {
        viewModelScope.launch {
            _state.value = RegistrationState.Loading
            try {
                val doc = firestore.collection("temp_validations").document(emailInput).get().await()
                val serverOtp = doc.getString("code")
                
                if (serverOtp == null || serverOtp != inputOtp) {
                    _state.value = RegistrationState.Error("El código de verificación es incorrecto o ha expirado.")
                    return@launch
                }

                val result = auth.createUserWithEmailAndPassword(emailInput, pass).await()
                val uid = result.user?.uid

                if (uid != null) {
                    val nuevoUsuario = Usuario(
                        uid = uid,
                        dni = dni,
                        nombre = nombre,
                        apellido = apellido,
                        nacionalidad = nacionalidad,
                        direccion = direccion,
                        email = emailInput,
                        telefono = telefono
                    )
                    firestore.collection("usuarios").document(uid).set(nuevoUsuario).await()
                    firestore.collection("temp_validations").document(emailInput).delete()
                    
                    Log.d("ServiWork_Debug", "Cuenta creada para $nombre $apellido")
                    _state.value = RegistrationState.Success("¡Búnker ServiWork creado!")
                }
            } catch (e: Exception) {
                Log.e("ServiWork_Debug", "Error en signUp: ${e.message}")
                _state.value = RegistrationState.Error(translateError(e.message))
            }
        }
    }

    private fun translateError(message: String?): String {
        return when {
            message == null -> "Ocurrió un error inesperado."
            message.contains("already in use") -> "Este correo electrónico ya está registrado por otra cuenta."
            message.contains("badly formatted") -> "El formato del correo electrónico no es válido."
            message.contains("at least 6 characters") -> "La contraseña debe tener al menos 6 caracteres."
            message.contains("network-request-failed") -> "Error de conexión a internet."
            else -> "Error en el búnker: $message"
        }
    }
}
