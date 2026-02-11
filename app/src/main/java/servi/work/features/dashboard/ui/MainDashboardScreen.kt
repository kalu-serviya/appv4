package servi.work.features.dashboard.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import servi.work.core.data.model.Usuario
import servi.work.features.agenda.ui.AgendaScreen
import servi.work.features.dashboard.DashboardState
import servi.work.features.dashboard.DashboardViewModel

@Composable
fun MainDashboardScreen(
    viewModel: DashboardViewModel = viewModel(),
    onNavigateToProfile: () -> Unit = {},
    onLogout: () -> Unit = {}
) {
    val state by viewModel.state.collectAsState()
    var selectedTab by remember { mutableIntStateOf(0) }

    Scaffold(
        bottomBar = {
            BottomNavigationBar(
                selectedTab = selectedTab,
                onTabSelected = { selectedTab = it }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color.White)
        ) {
            when (state) {
                is DashboardState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Color(0xFF1976D2))
                    }
                }
                is DashboardState.Success -> {
                    DashboardContent(
                        usuario = (state as DashboardState.Success).usuario,
                        selectedTab = selectedTab,
                        onLogout = onLogout
                    )
                }
                is DashboardState.Error -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(text = (state as DashboardState.Error).message, color = Color.Red)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardContent(
    usuario: Usuario,
    selectedTab: Int,
    onLogout: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }

    when (selectedTab) {
        0 -> { // INICIO
            Column(modifier = Modifier.fillMaxSize()) {
                // --- HEADER AZUL CORPORATIVO ---
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            color = Color(0xFF1976D2),
                            shape = RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp)
                        )
                        .padding(top = 48.dp, bottom = 48.dp)
                ) {
                    Column(modifier = Modifier.padding(horizontal = 24.dp)) {
                        Text(text = "¡Bienvenido a ServiWork!", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        Text(text = "Hola, ${usuario.nombre}", fontSize = 16.sp, color = Color.White.copy(alpha = 0.8f))
                    }
                }

                // --- BUSCADOR FLOTANTE ---
                Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp).offset(y = (-28).dp)) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("¿Qué servicio necesitás hoy?") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White,
                            focusedBorderColor = Color.LightGray,
                            unfocusedBorderColor = Color.LightGray
                        ),
                        singleLine = true
                    )
                }

                // --- ZONA PUBLICITARIA (BOX BLANCO) ---
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp)
                        .padding(horizontal = 24.dp)
                        .background(Color.White, shape = RoundedCornerShape(16.dp))
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Espacio Publicitario Reservado", color = Color.LightGray, fontWeight = FontWeight.Medium)
                }
            }
        }
        2 -> { // AGENDA (SOLDADURA COMPLETADA ✅)
            AgendaScreen()
        }
        3 -> { // PERFIL
            Column(
                modifier = Modifier.fillMaxSize().padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(text = "Tu Perfil", fontSize = 24.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(32.dp))
                Button(
                    onClick = {
                        Firebase.auth.signOut()
                        onLogout()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("CERRAR SESIÓN", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
        else -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Sección en construcción", color = Color.Gray)
            }
        }
    }
}

@Composable
fun BottomNavigationBar(selectedTab: Int, onTabSelected: (Int) -> Unit) {
    val items = listOf(
        NavigationItem("Inicio", Icons.Default.Home),
        NavigationItem("Favoritos", Icons.Default.Favorite),
        NavigationItem("Agenda", Icons.Default.CalendarMonth),
        NavigationItem("Perfil", Icons.Default.Person)
    )

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color(0xFF1A237E), // Azul Profundo
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEachIndexed { index, item ->
                val isSelected = selectedTab == index
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.clickable { onTabSelected(index) }
                ) {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.label,
                        tint = if (isSelected) Color(0xFF2196F3) else Color.Gray,
                        modifier = Modifier.size(28.dp)
                    )
                    Text(
                        text = item.label,
                        fontSize = 12.sp,
                        color = if (isSelected) Color(0xFF2196F3) else Color.Gray
                    )
                }
            }
        }
    }
}

data class NavigationItem(val label: String, val icon: ImageVector)
