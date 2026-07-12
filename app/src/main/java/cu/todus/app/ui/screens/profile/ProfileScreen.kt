package cu.todus.app.ui.screens.profile

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import cu.todus.app.data.local.JwtManager
import cu.todus.app.ui.theme.ToDusColors

@Composable
fun ProfileScreen(
    onBack: () -> Unit,
    onContinue: () -> Unit,
    phone: String = "",
    jwt: String = ""
) {
    var name by remember { mutableStateOf("") }
    val context = LocalContext.current

    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Column(modifier = Modifier.fillMaxSize().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            IconButton(onClick = onBack, modifier = Modifier.size(40.dp).align(Alignment.Start)) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver")
            }
            Spacer(modifier = Modifier.height(40.dp))
            Text("Mi perfil", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
            Spacer(modifier = Modifier.height(40.dp))
            Box(modifier = Modifier.size(120.dp), contentAlignment = Alignment.Center) {
                Surface(modifier = Modifier.fillMaxSize().clip(CircleShape), shape = CircleShape, color = MaterialTheme.colorScheme.surfaceVariant, border = BorderStroke(2.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Person, null, modifier = Modifier.size(60.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f))
                    }
                }
                FloatingActionButton(onClick = {}, modifier = Modifier.align(Alignment.BottomEnd).offset(x = 4.dp, y = 4.dp).size(36.dp), containerColor = ToDusColors.Red, shape = CircleShape) {
                    Icon(Icons.Default.CameraAlt, "Cambiar foto", modifier = Modifier.size(18.dp), tint = ToDusColors.White)
                }
            }
            Spacer(modifier = Modifier.height(32.dp))
            OutlinedTextField(value = name, onValueChange = { name = it.take(50) }, modifier = Modifier.fillMaxWidth(), label = { Text("Tu nombre") }, placeholder = { Text("Como te llamas?") }, singleLine = true, leadingIcon = { Icon(Icons.Default.Person, null) }, shape = RoundedCornerShape(12.dp))
        }
        Column(modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth().padding(24.dp, 32.dp)) {
            Button(
                onClick = {
                    // Guardar JWT al completar perfil
                    if (phone.isNotEmpty() && jwt.isNotEmpty()) {
                        JwtManager(context).saveJwt(jwt, phone)
                    }
                    onContinue()
                },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                enabled = name.isNotBlank(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = ToDusColors.Red)
            ) {
                Text("Continuar", style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}
