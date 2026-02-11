package servi.work.features.registration.ui

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun IdentityVerificationScreen(
    onNavigateBack: () -> Unit = {},
    onProceedToSelfie: () -> Unit = {}
) {
    val context = LocalContext.current
    var isConfirmed by remember { mutableStateOf(false) }

    val nombre = "JUAN"
    val apellido = "PÉREZ"
    val dni = "12.345.678"
    val nacionalidad = "ARGENTINA"
    val sexo = "MASCULINO"
    val direccion = "CALLE FALSA 123, CABA"

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF0F2F5))
    ) {
        IconButton(
            onClick = onNavigateBack,
            modifier = Modifier
                .padding(16.dp)
                .align(Alignment.TopStart)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Volver",
                tint = Color.Black
            )
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp, vertical = 64.dp)
                .align(Alignment.Center)
                .wrapContentHeight(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(vertical = 32.dp, horizontal = 24.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = Color(0xFF1a1a1a),
                    modifier = Modifier.size(48.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Verificación de Identidad",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    textAlign = TextAlign.Center
                )
                
                Text(
                    text = "Confirmá que los datos coincidan con tu DNI para ServiWork",
                    fontSize = 14.sp,
                    color = Color.Gray,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 8.dp)
                )

                Spacer(modifier = Modifier.height(24.dp))

                ReadOnlyField(label = "Nombre:", value = nombre)
                ReadOnlyField(label = "Apellido:", value = apellido)
                ReadOnlyField(label = "DNI:", value = dni)
                ReadOnlyField(label = "Nacionalidad:", value = nacionalidad)
                ReadOnlyField(label = "Sexo:", value = sexo)
                ReadOnlyField(label = "Dirección:", value = direccion)

                Spacer(modifier = Modifier.height(16.dp))

                TextButton(
                    onClick = {
                        val intent = Intent(Intent.ACTION_SENDTO).apply {
                            data = Uri.parse("mailto:soporte@serviwork.com")
                            putExtra(Intent.EXTRA_SUBJECT, "Modificación de Datos - ServiWork")
                            putExtra(Intent.EXTRA_TEXT, "Hola, solicito la modificación de mis datos de identidad en ServiWork.")
                        }
                        context.startActivity(intent)
                    }
                ) {
                    Text(
                        text = "¿Tus datos están mal copiados?",
                        color = Color(0xFFFF4D6D),
                        fontSize = 13.sp,
                        textDecoration = TextDecoration.Underline
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Checkbox(
                        checked = isConfirmed,
                        onCheckedChange = { isConfirmed = it },
                        colors = CheckboxDefaults.colors(checkedColor = Color(0xFF1a1a1a))
                    )
                    Text(
                        text = "Los datos son fieles a mi documento",
                        fontSize = 13.sp,
                        color = Color.Black
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = onProceedToSelfie,
                    enabled = isConfirmed,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF1a1a1a),
                        disabledContainerColor = Color.LightGray
                    )
                ) {
                    Text(
                        text = "PROCEDER A SELFIE",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}

@Composable
fun ReadOnlyField(label: String, value: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
    ) {
        Text(text = label, fontSize = 12.sp, color = Color.Gray)
        TextField(
            value = value,
            onValueChange = {},
            enabled = false,
            modifier = Modifier.fillMaxWidth(),
            colors = TextFieldDefaults.colors(
                disabledContainerColor = Color.Transparent,
                disabledIndicatorColor = Color(0xFFE0E0E0),
                disabledTextColor = Color.Black
            ),
            singleLine = true
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewIdentityVerification() {
    IdentityVerificationScreen()
}
