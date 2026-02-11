package servi.work.features.agenda.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import servi.work.features.agenda.AgendaViewModel
import servi.work.features.agenda.model.DayStatus
import servi.work.features.agenda.model.TimeSlot
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AgendaScreen(
    viewModel: AgendaViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var viewMode by remember { mutableStateOf("Mes") }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
        ) {
            // --- TOP BAR ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Add, contentDescription = "Añadir", modifier = Modifier.size(28.dp))
                Spacer(modifier = Modifier.width(16.dp))
                Icon(Icons.Default.MoreVert, contentDescription = "Más opciones", modifier = Modifier.size(28.dp))
            }

            // --- TITULO MES / AÑO ---
            Text(
                text = "${uiState.currentMonth.monthValue} / ${uiState.currentMonth.year}",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // --- DÍAS DE LA SEMANA ---
            Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
                listOf("DOM", "LUN", "MAR", "MIÉ", "JUE", "VIE", "SÁB").forEach { day ->
                    Text(
                        text = day,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.DarkGray
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // --- GRILLA DE DÍAS ---
            val firstDayOfMonth = uiState.currentMonth.atDay(1)
            val firstDayOfWeek = firstDayOfMonth.dayOfWeek.value % 7 

            Box(modifier = Modifier.weight(1f).padding(horizontal = 16.dp)) {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(7),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(firstDayOfWeek) { Spacer(modifier = Modifier.fillMaxWidth()) }
                    items(uiState.days) { agendaDay ->
                        val isSelected = uiState.selection.date == agendaDay.date
                        val isToday = agendaDay.date == LocalDate.now()
                        DayCell(
                            day = agendaDay.date.dayOfMonth,
                            status = agendaDay.status,
                            isSelected = isSelected,
                            isToday = isToday,
                            onClick = { viewModel.onDayClick(agendaDay.date) }
                        )
                    }
                }
            }

            // --- SELECTOR DE FRANJAS ---
            if (uiState.selection.date != null) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = Color(0xFFF8F9FA),
                    shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
                    shadowElevation = 8.dp
                ) {
                    Column(modifier = Modifier.padding(24.dp)) {
                        Text(
                            text = "Turnos para el ${uiState.selection.date?.dayOfMonth}/${uiState.selection.date?.monthValue}",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            TimeSlot.values().forEach { slot ->
                                val isSlotSelected = uiState.selection.slot == slot
                                SlotCard(
                                    slot = slot,
                                    isSelected = isSlotSelected,
                                    onClick = { viewModel.onSlotSelect(slot) }
                                )
                            }
                        }
                    }
                }
            }

            AgendaSubBottomBar(viewMode) { viewMode = it }
        }

        // --- MODAL "NUEVA CITA" (REFORMA TOTAL) ---
        if (uiState.showNewAppointmentForm) {
            NewAppointmentForm(
                uiState = uiState,
                onClose = { viewModel.closeForm() },
                onServiceTypeChange = { viewModel.onServiceTypeChange(it) },
                onDescriptionChange = { viewModel.onDescriptionChange(it) },
                onUrgencyChange = { viewModel.setUrgency(it) }
            )
        }

        // --- ALERTA DE URGENCIA ---
        if (uiState.appointmentFormState.showUrgencyWarning) {
            AlertDialog(
                onDismissRequest = { viewModel.dismissUrgencyWarning() },
                title = { Text("⚠️ Alerta de Prioridad", fontWeight = FontWeight.Bold) },
                text = { Text("Este pedido requiere atención inmediata. El prestador aplicará un recargo por desplazamiento de agenda. ¿Acepta los términos de urgencia?") },
                confirmButton = {
                    TextButton(onClick = { viewModel.dismissUrgencyWarning() }) {
                        Text("ACEPTO EL CARGO EXTRA", color = Color.Red, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { 
                        viewModel.setUrgency(false)
                        viewModel.dismissUrgencyWarning()
                    }) {
                        Text("CANCELAR")
                    }
                }
            )
        }
    }
}

@Composable
fun NewAppointmentForm(
    uiState: servi.work.features.agenda.AgendaUiState,
    onClose: () -> Unit,
    onServiceTypeChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onUrgencyChange: (Boolean) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFF1A1A1A) // FONDO OSCURO PREMIUM
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("🗓️ Nueva Cita", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                IconButton(onClick = onClose) {
                    Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = Color.White)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // GPS PASIVO
            Surface(
                color = Color(0xFF2E7D32).copy(alpha = 0.2f),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.LocationOn, contentDescription = null, tint = Color(0xFF4CAF50), modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("GPS Detectado: ${uiState.currentUbicacion}", color = Color.White, fontSize = 12.sp)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // INFO FECHA/HORA
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                InfoBox(label = "Fecha", value = "${uiState.selection.date?.dayOfMonth}/${uiState.selection.date?.monthValue}", icon = Icons.Default.CalendarToday, modifier = Modifier.weight(1f))
                InfoBox(label = "Horario", value = uiState.selection.slot?.range ?: "", icon = Icons.Default.Schedule, modifier = Modifier.weight(1f))
            }

            Spacer(modifier = Modifier.height(24.dp))

            // FORMULARIO
            Text("Tipo de Servicio", color = Color.Gray, fontSize = 14.sp)
            OutlinedTextField(
                value = uiState.appointmentFormState.serviceType,
                onValueChange = onServiceTypeChange,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Seleccionar servicio...", color = Color.DarkGray) },
                colors = OutlinedTextFieldDefaults.colors(unfocusedTextColor = Color.White, focusedTextColor = Color.White, unfocusedBorderColor = Color.DarkGray),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text("Descripción del Trabajo", color = Color.Gray, fontSize = 14.sp)
            OutlinedTextField(
                value = uiState.appointmentFormState.description,
                onValueChange = onDescriptionChange,
                modifier = Modifier.fillMaxWidth().height(120.dp),
                placeholder = { Text("Describe el trabajo a realizar...", color = Color.DarkGray) },
                colors = OutlinedTextFieldDefaults.colors(unfocusedTextColor = Color.White, focusedTextColor = Color.White, unfocusedBorderColor = Color.DarkGray),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // CARRUSEL DE EVIDENCIA (MOCK)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("📷 Evidencia Visual", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                Text("${uiState.appointmentFormState.images.size}/6", color = Color(0xFF4285F4), fontSize = 12.sp)
            }
            Spacer(modifier = Modifier.height(8.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                item {
                    Box(
                        modifier = Modifier.size(80.dp).background(Color(0xFF333333), RoundedCornerShape(12.dp)).clickable { /* Subir imagen */ },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.AddAPhoto, contentDescription = null, tint = Color.Gray)
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // SELECTOR DE URGENCIA
            Text("⚠️ Nivel de Urgencia", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                UrgencyButton(
                    label = "PROGRAMADO",
                    isSelected = !uiState.appointmentFormState.isUrgente,
                    color = Color(0xFF4CAF50),
                    onClick = { onUrgencyChange(false) },
                    modifier = Modifier.weight(1f)
                )
                UrgencyButton(
                    label = "URGENTE",
                    isSelected = uiState.appointmentFormState.isUrgente,
                    brush = Brush.horizontalGradient(listOf(Color(0xFFFF4B2B), Color(0xFFFF416C))),
                    onClick = { onUrgencyChange(true) },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = { /* ENVIAR SOLICITUD */ },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4285F4))
            ) {
                Text("ENVIAR SOLICITUD", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }
    }
}

@Composable
fun InfoBox(label: String, value: String, icon: ImageVector, modifier: Modifier = Modifier) {
    Surface(
        color = Color(0xFF333333),
        shape = RoundedCornerShape(12.dp),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(label, color = Color.Gray, fontSize = 10.sp)
            }
            Text(value, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun UrgencyButton(
    label: String,
    isSelected: Boolean,
    color: Color = Color.Gray,
    brush: Brush? = null,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val finalModifier = if (brush != null && isSelected) {
        modifier.background(brush, RoundedCornerShape(12.dp))
    } else if (isSelected) {
        modifier.background(color, RoundedCornerShape(12.dp))
    } else {
        modifier.border(1.dp, Color.DarkGray, RoundedCornerShape(12.dp))
    }

    Box(
        modifier = finalModifier
            .height(50.dp)
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            label,
            color = if (isSelected) Color.White else Color.Gray,
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp
        )
    }
}

@Composable
fun DayCell(day: Int, status: DayStatus, isSelected: Boolean, isToday: Boolean, onClick: () -> Unit) {
    val backgroundColor = when {
        isSelected -> Color(0xFF4285F4)
        else -> Color.Transparent
    }
    val textColor = when {
        isSelected -> Color.White
        status == DayStatus.OCUPADO -> Color.Red
        status == DayStatus.BLOQUEADO -> Color.LightGray
        day % 7 == 0 || (day + 1) % 7 == 0 -> Color(0xFF4285F4)
        else -> Color.Black
    }
    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .padding(4.dp)
            .background(backgroundColor, RoundedCornerShape(12.dp))
            .clickable(enabled = status == DayStatus.DISPONIBLE) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = day.toString(), fontSize = 18.sp, fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.Normal, color = textColor)
            if (isToday && !isSelected) { Box(modifier = Modifier.size(4.dp).background(Color(0xFF4285F4), CircleShape)) }
        }
    }
}

@Composable
fun SlotCard(slot: TimeSlot, isSelected: Boolean, onClick: () -> Unit) {
    Card(
        modifier = Modifier.height(80.dp).width(100.dp).clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = if (isSelected) Color(0xFF4285F4) else Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize().padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Text(text = slot.label, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = if (isSelected) Color.White else Color.Black)
            Text(text = slot.range, fontSize = 10.sp, color = if (isSelected) Color.White.copy(alpha = 0.8f) else Color.Gray)
        }
    }
}

@Composable
fun AgendaSubBottomBar(currentMode: String, onModeChange: (String) -> Unit) {
    Surface(modifier = Modifier.fillMaxWidth(), color = Color(0xFFF8F9FA), tonalElevation = 2.dp) {
        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), horizontalArrangement = Arrangement.SpaceAround) {
            AgendaSubItem("Año", Icons.Default.CalendarMonth, currentMode == "Año") { onModeChange("Año") }
            AgendaSubItem("Mes", Icons.Default.CalendarViewMonth, currentMode == "Mes") { onModeChange("Mes") }
            AgendaSubItem("Semana", Icons.Default.CalendarViewWeek, currentMode == "Semana") { onModeChange("Semana") }
            AgendaSubItem("Día", Icons.Default.CalendarViewDay, currentMode == "Día") { onModeChange("Día") }
        }
    }
}

@Composable
fun AgendaSubItem(label: String, icon: ImageVector, isSelected: Boolean, onClick: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable { onClick() }) {
        Icon(imageVector = icon, contentDescription = label, tint = if (isSelected) Color.Black else Color.Gray, modifier = Modifier.size(24.dp))
        Text(text = label, fontSize = 10.sp, color = if (isSelected) Color.Black else Color.Gray)
    }
}
