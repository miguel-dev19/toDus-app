package cu.todus.app.ui.screens.contactprofile

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Message
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import cu.todus.app.ToDusApp
import cu.todus.app.data.remote.ConnectionState
import cu.todus.app.data.remote.ProfileManager
import cu.todus.app.ui.theme.ToDusColors
import kotlinx.coroutines.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactProfileScreen(phone: String, onBack: () -> Unit, onMessage: (String, String) -> Unit) {
    var name by remember { mutableStateOf(phone) }
    var toDusId by remember { mutableStateOf("") }
    var bio by remember { mutableStateOf("") }
    var photoUrl by remember { mutableStateOf<String?>(null) }
    var photoThumbUrl by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var isBlocked by remember { mutableStateOf(false) }
    val context = LocalContext.current; val app = context.applicationContext as ToDusApp
    val scope = rememberCoroutineScope()
    val connectionState by app.xmppClient.connectionState.collectAsState()

    // ⭐ CORREGIDO: Callback primero, collect en launch separado
    LaunchedEffect(phone) {
        // 1. Configurar el callback ANTES de cualquier collect
        app.xmppClient.onProfileResponse = { stanza ->
            scope.launch(Dispatchers.IO) {
                val pm = ProfileManager(app.xmppClient)
                pm.getProfile(phone).onSuccess { profile ->
                    withContext(Dispatchers.Main) {
                        name = profile.alias.ifEmpty { phone }
                        toDusId = profile.toDusId
                        bio = profile.description
                        photoUrl = profile.photoUrl
                        photoThumbUrl = profile.photoThumbUrl
                        isLoading = false
                    }
                }.onFailure {
                    withContext(Dispatchers.Main) { isLoading = false }
                }
            }
        }

        // 2. Escuchar conexión en corrutina separada (NO bloquea)
        launch {
            app.xmppClient.connectionState.collect { state ->
                if (state == ConnectionState.CONNECTED) {
                    app.xmppClient.requestUserInfo(phone)
                }
            }
        }
        
        // 3. Si ya está conectado, pedir perfil inmediatamente
        if (app.xmppClient.connectionState.value == ConnectionState.CONNECTED) {
            app.xmppClient.requestUserInfo(phone)
        }
        
        // 4. Timeout de seguridad: si en 10s no carga, mostrar datos básicos
        launch {
            delay(10000)
            if (isLoading) {
                withContext(Dispatchers.Main) { isLoading = false }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Perfil", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver", tint = Color.White)
                    }
                },
                actions = {
                    IconButton(onClick = { /* Compartir */ }) {
                        Icon(Icons.Default.Share, "Compartir", tint = Color.White)
                    }
                    IconButton(onClick = { isBlocked = !isBlocked }) {
                        Icon(
                            if (isBlocked) Icons.Default.Block else Icons.Default.Block,
                            if (isBlocked) "Desbloquear" else "Bloquear",
                            tint = if (isBlocked) ToDusColors.Error else Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = ToDusColors.Red)
            )
        }
    ) { padding ->
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = ToDusColors.Red)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Cargando perfil...", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        } else {
            Column(
                modifier = Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(32.dp))
                
                Box(modifier = Modifier.size(120.dp).clip(CircleShape).background(MaterialTheme.colorScheme.surfaceVariant), contentAlignment = Alignment.Center) {
                    if (photoUrl != null) {
                        AsyncImage(model = photoUrl, contentDescription = "Foto de perfil", modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                    } else {
                        Text(name.first().uppercase(), fontSize = 42.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f))
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
                Text(name, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground, textAlign = TextAlign.Center)
                
                if (toDusId.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("@$toDusId", fontSize = 14.sp, color = MaterialTheme.colorScheme.primary)
                }
                
                Spacer(modifier = Modifier.height(4.dp))
                Text(phone, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                
                if (bio.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(bio, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center, lineHeight = 20.sp, modifier = Modifier.padding(horizontal = 32.dp))
                }

                Spacer(modifier = Modifier.height(32.dp))

                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Button(
                        onClick = { onMessage(phone, name) },
                        modifier = Modifier.weight(1f).height(48.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = ToDusColors.Red)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Message, null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Enviar mensaje", color = Color.White, fontSize = 14.sp)
                    }

                    OutlinedButton(
                        onClick = { /* Llamar */ },
                        modifier = Modifier.size(48.dp),
                        shape = RoundedCornerShape(14.dp),
                        border = ButtonDefaults.outlinedButtonBorder,
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Icon(Icons.Default.Call, "Llamar", tint = ToDusColors.Green, modifier = Modifier.size(20.dp))
                    }

                    OutlinedButton(
                        onClick = { /* Videollamada */ },
                        modifier = Modifier.size(48.dp),
                        shape = RoundedCornerShape(14.dp),
                        border = ButtonDefaults.outlinedButtonBorder,
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Icon(Icons.Default.Videocam, "Videollamada", tint = ToDusColors.Red, modifier = Modifier.size(20.dp))
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Card(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        ProfileInfoRow(Icons.Default.Call, "Teléfono", phone)
                        if (toDusId.isNotEmpty()) ProfileInfoRow(Icons.Default.AlternateEmail, "Usuario", "@$toDusId")
                        ProfileInfoRow(Icons.Default.Info, "Bio", bio.ifEmpty { "Sin descripción" })
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Card(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Column {
                        ProfileActionRow(Icons.Default.NotificationsOff, "Silenciar notificaciones") { /* TODO */ }
                        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                        ProfileActionRow(Icons.Default.Delete, "Eliminar contacto") { /* TODO */ }
                        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                        ProfileActionRow(
                            if (isBlocked) Icons.Default.Block else Icons.Default.Block,
                            if (isBlocked) "Desbloquear contacto" else "Bloquear contacto",
                            textColor = ToDusColors.Error
                        ) { isBlocked = !isBlocked }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
fun ProfileInfoRow(icon: ImageVector, label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(label, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
        }
    }
}

@Composable
fun ProfileActionRow(icon: ImageVector, label: String, textColor: Color = MaterialTheme.colorScheme.onSurface, onClick: () -> Unit) {
    Surface(modifier = Modifier.fillMaxWidth().clickable(onClick = onClick), color = Color.Transparent) {
        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, label, tint = textColor.copy(alpha = 0.7f), modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(16.dp))
            Text(label, fontSize = 14.sp, color = textColor)
        }
    }
}
