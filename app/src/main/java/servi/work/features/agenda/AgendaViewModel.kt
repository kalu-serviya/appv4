package servi.work.features.agenda

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import servi.work.features.agenda.model.AgendaDay
import servi.work.features.agenda.model.AgendaSelection
import servi.work.features.agenda.model.DayStatus
import servi.work.features.agenda.model.TimeSlot
import java.time.LocalDate
import java.time.YearMonth

data class AgendaUiState(
    val currentMonth: YearMonth = YearMonth.now(),
    val days: List<AgendaDay> = emptyList(),
    val selection: AgendaSelection = AgendaSelection(),
    val isLoading: Boolean = false,
    val viewMode: String = "Mes",
    val alertsCount: Int = 3,
    val currentUbicacion: String = "Buenos Aires, Argentina",
    val showNewAppointmentForm: Boolean = false,
    val appointmentFormState: NewAppointmentFormState = NewAppointmentFormState()
)

data class NewAppointmentFormState(
    val serviceType: String = "",
    val description: String = "",
    val images: List<String> = emptyList(),
    val isUrgente: Boolean = false,
    val showUrgencyWarning: Boolean = false
)

class AgendaViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(AgendaUiState())
    val uiState: StateFlow<AgendaUiState> = _uiState.asStateFlow()

    init {
        generateMonthDays(YearMonth.now())
    }

    fun onMonthChange(newMonth: YearMonth) {
        _uiState.value = _uiState.value.copy(currentMonth = newMonth)
        generateMonthDays(newMonth)
    }

    fun onDayClick(date: LocalDate) {
        _uiState.value = _uiState.value.copy(
            selection = _uiState.value.selection.copy(date = date, slot = null)
        )
    }

    fun onSlotSelect(slot: TimeSlot) {
        // Implementación de Validación de Solapamiento Zero (Simulada)
        val isSlotOccupied = false 
        
        if (!isSlotOccupied) {
            _uiState.value = _uiState.value.copy(
                selection = _uiState.value.selection.copy(slot = slot),
                showNewAppointmentForm = true // DISPARA EL FORMULARIO
            )
        }
    }

    fun setViewMode(mode: String) {
        _uiState.value = _uiState.value.copy(viewMode = mode)
    }

    fun closeForm() {
        _uiState.value = _uiState.value.copy(showNewAppointmentForm = false)
    }

    fun onServiceTypeChange(type: String) {
        _uiState.value = _uiState.value.copy(
            appointmentFormState = _uiState.value.appointmentFormState.copy(serviceType = type)
        )
    }

    fun onDescriptionChange(desc: String) {
        _uiState.value = _uiState.value.copy(
            appointmentFormState = _uiState.value.appointmentFormState.copy(description = desc)
        )
    }

    fun setUrgency(urgente: Boolean) {
        if (urgente) {
            _uiState.value = _uiState.value.copy(
                appointmentFormState = _uiState.value.appointmentFormState.copy(
                    isUrgente = true,
                    showUrgencyWarning = true
                )
            )
        } else {
            _uiState.value = _uiState.value.copy(
                appointmentFormState = _uiState.value.appointmentFormState.copy(isUrgente = false)
            )
        }
    }

    fun dismissUrgencyWarning() {
        _uiState.value = _uiState.value.copy(
            appointmentFormState = _uiState.value.appointmentFormState.copy(showUrgencyWarning = false)
        )
    }

    private fun generateMonthDays(month: YearMonth) {
        val daysInMonth = month.lengthOfMonth()
        val days = (1..daysInMonth).map { day ->
            val date = month.atDay(day)
            val status = when {
                day % 8 == 0 -> DayStatus.OCUPADO
                day % 12 == 0 -> DayStatus.BLOQUEADO
                else -> DayStatus.DISPONIBLE
            }
            AgendaDay(date = date, status = status)
        }
        _uiState.value = _uiState.value.copy(days = days)
    }
}
