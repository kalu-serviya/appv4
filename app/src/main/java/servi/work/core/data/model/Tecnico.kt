package servi.work.core.data.model

data class Tecnico(
    val uid: String = "",
    val dni: String = "",
    val nombre: String = "",
    val email: String = "",
    val telefono: String = "",
    val especialidad: String = "",
    val is_active: Boolean = false,
    val rating: Double = 0.0,
    val location: String = ""
)
