package cu.todus.app.ui.screens.editprofile

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import cu.todus.app.ToDusApp
import cu.todus.app.data.local.JwtManager
import cu.todus.app.data.remote.S3Uploader
import cu.todus.app.ui.theme.ToDusColors
import kotlinx.coroutines.*

@Composable
fun EditProfileScreen(onBack: () -> Unit, onSaved: () -> Unit) {
    val context = LocalContext.current; val app = context.applicationContext as ToDusApp
    val jwtManager = remember { JwtManager(context) }
    var name by remember { mutableStateOf(jwtManager.getAlias() ?: jwtManager.getPhone() ?: "") }
    var bio by remember { mutableStateOf(jwtManager.getDescription() ?: "") }
    var photoUrl by remember { mutableStateOf(jwtManager.getAvatar()) }
    var isSaving by remember { mutableStateOf(false) }; var photoUri by remember { mutableStateOf<Uri?>(null) }
    val phone = jwtManager.getPhone() ?: ""; val jwt = jwtManager.getJwt() ?: ""; val todusId = jwtManager.getToDusId() ?: ""
    val imagePicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri -> if (uri != null) photoUri = uri }

    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Column(modifier = Modifier.fillMaxSize()) {
            Surface(shadowElevation = 2.dp) {
                Row(modifier = Modifier.fillMaxWidth().statusBarsPadding().height(56.dp).padding(horizontal = 8.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver") }
                    Text("Editar Perfil", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    TextButton(onClick = {
                        isSaving = true
                        CoroutineScope(Dispatchers.IO).launch {
                            if (photoUri != null) { val s3 = S3Uploader(app.xmppClient); s3.uploadProfileImage(photoUri!!, context, phone, jwt).onSuccess { url -> photoUrl = url } }
                            jwtManager.saveProfile(name, photoUrl ?: "", todusId); jwtManager.saveDescription(bio)
                            withContext(Dispatchers.Main) { isSaving = false; onSaved() }
                        }
                    }, enabled = !isSaving) { if (isSaving) CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp) else Text("Guardar", fontWeight = FontWeight.Bold) }
                }
            }
            Column(modifier = Modifier.fillMaxSize().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Spacer(modifier = Modifier.height(20.dp))
                Box(modifier = Modifier.size(100.dp), contentAlignment = Alignment.Center) {
                    Surface(modifier = Modifier.fillMaxSize().clip(CircleShape), shape = CircleShape, color = MaterialTheme.colorScheme.surfaceVariant) {
                        if (photoUri != null) AsyncImage(model = photoUri, contentDescription = "Foto", modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                        else if (photoUrl != null) AsyncImage(model = photoUrl, contentDescription = "Foto", modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                        else Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Icon(Icons.Default.Person, null, modifier = Modifier.size(50.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)) }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                TextButton(onClick = { imagePicker.launch("image/*") }) { Icon(Icons.Default.Edit, null, modifier = Modifier.size(16.dp)); Spacer(modifier = Modifier.width(4.dp)); Text("Cambiar foto") }
                Spacer(modifier = Modifier.height(24.dp))
                OutlinedTextField(value = name, onValueChange = { name = it.take(50) }, modifier = Modifier.fillMaxWidth(), label = { Text("Nombre") }, leadingIcon = { Icon(Icons.Default.Person, null) }, singleLine = true, shape = RoundedCornerShape(12.dp))
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(value = bio, onValueChange = { bio = it.take(200) }, modifier = Modifier.fillMaxWidth().height(100.dp), label = { Text("Biografía") }, leadingIcon = { Icon(Icons.Default.Info, null) }, maxLines = 4, shape = RoundedCornerShape(12.dp))
                if (todusId.isNotEmpty()) { Spacer(modifier = Modifier.height(12.dp)); OutlinedTextField(value = "@$todusId", onValueChange = {}, modifier = Modifier.fillMaxWidth(), label = { Text("Usuario") }, leadingIcon = { Icon(Icons.Default.AlternateEmail, null) }, enabled = false, singleLine = true, shape = RoundedCornerShape(12.dp)) }
            }
        }
    }
}
