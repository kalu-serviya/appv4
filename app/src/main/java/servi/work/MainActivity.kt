package servi.work

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import servi.work.features.auth.ui.LoginScreen
import servi.work.features.dashboard.ui.MainDashboardScreen
import servi.work.features.registration.ui.RegistrationScreen
import servi.work.ui.theme.ServiWorkTheme

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ServiWorkTheme {
                val navController = rememberNavController()
                
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = "login",
                        modifier = Modifier.padding(innerPadding)
                    ) {
                        composable("login") {
                            LoginScreen(
                                onNavigateToRegister = { navController.navigate("registration") },
                                onLoginSuccess = { role ->
                                    navController.navigate("main_dashboard") {
                                        popUpTo("login") { inclusive = true }
                                    }
                                }
                            )
                        }
                        composable("registration") {
                            RegistrationScreen(
                                onNavigateBack = { navController.popBackStack() },
                                onRegistrationSuccess = {
                                    navController.navigate("login") {
                                        popUpTo("registration") { inclusive = true }
                                    }
                                }
                            )
                        }
                        composable("main_dashboard") {
                            MainDashboardScreen()
                        }
                    }
                }
            }
        }
    }
}
