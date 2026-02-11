package servi.work.features.agenda.model

import java.time.LocalDate

enum class DayStatus {
    DISPONIBLE,
    OCUPADO,
    BLOQUEADO
}

enum class TimeSlot(val label: String, val range: String) {
    MANANA("Mañana", "8:00 - 12:00"),
    TARDE("Tarde", "13:00 - 17:00"),
    NOCHE("Noche", "18:00 - 22:00")
}

data class AgendaDay(
    val date: LocalDate,
    val status: DayStatus = DayStatus.DISPONIBLE,
    val selectedSlots: Set<TimeSlot> = emptySet()
)

data class AgendaSelection(
    val date: LocalDate? = null,
    val slot: TimeSlot? = null
)
