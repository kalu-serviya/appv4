package servi.work.features.admin

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class AdminDashboardActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val userEmail = Firebase.auth.currentUser?.email
        if (userEmail != "contacto@serviwork.com" && userEmail != "kalucorrea117@gmail.com") {
            finish()
            return
        }

        setContent {
            AdminDashboardScreen()
        }
    }
}

@Composable
fun AdminDashboardScreen(
    viewModel: AdminViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
            .padding(16.dp)
    ) {
        Text(
            text = "Inteligencia de Negocio ServiWork",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1A237E),
            modifier = Modifier.padding(bottom = 24.dp)
        )

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            // --- SECCIÓN 1: SALUD DEL BÚNKER ---
            item {
                HealthMonitorCard(resendOk = state.resendApiStatus, alerts = state.intentosFallidosAlertas)
            }

            // --- SECCIÓN 2: EMBUDO DE CONVERSIÓN ---
            item {
                Text(text = "Embudo de Conversión (Funnel)", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1A237E))
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    KPIWeightCard(label = "Éxito Registro", value = "${"%.1f".format(state.tasaExitoValidacion)}%", color = Color(0xFF4CAF50), modifier = Modifier.weight(1f))
                    KPIWeightCard(label = "Abandono OTP", value = "${"%.1f".format(state.tasaAbandono)}%", color = Color(0xFFF44336), modifier = Modifier.weight(1f))
                }
            }

            // --- SECCIÓN 3: CAPACIDAD INSTALADA ---
            item {
                Text(text = "Capacidad Instalada", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1A237E))
                Spacer(modifier = Modifier.height(8.dp))
                KPIWeightCard(label = "Técnicos de Élite (Perfil 100%)", value = state.tecnicosElite.toString(), color = Color(0xFF1976D2), modifier = Modifier.fillMaxWidth())
            }

            item {
                Text(text = "Top 10 Especialidades", fontSize = 16.sp, fontWeight = FontWeight.Medium)
            }
            items(state.tecnicosPorRubro.toList().take(10)) { (rubro, count) ->
                MetricRow(label = rubro, value = count.toString())
            }

            // --- SECCIÓN 4: EXPANSIÓN FEDERAL ---
            item {
                Text(text = "Distribución por Regiones", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1A237E))
            }
            items(state.registrosPorRegion.toList()) { (region, count) ->
                MetricRow(label = region, value = count.toString())
            }
            
            item { Spacer(modifier = Modifier.height(32.dp)) }
        }
    }
}

@Composable
fun HealthMonitorCard(resendOk: Boolean, alerts: Int) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = "Estado del Búnker", fontSize = 12.sp, color = Color.Gray)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (resendOk) Icons.Default.CheckCircle else Icons.Default.Error,
                        contentDescription = null,
                        tint = if (resendOk) Color(0xFF4CAF50) else Color.Red,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = if (resendOk) "Mails: Online" else "Mails: Error", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }
            }
            if (alerts > 0) {
                Badge(containerColor = Color.Red) {
                    Text("$alerts Alertas", color = Color.White, modifier = Modifier.padding(4.dp))
                }
            }
        }
    }
}

@Composable
fun KPIWeightCard(label: String, value: String, color: Color, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = label, fontSize = 11.sp, color = Color.Gray, fontWeight = FontWeight.Medium)
            Text(text = value, fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = color)
        }
    }
}

@Composable
fun MetricRow(label: String, value: String) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(modifier = Modifier.padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(text = label, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Text(text = value, color = Color(0xFF1976D2), fontWeight = FontWeight.ExtraBold)
        }
    }
}
