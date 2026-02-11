package servi.work.core.data.model

/**
 * Modelo Maestro de Usuario - ServiWork
 * Incluye lógica de estados para el Centro de Mando.
 */
data class Usuario(
    val uid: String = "",
    val dni: String = "",
    val nombre: String = "",
    val apellido: String = "",
    val nacionalidad: String = "",
    val direccion: String = "",
    val especialidad: String = "",
    val zona_trabajo: String = "",
    val email: String = "",
    val telefono: String = "",
    val is_provider: Boolean = false,
    val is_active: Boolean = true,
    
    // GESTOR DE ESTADOS (Mando Central)
    // Valores: "active", "blocked", "pending_verification"
    val status: String = "active", 
    
    // MÓDULO DE VERIFICACIÓN (URLs de Storage)
    val dni_front_url: String? = null,
    val dni_back_url: String? = null,
    val matricula_url: String? = null,
    
    val avatar_url: String? = null,
    val role: String = "CLIENTE",
    val solana_identity_hash: String? = null
)
