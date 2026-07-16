package cu.todus.app.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
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
import cu.todus.app.data.local.JwtManager
import cu.todus.app.data.remote.ProfileManager
import cu.todus.app.data.remote.ConnectionState
import cu.todus.app.ui.theme.ToDusColors
import kotlinx.coroutines.*

@Composable
fun ProfileScreen(
    phone: String,
    jwt: String,
    onBack: () -> Unit,
    onContinue: () -> Unit,
    onEditProfile: () -> Unit = {}
) {
    var name by remember { mutableStateOf("") }
    var toDusId by remember { mutableStateOf("") }
    var bio by remember { mutableStateOf("") }
    var photoUrl by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    val context = LocalContext.current
    val app = context.applicationContext as ToDusApp
    val jwtManager = remember { JwtManager(context) }

    LaunchedEffect(Unit) {
        try {
            var retries = 0
            while (app.xmppClient.connectionState.value != ConnectionState.CONNECTED && retries < 20) {
                delay(500); retries++
            }
            if (app.xmppClient.connectionState.value == ConnectionState.CONNECTED) {
                app.xmppClient.requestUserInfo(phone)
                val pm = ProfileManager(app.xmppClient)
                pm.getProfile(phone).onSuccess { profile ->
                    name = profile.alias.ifEmpty { phone }
                    toDusId = profile.toDusId
                    bio = profile.description
                    if (profile.photoUrl.isNotEmpty()) photoUrl = profile.photoUrl
                    jwtManager.saveProfile(name, profile.photoUrl, profile.toDusId)
                    jwtManager.saveDescription(bio)
                }.onFailure {
                    name = jwtManager.getAlias() ?: phone
                    photoUrl = jwtManager.getAvatar()
                    toDusId = jwtManager.getToDusId() ?: ""
                    bio = jwtManager.getDescription() ?: ""
                }
            } else {
                name = jwtManager.getAlias() ?: phone
                photoUrl = jwtManager.getAvatar()
                toDusId = jwtManager.getToDusId() ?: ""
                bio = jwtManager.getDescription() ?: ""
            }
        } catch (e: Exception) {
            name = jwtManager.getAlias() ?: phone
            photoUrl = jwtManager.getAvatar()
            toDusId = jwtManager.getToDusId() ?: ""
        }
        isLoading = false
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Fondo con gradiente
        Box(
            modifier = Modifier.fillMaxWidth().height(200.dp).background(
                Brush.verticalGradient(colors = listOf(ToDusColors.Red, ToDusColors.Red.copy(alpha = 0.7f)))
            )
        )

        Column(
            modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())
        ) {
            // Top Bar transparente
            Row(
                modifier = Modifier.fillMaxWidth().statusBarsPadding().padding(horizontal = 8.dp).height(56.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver", tint = Color.White)
                }
                IconButton(onClick = onEditProfile) {
                    Icon(Icons.Default.Edit, "Editar", tint = Color.White)
                }
            }

            // Foto de perfil (sobre el gradiente)
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Box(modifier = Modifier.size(100.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.3f))) {
                    if (photoUrl != null) {
                        AsyncImage(model = photoUrl, contentDescription = "Foto", modifier = Modifier.fillMaxSize().clip(CircleShape), contentScale = ContentScale.Crop)
                    } else {
                        Box(modifier = Modifier.fillMaxSize().clip(CircleShape).background(ToDusColors.Red), contentAlignment = Alignment.Center) {
                            Text(name.first().uppercase(), fontSize = 36.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Nombre y @usuario
            if (isLoading) {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = ToDusColors.Red, modifier = Modifier.padding(32.dp))
                }
            } else {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(name, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground, textAlign = TextAlign.Center)
                    if (toDusId.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("@$toDusId", fontSize = 14.sp, color = MaterialTheme.colorScheme.primary)
                    }
                    if (bio.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(bio, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center, lineHeight = 20.sp)
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Tarjeta de información
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

                Spacer(modifier = Modifier.height(16.dp))

                // Botón continuar
                Button(
                    onClick = {
                        jwtManager.saveJwt(jwt, phone)
                        if (name.isNotBlank()) jwtManager.saveProfile(name, photoUrl ?: "", toDusId)
                        onContinue()
                    },
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = ToDusColors.Red)
                ) {
                    Text("Continuar", style = MaterialTheme.typography.titleMedium, color = Color.White)
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun ProfileInfoRow(icon: ImageVector, label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(label, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
        }
    }
}
