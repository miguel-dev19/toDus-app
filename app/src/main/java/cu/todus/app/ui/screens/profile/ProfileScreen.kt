package cu.todus.app.ui.screens.profile

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import cu.todus.app.data.local.JwtManager
import cu.todus.app.data.remote.XmppClient
import cu.todus.app.ui.theme.ToDusColors
import kotlinx.coroutines.*

@Composable
fun ProfileScreen(
    phone: String,
    jwt: String,
    onBack: () -> Unit,
    onContinue: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var toDusId by remember { mutableStateOf("") }
    var photoUrl by remember { mutableStateOf<String?>(null) }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var isUploading by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(true) }
    val context = LocalContext.current
    val xmppClient = remember { XmppClient() }

    val imagePicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri -> uri?.let { selectedImageUri = it } }

    LaunchedEffect(phone, jwt) {
        if (phone.isNotEmpty() && jwt.isNotEmpty()) {
            try {
                xmppClient.connect(phone, jwt)
                // Intentar cargar perfil del servidor (si falla, se queda vacío)
                isLoading = false
            } catch (e: Exception) { isLoading = false }
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = ToDusColors.Red) }
        } else {
            Column(modifier = Modifier.fillMaxSize().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                IconButton(onClick = onBack, modifier = Modifier.size(40.dp).align(Alignment.Start)) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver", tint = MaterialTheme.colorScheme.onBackground) }
                Spacer(modifier = Modifier.height(40.dp))
                Text("Mi perfil", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.onBackground)
                Spacer(modifier = Modifier.height(40.dp))
                Box(modifier = Modifier.size(120.dp), contentAlignment = Alignment.Center) {
                    Surface(modifier = Modifier.fillMaxSize().clip(CircleShape), shape = CircleShape, color = MaterialTheme.colorScheme.surfaceVariant) {
                        val imageToShow = selectedImageUri ?: photoUrl?.let { Uri.parse(it) }
                        if (imageToShow != null) {
                            AsyncImage(model = imageToShow, contentDescription = "Foto", modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                        } else {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Icon(Icons.Default.Person, null, modifier = Modifier.size(60.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)) }
                        }
                    }
                    FloatingActionButton(onClick = { imagePicker.launch("image/*") }, modifier = Modifier.align(Alignment.BottomEnd).offset(x = 4.dp, y = 4.dp).size(36.dp), containerColor = ToDusColors.Red, shape = CircleShape) { Icon(Icons.Default.CameraAlt, "Cambiar foto", modifier = Modifier.size(18.dp), tint = ToDusColors.White) }
                }
                Spacer(modifier = Modifier.height(32.dp))
                OutlinedTextField(value = name, onValueChange = { name = it.take(50) }, modifier = Modifier.fillMaxWidth(), label = { Text("Tu nombre") }, placeholder = { Text("Como te llamas?") }, singleLine = true, leadingIcon = { Icon(Icons.Default.Person, null) }, shape = RoundedCornerShape(12.dp), colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MaterialTheme.colorScheme.primary, unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)))
                if (toDusId.isNotEmpty()) { Spacer(modifier = Modifier.height(8.dp)); Text("@$toDusId", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary) }
            }
            Column(modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth().padding(24.dp, 32.dp)) {
                Button(onClick = {
                    JwtManager(context).saveJwt(jwt, phone)
                    onContinue()
                }, modifier = Modifier.fillMaxWidth().height(52.dp), enabled = name.isNotBlank(), shape = RoundedCornerShape(12.dp), colors = ButtonDefaults.buttonColors(containerColor = ToDusColors.Red)) { Text("Continuar", style = MaterialTheme.typography.titleMedium) }
            }
        }
    }
}
