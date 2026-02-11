package servi.work.features.registration.ui

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import servi.work.features.registration.RegistrationState
import servi.work.features.registration.RegistrationViewModel
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegistrationScreen(
    viewModel: RegistrationViewModel = viewModel(),
    onNavigateBack: () -> Unit = {},
    onRegistrationSuccess: () -> Unit = {}
) {
    var dni by remember { mutableStateOf("") }
    var nombre by remember { mutableStateOf("") }
    var apellido by remember { mutableStateOf("") }
    var nacionalidad by remember { mutableStateOf("") }
    var direccion by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var codigoOTP by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var isOtpSent by remember { mutableStateOf(false) }
    var timeLeft by remember { mutableIntStateOf(0) }

    val state by viewModel.state.collectAsState()
    val passwordsMatch = password.isNotEmpty() && password == confirmPassword

    LaunchedEffect(state) {
        when (state) {
            is RegistrationState.OtpSent -> {
                isOtpSent = true
                timeLeft = 60
            }
            is RegistrationState.Success -> {
                Log.d("SERVI_TEST", "LLEGÓ EL ÉXITO")
                onRegistrationSuccess()
            }
            else -> Unit
        }
    }

    LaunchedEffect(timeLeft) {
        if (timeLeft > 0) {
            delay(1000L)
            timeLeft--
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(Color(0xFFF0F2F5))) {
        IconButton(
            onClick = onNavigateBack,
            modifier = Modifier.padding(16.dp).align(Alignment.TopStart)
        ) {
            Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver", tint = Color.Black)
        }

        Card(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 32.dp, vertical = 64.dp).align(Alignment.Center).wrapContentHeight(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier.padding(vertical = 32.dp, horizontal = 24.dp).verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier.size(60.dp).background(Color(0xFF1a1a1a), shape = RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(imageVector = Icons.Default.Build, contentDescription = "Logo", tint = Color.White, modifier = Modifier.size(36.dp))
                }

                Spacer(modifier = Modifier.height(12.dp))
                Text(text = "Crear Cuenta ServiWork", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                Spacer(modifier = Modifier.height(24.dp))

                // --- CAMPOS DE IDENTIDAD ---
                CustomInputField(label = "DNI:", value = dni, onValueChange = { if (it.all { c -> c.isDigit() }) dni = it }, keyboardType = KeyboardType.Number)
                Spacer(modifier = Modifier.height(16.dp))
                
                CustomInputField(label = "Nombre:", value = nombre, onValueChange = { nombre = it })
                Spacer(modifier = Modifier.height(16.dp))
                
                CustomInputField(label = "Apellido:", value = apellido, onValueChange = { apellido = it })
                Spacer(modifier = Modifier.height(16.dp))
                
                CustomInputField(label = "Nacionalidad:", value = nacionalidad, onValueChange = { nacionalidad = it })
                Spacer(modifier = Modifier.height(16.dp))
                
                CustomInputField(label = "Dirección:", value = direccion, onValueChange = { direccion = it })
                Spacer(modifier = Modifier.height(16.dp))

                // --- CAMPO EMAIL Y OTP ---
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(text = "Email:", fontSize = 14.sp, color = Color.Gray)
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        TextField(
                            value = email,
                            onValueChange = { email = it },
                            modifier = Modifier.weight(1f),
                            colors = TextFieldDefaults.colors(focusedContainerColor = Color.Transparent, unfocusedContainerColor = Color.Transparent),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                        )
                        Button(
                            onClick = { viewModel.sendOtpCode(email, nombre) },
                            enabled = email.contains("@") && nombre.isNotEmpty() && state !is RegistrationState.Loading && timeLeft == 0,
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1a1a1a))
                        ) {
                            Text(text = if (timeLeft > 0) "${timeLeft}s" else "ENVIAR", fontSize = 10.sp, color = Color.White)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                CustomInputField(label = "Código de Verificación:", value = codigoOTP, onValueChange = { codigoOTP = it.uppercase() }, enabled = isOtpSent)
                Spacer(modifier = Modifier.height(16.dp))

                // --- CAMPO CONTRASEÑA ---
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(text = "Contraseña:", fontSize = 14.sp, color = Color.Gray)
                    TextField(
                        value = password,
                        onValueChange = { password = it },
                        modifier = Modifier.fillMaxWidth(),
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff, contentDescription = null)
                            }
                        },
                        colors = TextFieldDefaults.colors(focusedContainerColor = Color.Transparent, unfocusedContainerColor = Color.Transparent),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                if (state is RegistrationState.Error) {
                    Text(text = (state as RegistrationState.Error).error, color = Color.Red, fontSize = 12.sp)
                }

                // BOTÓN DE ALTA SINCRONIZADO
                Button(
                    onClick = { 
                        viewModel.signUp(
                            dni = dni,
                            nombre = nombre,
                            apellido = apellido,
                            nacionalidad = nacionalidad,
                            direccion = direccion,
                            emailInput = email,
                            telefono = "", // TODO: Agregar campo si es necesario
                            pass = password,
                            inputOtp = codigoOTP
                        ) 
                    },
                    enabled = dni.isNotEmpty() && nombre.isNotEmpty() && apellido.isNotEmpty() && email.contains("@") && codigoOTP.isNotEmpty() && password.length >= 6,
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1a1a1a))
                ) {
                    if (state is RegistrationState.Loading) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                    } else {
                        Text(text = "CREAR CUENTA", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                TextButton(onClick = onNavigateBack) {
                    Text(text = "¿Ya tienes cuenta? Ingresa aquí", color = Color(0xFF1976D2), fontSize = 14.sp)
                }
            }
        }
    }
}

@Composable
fun CustomInputField(label: String, value: String, onValueChange: (String) -> Unit, keyboardType: KeyboardType = KeyboardType.Text, enabled: Boolean = true) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(text = label, fontSize = 14.sp, color = Color.Gray)
        TextField(
            value = value,
            onValueChange = onValueChange,
            enabled = enabled,
            modifier = Modifier.fillMaxWidth(),
            colors = TextFieldDefaults.colors(focusedContainerColor = Color.Transparent, unfocusedContainerColor = Color.Transparent),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType)
        )
    }
}
