package cu.todus.app.ui.screens.contactprofile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import cu.todus.app.ToDusApp
import cu.todus.app.data.remote.ProfileManager
import cu.todus.app.data.remote.ConnectionState
import cu.todus.app.ui.theme.ToDusColors
import kotlinx.coroutines.*

@Composable
fun ContactProfileScreen(phone: String, onBack: () -> Unit, onMessage: (String, String) -> Unit) {
    var name by remember { mutableStateOf(phone) }
    var toDusId by remember { mutableStateOf("") }
    var bio by remember { mutableStateOf("") }
    var photoUrl by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    val context = LocalContext.current; val app = context.applicationContext as ToDusApp

    LaunchedEffect(phone) {
        try {
            var retries = 0
            while (app.xmppClient.connectionState.value != ConnectionState.CONNECTED && retries < 20) { delay(500); retries++ }
            if (app.xmppClient.connectionState.value == ConnectionState.CONNECTED) {
                app.xmppClient.requestUserInfo(phone)
                val pm = ProfileManager(app.xmppClient)
                pm.getProfile(phone).onSuccess { profile ->
                    name = profile.alias.ifEmpty { phone }; toDusId = profile.toDusId; bio = profile.description
                    if (profile.photoUrl.isNotEmpty()) photoUrl = profile.photoUrl
                }
            }
        } catch (_: Exception) {}
        isLoading = false
    }

    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
            Surface(shadowElevation = 2.dp) {
                Row(modifier = Modifier.fillMaxWidth().statusBarsPadding().height(56.dp).padding(start = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver") }
                    Text("Perfil", style = MaterialTheme.typography.titleMedium)
                }
            }
            if (isLoading) {
                Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = ToDusColors.Red) }
            } else {
                Column(modifier = Modifier.fillMaxWidth().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Spacer(modifier = Modifier.height(32.dp))
                    Box(modifier = Modifier.size(100.dp).clip(CircleShape).background(MaterialTheme.colorScheme.surfaceVariant), contentAlignment = Alignment.Center) {
                        if (photoUrl != null) AsyncImage(model = photoUrl, contentDescription = "Foto", modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                        else Text(name.first().uppercase(), fontSize = 36.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Spacer(modifier = Modifier.height(20.dp))
                    Text(name, fontSize = 22.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.onBackground)
                    if (toDusId.isNotEmpty()) { Spacer(modifier = Modifier.height(4.dp)); Text("@$toDusId", fontSize = 14.sp, color = MaterialTheme.colorScheme.primary) }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(phone, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    if (bio.isNotEmpty()) { Spacer(modifier = Modifier.height(16.dp)); Text(bio, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center, lineHeight = 20.sp) }
                }
            }
        }
    }
}
