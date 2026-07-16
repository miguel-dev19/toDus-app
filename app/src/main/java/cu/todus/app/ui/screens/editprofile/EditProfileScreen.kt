package cu.todus.app.ui.screens.editprofile

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import cu.todus.app.ToDusApp
import cu.todus.app.data.local.JwtManager
import cu.todus.app.data.remote.S3Uploader
import cu.todus.app.ui.theme.ToDusColors
import kotlinx.coroutines.*

@Composable
fun EditProfileScreen(onBack: () -> Unit, onSaved: () -> Unit) {
    val context = LocalContext.current
    val app = context.applicationContext as ToDusApp
    val jwtManager = remember { JwtManager(context) }
    
    var name by remember { mutableStateOf(jwtManager.getAlias() ?: jwtManager.getPhone() ?: "") }
    var bio by remember { mutableStateOf(jwtManager.getDescription() ?: "") }
    var photoUrl by remember { mutableStateOf(jwtManager.getAvatar()) }
    var isSaving by remember { mutableStateOf(false) }
    var photoUri by remember { mutableStateOf<Uri?>(null) }
    
    val phone = jwtManager.getPhone() ?: ""
    val jwt = jwtManager.getJwt() ?: ""
    val todusId = jwtManager.getToDusId() ?: ""

    val imagePicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) photoUri = uri
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier.fillMaxWidth().height(180.dp).background(
                Brush.verticalGradient(colors = listOf(ToDusColors.Red, ToDusColors.Red.copy(alpha = 0.7f)))
            )
        )

        Column(
            modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())
        ) {
            // Top Bar
            Row(
                modifier = Modifier.fillMaxWidth().statusBarsPadding().padding(horizontal = 8.dp).height(56.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver", tint = Color.White)
                }
                Text("Editar perfil", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                TextButton(onClick = {
                    isSaving = true
                    CoroutineScope(Dispatchers.IO).launch {
                        if (photoUri != null) {
                            val s3 = S3Uploader(app.xmppClient)
                            s3.uploadProfileImage(photoUri!!, context, phone, jwt).onSuccess { url ->
                                photoUrl = url
                            }
                        }
                        jwtManager.saveProfile(name, photoUrl ?: "", todusId)
                        jwtManager.saveDescription(bio)
                        withContext(Dispatchers.Main) { isSaving = false; onSaved() }
                    }
                }, enabled = !isSaving) {
                    if (isSaving) CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White, strokeWidth = 2.dp)
                    else Text("Guardar", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }

            // Foto
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                Box(modifier = Modifier.size(100.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.3f))) {
                    if (photoUri != null) {
                        AsyncImage(model = photoUri, contentDescription = "Nueva foto", modifier = Modifier.fillMaxSize().clip(CircleShape), contentScale = ContentScale.Crop)
                    } else if (photoUrl != null) {
                        AsyncImage(model = photoUrl, contentDescription = "Foto", modifier = Modifier.fillMaxSize().clip(CircleShape), contentScale = ContentScale.Crop)
                    } else {
                        Box(modifier = Modifier.fillMaxSize().clip(CircleShape).background(ToDusColors.Red), contentAlignment = Alignment.Center) {
                            Text(name.first().uppercase(), fontSize = 36.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            TextButton(
                onClick = { imagePicker.launch("image/*") },
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                Icon(Icons.Default.CameraAlt, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Cambiar foto", color = MaterialTheme.colorScheme.primary)
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Formulario
            Card(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    OutlinedTextField(
                        value = name, onValueChange = { name = it.take(50) },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Nombre") },
                        leadingIcon = { Icon(Icons.Default.Person, null) },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = bio, onValueChange = { bio = it.take(200) },
                        modifier = Modifier.fillMaxWidth().height(100.dp),
                        label = { Text("Biografía") },
                        leadingIcon = { Icon(Icons.Default.Info, null) },
                        maxLines = 4,
                        shape = RoundedCornerShape(12.dp)
                    )
                    if (todusId.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedTextField(
                            value = "@$todusId", onValueChange = {},
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("Usuario") },
                            leadingIcon = { Icon(Icons.Default.AlternateEmail, null) },
                            enabled = false,
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
