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
    val phone = remember { jwtManager.getPhone() ?: "" }
    val jwt = remember { jwtManager.getJwt() ?: "" }
    
    var alias by remember { mutableStateOf(jwtManager.getAlias() ?: "") }
    var bio by remember { mutableStateOf(jwtManager.getDescription() ?: "") }
    var photoUrl by remember { mutableStateOf(jwtManager.getAvatar()) }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var isSaving by remember { mutableStateOf(false) }
    var errorMsg by remember { mutableStateOf<String?>(null) }
    var successMsg by remember { mutableStateOf<String?>(null) }

    val imagePicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri -> uri?.let { selectedImageUri = it } }

    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Column(modifier = Modifier.fillMaxSize().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            IconButton(onClick = onBack, modifier = Modifier.size(40.dp).align(Alignment.Start)) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver", tint = MaterialTheme.colorScheme.onBackground) }
            Spacer(modifier = Modifier.height(40.dp))
            Text("Editar Perfil", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.onBackground)
            Spacer(modifier = Modifier.height(40.dp))
            
            Box(modifier = Modifier.size(120.dp), contentAlignment = Alignment.Center) {
                Surface(modifier = Modifier.fillMaxSize().clip(CircleShape), shape = CircleShape, color = MaterialTheme.colorScheme.surfaceVariant) {
                    val img = selectedImageUri ?: photoUrl?.let { Uri.parse(it) }
                    if (img != null) AsyncImage(model = img, contentDescription = "Foto", modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                    else Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Icon(Icons.Default.Person, null, modifier = Modifier.size(60.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)) }
                }
                FloatingActionButton(onClick = { imagePicker.launch("image/*") }, modifier = Modifier.align(Alignment.BottomEnd).offset(x = 4.dp, y = 4.dp).size(36.dp), containerColor = ToDusColors.Red, shape = CircleShape) { Icon(Icons.Default.CameraAlt, "Cambiar foto", modifier = Modifier.size(18.dp), tint = ToDusColors.White) }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            OutlinedTextField(value = alias, onValueChange = { alias = it.take(50) }, modifier = Modifier.fillMaxWidth(), label = { Text("Alias") }, singleLine = true, leadingIcon = { Icon(Icons.Default.Person, null) }, shape = RoundedCornerShape(12.dp), colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MaterialTheme.colorScheme.primary, unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)))
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(value = bio, onValueChange = { bio = it.take(200) }, modifier = Modifier.fillMaxWidth(), label = { Text("Bio") }, minLines = 2, maxLines = 4, shape = RoundedCornerShape(12.dp), colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MaterialTheme.colorScheme.primary, unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)))
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(value = phone, onValueChange = {}, modifier = Modifier.fillMaxWidth(), label = { Text("Numero de telefono") }, readOnly = true, enabled = false, singleLine = true, shape = RoundedCornerShape(12.dp))
            
            if (errorMsg != null) { Spacer(modifier = Modifier.height(8.dp)); Text(errorMsg!!, color = ToDusColors.Error, style = MaterialTheme.typography.bodySmall) }
            if (successMsg != null) { Spacer(modifier = Modifier.height(8.dp)); Text(successMsg!!, color = ToDusColors.Green, style = MaterialTheme.typography.bodySmall) }
        }
        
        Column(modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth().padding(24.dp, 32.dp)) {
            Button(onClick = {
                CoroutineScope(Dispatchers.IO).launch {
                    isSaving = true; errorMsg = null; successMsg = null
                    try {
                        var finalPhotoUrl = photoUrl
                        selectedImageUri?.let { uri ->
                            val s3 = S3Uploader(app.xmppClient)
                            s3.uploadProfileImage(uri, context, phone, jwt).onSuccess { url -> finalPhotoUrl = url }
                        }
                        jwtManager.saveProfile(alias, finalPhotoUrl ?: "", jwtManager.getToDusId() ?: "")
                        jwtManager.saveDescription(bio)
                        withContext(Dispatchers.Main) { isSaving = false; successMsg = "Perfil actualizado"; delay(1000); onSaved() }
                    } catch (e: Exception) {
                        withContext(Dispatchers.Main) { isSaving = false; errorMsg = "Error: ${e.message}" }
                    }
                }
            }, modifier = Modifier.fillMaxWidth().height(52.dp), enabled = alias.isNotBlank() && !isSaving, shape = RoundedCornerShape(12.dp), colors = ButtonDefaults.buttonColors(containerColor = ToDusColors.Red)) {
                if (isSaving) CircularProgressIndicator(modifier = Modifier.size(24.dp), color = ToDusColors.White)
                else Text("Guardar", style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}
