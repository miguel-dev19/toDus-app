package cu.todus.app.ui.screens.phone

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
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

    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Column(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Column(modifier = Modifier.padding(24.dp).fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                IconButton(onClick = onBack, modifier = Modifier.size(40.dp).align(Alignment.Start)) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver", tint = MaterialTheme.colorScheme.onBackground) }
                Spacer(modifier = Modifier.height(32.dp))
                Text("Ingresar numero", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
                Spacer(modifier = Modifier.height(8.dp))
                Text("Por favor, ingresa tu numero de telefono para continuar", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f))
                Spacer(modifier = Modifier.height(32.dp))
                Row(modifier = Modifier.fillMaxWidth().height(56.dp), verticalAlignment = Alignment.CenterVertically) {
                    Row(modifier = Modifier.width(100.dp).height(56.dp).clip(RoundedCornerShape(12.dp)).background(MaterialTheme.colorScheme.surfaceVariant).border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), RoundedCornerShape(12.dp)).padding(horizontal = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text("\uD83C\uDDE8\uD83C\uDDFA", fontSize = 18.sp); Spacer(modifier = Modifier.width(6.dp))
                        Text("+53", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    OutlinedTextField(value = phone, onValueChange = { if (it.length <= 8) phone = it.filter { c -> c.isDigit() } }, modifier = Modifier.weight(1f).focusRequester(focusRequester), textStyle = MaterialTheme.typography.titleMedium.copy(color = MaterialTheme.colorScheme.onBackground), singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone), shape = RoundedCornerShape(12.dp), isError = errorMsg != null, colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = if (errorMsg != null) ToDusColors.Error else MaterialTheme.colorScheme.primary, unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)))
                }
                if (phone.isNotEmpty() && !isValid) { Spacer(modifier = Modifier.height(8.dp)); Row(verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Default.ErrorOutline, null, tint = ToDusColors.Error, modifier = Modifier.size(16.dp)); Spacer(modifier = Modifier.width(4.dp)); Text(if (phone.length < 8) "El numero debe tener 8 digitos" else "El numero debe comenzar con 5", style = MaterialTheme.typography.bodySmall, color = ToDusColors.Error) } }
                if (errorMsg != null) { Spacer(modifier = Modifier.height(8.dp)); Surface(color = ToDusColors.Error.copy(alpha = 0.1f), shape = RoundedCornerShape(8.dp)) { Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Default.ErrorOutline, null, tint = ToDusColors.Error, modifier = Modifier.size(20.dp)); Spacer(modifier = Modifier.width(8.dp)); Text(errorMsg!!, style = MaterialTheme.typography.bodySmall, color = ToDusColors.Error) } } }
                if (isLoading) { Spacer(modifier = Modifier.height(16.dp)); Row(verticalAlignment = Alignment.CenterVertically) { CircularProgressIndicator(modifier = Modifier.size(16.dp), color = ToDusColors.Red, strokeWidth = 2.dp); Spacer(modifier = Modifier.width(8.dp)); Text(when (step) { 1 -> "Verificando numero..."; 2 -> "Conectando..."; else -> "Procesando..." }, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) } }
            }
            Spacer(modifier = Modifier.weight(1f))
            Column(modifier = Modifier.fillMaxWidth().padding(24.dp, 32.dp)) {
                Button(onClick = {
                    isLoading = true; errorMsg = null; step = 1; val fullPhone = "53$phone"
                    CoroutineScope(Dispatchers.IO).launch {
                        app.xmppClient.authenticate(fullPhone).onSuccess { jwt ->
                            jwtManager.saveJwt(jwt, fullPhone)
                            step = 2
                            app.xmppClient.connect(fullPhone, jwt).onSuccess {
                                withContext(Dispatchers.Main) {
                                    isLoading = false
                                    onContinue(fullPhone, jwt)
                                }
                            }.onFailure { e ->
                                withContext(Dispatchers.Main) {
                                    isLoading = false
                                    errorMsg = "Error de conexion: ${e.message}"
                                }
                            }
                        }.onFailure {
                            withContext(Dispatchers.Main) {
                                isLoading = false
                                errorMsg = "No se pudo verificar el numero"
                            }
                        }
                    }
                }, modifier = Modifier.fillMaxWidth().height(52.dp), enabled = isValid && !isLoading, shape = RoundedCornerShape(12.dp), colors = ButtonDefaults.buttonColors(containerColor = ToDusColors.Red)) {
                    if (isLoading) CircularProgressIndicator(modifier = Modifier.size(24.dp), color = ToDusColors.White) else Text("Continuar", style = MaterialTheme.typography.titleMedium)
                }
            }
        }
    }
}
