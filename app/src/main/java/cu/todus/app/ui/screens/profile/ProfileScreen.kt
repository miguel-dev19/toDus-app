package cu.todus.app.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
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
import cu.todus.app.ToDusApp
import cu.todus.app.data.local.JwtManager
import cu.todus.app.data.remote.ProfileManager
import cu.todus.app.data.remote.ConnectionState
import cu.todus.app.ui.theme.ToDusColors
import kotlinx.coroutines.*

@Composable
fun ProfileScreen(phone: String, jwt: String, onBack: () -> Unit, onContinue: () -> Unit, onEditProfile: () -> Unit = {}) {
    var name by remember { mutableStateOf("") }
    var toDusId by remember { mutableStateOf("") }
    var photoUrl by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    val context = LocalContext.current
    val app = context.applicationContext as ToDusApp
    val jwtManager = remember { JwtManager(context) }

    LaunchedEffect(phone, jwt) {
        try {
            val cs = app.xmppClient.connectionState.value
            if (cs == ConnectionState.CONNECTED) {
                val pm = ProfileManager(app.xmppClient)
                pm.getProfile(phone).onSuccess { profile ->
                    name = profile.alias.ifEmpty { phone }
                    toDusId = profile.toDusId
                    if (profile.photoUrl.isNotEmpty()) photoUrl = profile.photoUrl
                    jwtManager.saveProfile(name, profile.photoUrl, profile.toDusId)
                }.onFailure {
                    name = jwtManager.getAlias() ?: phone
                    photoUrl = jwtManager.getAvatar()
                }
            } else {
                name = jwtManager.getAlias() ?: phone
                photoUrl = jwtManager.getAvatar()
            }
        } catch (e: Exception) {
            name = jwtManager.getAlias() ?: phone
            photoUrl = jwtManager.getAvatar()
        }
        isLoading = false
    }

    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = ToDusColors.Red)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Cargando perfil...")
                }
            }
        } else {
            Column(modifier = Modifier.fillMaxSize().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver") }
                    TextButton(onClick = onEditProfile) { Icon(Icons.Default.Edit, null); Spacer(modifier = Modifier.width(4.dp)); Text("Editar") }
                }
                Spacer(modifier = Modifier.height(40.dp))
                Text("Mi perfil", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(40.dp))
                Box(modifier = Modifier.size(120.dp), contentAlignment = Alignment.Center) {
                    Surface(modifier = Modifier.fillMaxSize().clip(CircleShape), shape = CircleShape, color = MaterialTheme.colorScheme.surfaceVariant) {
                        if (photoUrl != null) AsyncImage(model = photoUrl, contentDescription = "Foto", modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                        else Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Icon(Icons.Default.Person, null, modifier = Modifier.size(60.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)) }
                    }
                }
                Spacer(modifier = Modifier.height(32.dp))
                OutlinedTextField(value = name, onValueChange = { name = it.take(50) }, modifier = Modifier.fillMaxWidth(), label = { Text("Nombre") }, singleLine = true, shape = RoundedCornerShape(12.dp))
                if (toDusId.isNotEmpty()) { Spacer(modifier = Modifier.height(8.dp)); Text("@$toDusId", color = MaterialTheme.colorScheme.primary) }
            }
            Column(modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth().padding(24.dp, 32.dp)) {
                Button(onClick = { jwtManager.saveJwt(jwt, phone); if (name.isNotBlank()) jwtManager.saveProfile(name, photoUrl ?: "", toDusId); onContinue() }, modifier = Modifier.fillMaxWidth().height(52.dp), enabled = name.isNotBlank(), shape = RoundedCornerShape(12.dp), colors = ButtonDefaults.buttonColors(containerColor = ToDusColors.Red)) { Text("Continuar") }
            }
        }
    }
}
