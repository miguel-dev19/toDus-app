package cu.todus.app.ui.screens.phone

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cu.todus.app.ToDusApp
import cu.todus.app.data.local.JwtManager
import cu.todus.app.ui.theme.ToDusColors
import kotlinx.coroutines.*

@Composable
fun PhoneInputScreen(onBack: () -> Unit, onContinue: (String, String) -> Unit) {
    var phone by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMsg by remember { mutableStateOf<String?>(null) }
    var step by remember { mutableStateOf(0) }
    val isValid = phone.length == 8 && phone.startsWith("5")
    val focusRequester = remember { FocusRequester() }
    val context = LocalContext.current
    val app = context.applicationContext as ToDusApp
    val jwtManager = remember { JwtManager(context) }

    LaunchedEffect(Unit) { focusRequester.requestFocus() }

    Box(
        modifier = Modifier.fillMaxSize().background(
            Brush.verticalGradient(colors = listOf(Color(0xFF1A1A2E), Color(0xFF16213E), Color(0xFF0F3460)))
        )
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier.size(40.dp).align(Alignment.Start)
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver", tint = Color.White)
            }

            Spacer(modifier = Modifier.height(40.dp))

            Box(
                modifier = Modifier.size(80.dp).clip(RoundedCornerShape(20.dp)).background(Color.White.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Phone, null, tint = Color.White, modifier = Modifier.size(36.dp))
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                "Ingresa tu número",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                "Te enviaremos un código de verificación",
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(40.dp))

            // Input de teléfono
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.1f))
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().height(60.dp).padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        color = Color.White.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(
                            "🇨🇺 +53",
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    OutlinedTextField(
                        value = phone,
                        onValueChange = { if (it.length <= 8) phone = it.filter { c -> c.isDigit() } },
                        modifier = Modifier.weight(1f).focusRequester(focusRequester),
                        textStyle = MaterialTheme.typography.titleMedium.copy(color = Color.White, fontSize = 20.sp, letterSpacing = 4.sp),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color.Transparent,
                            unfocusedBorderColor = Color.Transparent,
                            cursorColor = Color.White
                        ),
                        placeholder = { Text("5XXXXXXXX", color = Color.White.copy(alpha = 0.3f), fontSize = 20.sp, letterSpacing = 4.sp) }
                    )
                }
            }

            // Errores
            AnimatedVisibility(visible = phone.isNotEmpty() && !isValid) {
                Row(
                    modifier = Modifier.padding(top = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.ErrorOutline, null, tint = ToDusColors.Error, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        if (phone.length < 8) "El número debe tener 8 dígitos" else "El número debe comenzar con 5",
                        style = MaterialTheme.typography.bodySmall,
                        color = ToDusColors.Error
                    )
                }
            }

            AnimatedVisibility(visible = errorMsg != null) {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = ToDusColors.Error.copy(alpha = 0.2f))
                ) {
                    Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.ErrorOutline, null, tint = ToDusColors.Error, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(errorMsg!!, style = MaterialTheme.typography.bodySmall, color = ToDusColors.Error)
                    }
                }
            }

            if (isLoading) {
                Spacer(modifier = Modifier.height(16.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White, strokeWidth = 2.dp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        when (step) { 1 -> "Verificando número..."; 2 -> "Conectando..."; else -> "Procesando..." },
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                }
            }
        }

        // Botón al fondo
        Column(
            modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth().padding(24.dp, 32.dp)
        ) {
            Button(
                onClick = {
                    isLoading = true; errorMsg = null; step = 1; val fullPhone = "53$phone"
                    CoroutineScope(Dispatchers.IO).launch {
                        app.xmppClient.authenticate(fullPhone).onSuccess { jwt ->
                            jwtManager.saveJwt(jwt, fullPhone)
                            step = 2
                            app.xmppClient.connect(fullPhone, jwt).onSuccess {
                                withContext(Dispatchers.Main) { isLoading = false; onContinue(fullPhone, jwt) }
                            }.onFailure { e ->
                                withContext(Dispatchers.Main) { isLoading = false; errorMsg = "Error de conexión: ${e.message}" }
                            }
                        }.onFailure {
                            withContext(Dispatchers.Main) { isLoading = false; errorMsg = "No se pudo verificar el número" }
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                enabled = isValid && !isLoading,
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = ToDusColors.Red)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
                } else {
                    Text("Continuar", style = MaterialTheme.typography.titleMedium, color = Color.White)
                }
            }
        }
    }
}
