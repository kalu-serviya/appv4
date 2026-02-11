package servi.work.features.admin

import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import servi.work.core.data.model.Usuario

data class AdminDashboardState(
    val tecnicosPorRubro: Map<String, Int> = emptyMap(),
    val registrosPorProvincia: Map<String, Int> = emptyMap(),
    val registrosPorRegion: Map<String, Int> = emptyMap(),
    val tasaExitoValidacion: Float = 0f,
    val tasaAbandono: Float = 0f,
    val totalTecnicos: Int = 0,
    val tecnicosElite: Int = 0,
    val pedidosSinRespuesta: Int = 0, // KPI Brecha de Servicio
    val resendApiStatus: Boolean = true, // Monitor de Salud
    val intentosFallidosAlertas: Int = 0,
    val isLoading: Boolean = true
)

class AdminViewModel : ViewModel() {

    private val db = Firebase.firestore
    private val _state = MutableStateFlow(AdminDashboardState())
    val state: StateFlow<AdminDashboardState> = _state.asStateFlow()

    private val ADMIN_EMAIL = "contacto@serviwork.com"

    init {
        startNacionalMonitoring()
    }

    private fun startNacionalMonitoring() {
        // Monitoreo de Usuarios y Capacidad Instalada
        db.collection("usuarios").addSnapshotListener { snapshot, _ ->
            snapshot?.let {
                val usuarios = it.toObjects(Usuario::class.java)
                val tecnicos = usuarios.filter { u -> u.role == "TECNICO" || u.is_provider }
                
                // 1. Ranking de Especialidades
                val rubros = tecnicos.groupBy { t -> t.especialidad.ifEmpty { "Sin Rubro" } }
                    .mapValues { entry -> entry.value.size }
                    .toList()
                    .sortedByDescending { (_, count) -> count }
                    .toMap()

                // 2. Técnicos de Élite (Perfil 100% completo)
                val elite = tecnicos.count { t -> 
                    t.avatar_url != null && t.especialidad.isNotEmpty() && t.direccion.isNotEmpty() 
                }

                // 3. Distribución Federal por Provincias y Regiones
                val provincias = tecnicos.groupBy { t -> t.zona_trabajo.ifEmpty { "Misiones" } }
                    .mapValues { entry -> entry.value.size }
                
                val regiones = mapToRegions(provincias)

                _state.value = _state.value.copy(
                    tecnicosPorRubro = rubros,
                    registrosPorProvincia = provincias,
                    registrosPorRegion = regiones,
                    totalTecnicos = tecnicos.size,
                    tecnicosElite = elite,
                    isLoading = false
                )
            }
        }

        // 4. Embudo de Conversión (OTP vs Registros)
        db.collection("temp_validations").addSnapshotListener { otpSnapshot, _ ->
            val otpsActivos = otpSnapshot?.size() ?: 0
            val totalRegistrados = _state.value.totalTecnicos
            
            val totalIntentos = totalRegistrados + otpsActivos
            val tasaExito = if (totalIntentos > 0) (totalRegistrados.toFloat() / totalIntentos) * 100 else 0f
            val tasaAbandono = if (totalIntentos > 0) (otpsActivos.toFloat() / totalIntentos) * 100 else 0f

            _state.value = _state.value.copy(
                tasaExitoValidacion = tasaExito,
                tasaAbandono = tasaAbandono
            )
        }
    }

    private fun mapToRegions(provincias: Map<String, Int>): Map<String, Int> {
        val mapping = mutableMapOf("NEA" to 0, "NOA" to 0, "Centro" to 0, "Cuyo" to 0, "Patagonia" to 0)
        
        provincias.forEach { (prov, count) ->
            when (prov.lowercase()) {
                "misiones", "corrientes", "chaco", "formosa" -> mapping["NEA"] = mapping["NEA"]!! + count
                "jujuy", "salta", "tucuman", "catamarca", "la rioja", "santiago del estero" -> mapping["NOA"] = mapping["NOA"]!! + count
                "buenos aires", "caba", "cordoba", "santa fe", "entre rios" -> mapping["Centro"] = mapping["Centro"]!! + count
                "san juan", "san luis", "mendoza" -> mapping["Cuyo"] = mapping["Cuyo"]!! + count
                else -> mapping["Patagonia"] = mapping["Patagonia"]!! + count
            }
        }
        return mapping
    }

    fun isUserAdmin(email: String?): Boolean {
        return email == ADMIN_EMAIL || email == "kalucorrea117@gmail.com"
    }
}
