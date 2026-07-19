package cu.todus.app.ui.screens.chat

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import cu.todus.app.ToDusApp
import cu.todus.app.data.local.AudioRecorder
import cu.todus.app.data.local.ImageCompressor
import cu.todus.app.data.local.ToDusDatabase
import cu.todus.app.data.local.entity.MessageEntity
import cu.todus.app.data.remote.S3Uploader
import cu.todus.app.data.remote.ToDusProtocol
import cu.todus.app.ui.components.MessageBubble
import cu.todus.app.ui.components.VoiceRecorderBar
import cu.todus.app.ui.theme.ToDusColors
import kotlinx.coroutines.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(chatJid: String, chatName: String, onBack: () -> Unit, onContactProfile: (String) -> Unit = {}) {
    val context = LocalContext.current; val app = context.applicationContext as ToDusApp
    val db = remember { ToDusDatabase.getInstance(context) }
    val viewModel = remember { ChatViewModel(chatJid, app.xmppClient, db.messageDao(), db.chatDao()) }
    val messages by viewModel.messages.collectAsStateWithLifecycle()
    val messageText by viewModel.messageText.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()
    var showAttachMenu by remember { mutableStateOf(false) }
    var isUploading by remember { mutableStateOf(false) }
    var uploadProgress by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    
    // ⭐ Grabadora de voz
    val audioRecorder = remember { AudioRecorder(context) }
    var isRecording by remember { mutableStateOf(false) }
    var recordingDuration by remember { mutableIntStateOf(0) }
    var recordingAmplitude by remember { mutableFloatStateOf(0f) }
    var recordingJob by remember { mutableStateOf<Job?>(null) }

    // Selectores
    val imagePicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? -> uri?.let { uploadMedia(it, 4, "Imagen", context, app, chatJid, scope) { showAttachMenu = false; isUploading = true } { isUploading = false } { progress -> uploadProgress = progress } } }
    val videoPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? -> uri?.let { uploadMedia(it, 3, "Video", context, app, chatJid, scope) { showAttachMenu = false; isUploading = true } { isUploading = false } { progress -> uploadProgress = progress } } }
    val audioPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? -> uri?.let { uploadMedia(it, 2, "Audio", context, app, chatJid, scope) { showAttachMenu = false; isUploading = true } { isUploading = false } { progress -> uploadProgress = progress } } }
    val filePicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? -> uri?.let { uploadMedia(it, 0, "Archivo", context, app, chatJid, scope) { showAttachMenu = false; isUploading = true } { isUploading = false } { progress -> uploadProgress = progress } } }

    val isAtBottom by remember { derivedStateOf { val layoutInfo = listState.layoutInfo; val visibleItemsInfo = layoutInfo.visibleItemsInfo; if (layoutInfo.totalItemsCount == 0) true else visibleItemsInfo.lastOrNull()?.index ?: 0 >= layoutInfo.totalItemsCount - 2 } }
    LaunchedEffect(messages.size) { if (messages.isNotEmpty() && isAtBottom) listState.animateScrollToItem(messages.size - 1) }

    val groupedMessages = remember(messages) {
        val grouped = mutableListOf<Any>(); var lastDate = ""
        for (msg in messages) {
            val msgDate = SimpleDateFormat("dd 'de' MMMM 'de' yyyy", Locale("es")).format(Date(msg.timestamp))
            if (msgDate != lastDate) { grouped.add(msgDate); lastDate = msgDate }; grouped.add(msg)
        }; grouped
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(modifier = Modifier.clickable { onContactProfile(chatJid) }, verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(40.dp).clip(CircleShape).background(MaterialTheme.colorScheme.surfaceVariant), contentAlignment = Alignment.Center) { Text(chatName.first().uppercase(), color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold, fontSize = 18.sp) }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column { Text(chatName, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = Color.White, maxLines = 1, overflow = TextOverflow.Ellipsis) }
                    }
                },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver", tint = Color.White) } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = ToDusColors.Red, titleContentColor = Color.White)
            )
        },
        bottomBar = {
            Column {
                AnimatedVisibility(visible = isUploading) {
                    Surface(modifier = Modifier.fillMaxWidth(), color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)) {
                        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), color = ToDusColors.Red, strokeWidth = 2.dp); Spacer(modifier = Modifier.width(8.dp)); Text(uploadProgress, fontSize = 12.sp)
                        }
                    }
                }

                // ⭐ Barra de grabación de voz
                VoiceRecorderBar(
                    isRecording = isRecording,
                    duration = recordingDuration,
                    amplitude = recordingAmplitude,
                    onStartRecording = {
                        audioRecorder.startRecording()
                        isRecording = true
                        recordingJob = scope.launch {
                            while (isActive && isRecording) {
                                recordingDuration = audioRecorder.getDuration()
                                recordingAmplitude = audioRecorder.getCurrentAmplitude()
                                delay(100)
                            }
                        }
                    },
                    onStopRecording = {
                        isRecording = false
                        recordingJob?.cancel()
                    },
                    onCancelRecording = {
                        isRecording = false
                        recordingJob?.cancel()
                        audioRecorder.cancelRecording()
                    },
                    onSendVoice = {
                        val result = audioRecorder.stopRecording()
                        result.onSuccess { (file, duration) ->
                            isUploading = true
                            uploadProgress = "Subiendo nota de voz..."
                            scope.launch(Dispatchers.IO) {
                                val s3 = S3Uploader(app.xmppClient)
                                val uploadResult = s3.uploadCompressedFile(file, 1)
                                uploadResult.onSuccess { urls ->
                                    val msgXml = ToDusProtocol.buildVoiceMessage(chatJid, urls.getUrl, file.length(), duration)
                                    app.xmppClient.sendIq(msgXml)
                                    withContext(Dispatchers.Main) { isUploading = false; uploadProgress = "" }
                                    file.delete()
                                }.onFailure { e -> withContext(Dispatchers.Main) { isUploading = false; uploadProgress = "Error: ${e.message}" } }
                            }
                        }
                    }
                )

                Surface(modifier = Modifier.fillMaxWidth(), color = MaterialTheme.colorScheme.surface, shadowElevation = 8.dp) {
                    Row(
                        modifier = Modifier.fillMaxWidth().navigationBarsPadding().imePadding().padding(horizontal = 6.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = { showAttachMenu = !showAttachMenu }, modifier = Modifier.size(42.dp)) {
                            Icon(if (showAttachMenu) Icons.Default.Close else Icons.Default.AttachFile, "Adjuntar", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(22.dp))
                        }
                        Surface(modifier = Modifier.weight(1f), shape = RoundedCornerShape(24.dp), color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)) {
                            OutlinedTextField(value = messageText, onValueChange = { viewModel.onMessageTextChanged(it) }, modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 6.dp), placeholder = { Text("Mensaje", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f), fontSize = 15.sp) }, textStyle = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onSurface, fontSize = 15.sp), maxLines = 5, colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color.Transparent, unfocusedBorderColor = Color.Transparent, focusedContainerColor = Color.Transparent, unfocusedContainerColor = Color.Transparent))
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                        AnimatedContent(targetState = messageText.isNotBlank()) { hasText ->
                            if (hasText) {
                                IconButton(onClick = { viewModel.sendMessage() }, modifier = Modifier.size(42.dp)) { Icon(Icons.AutoMirrored.Filled.Send, "Enviar", tint = ToDusColors.Red, modifier = Modifier.size(22.dp)) }
                            } else {
                                // ⭐ Botón de grabar voz
                                IconButton(
                                    onClick = {
                                        if (!isRecording) {
                                            audioRecorder.startRecording()
                                            isRecording = true
                                            recordingJob = scope.launch {
                                                while (isActive && isRecording) {
                                                    recordingDuration = audioRecorder.getDuration()
                                                    recordingAmplitude = audioRecorder.getCurrentAmplitude()
                                                    delay(100)
                                                }
                                            }
                                        }
                                    },
                                    modifier = Modifier.size(42.dp)
                                ) {
                                    Icon(if (isRecording) Icons.Default.Stop else Icons.Default.Mic, if (isRecording) "Detener" else "Grabar", tint = if (isRecording) ToDusColors.Error else MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(22.dp))
                                }
                            }
                        }
                    }
                }

                AnimatedVisibility(visible = showAttachMenu, enter = slideInVertically(initialOffsetY = { it }) + fadeIn(), exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()) {
                    Surface(modifier = Modifier.fillMaxWidth(), color = MaterialTheme.colorScheme.surface, shadowElevation = 4.dp) {
                        Row(modifier = Modifier.fillMaxWidth().padding(12.dp), horizontalArrangement = Arrangement.SpaceEvenly) {
                            AttachOption(Icons.Default.Image, "Imagen") { imagePicker.launch("image/*") }
                            AttachOption(Icons.Default.Videocam, "Video") { videoPicker.launch("video/*") }
                            AttachOption(Icons.Default.Audiotrack, "Audio") { audioPicker.launch("audio/*") }
                            AttachOption(Icons.Default.InsertDriveFile, "Archivo") { filePicker.launch("*/*") }
                        }
                    }
                }
            }
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            if (messages.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(modifier = Modifier.size(80.dp).clip(CircleShape).background(MaterialTheme.colorScheme.surfaceVariant), contentAlignment = Alignment.Center) { Text(chatName.first().uppercase(), fontSize = 32.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)) }
                        Spacer(modifier = Modifier.height(16.dp)); Text(chatName, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                        Spacer(modifier = Modifier.height(4.dp)); Text("Inicia una conversacion", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
                        Text("Los mensajes estan cifrados de extremo a extremo", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f))
                    }
                }
            } else {
                LazyColumn(state = listState, modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(vertical = 8.dp)) {
                    items(groupedMessages) { item ->
                        when (item) {
                            is String -> { Box(modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp), contentAlignment = Alignment.Center) { Surface(shape = RoundedCornerShape(10.dp), color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)) { Text(item, modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp), fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Medium) } } }
                            is MessageEntity -> { MessageBubble(text = item.body, time = item.timestamp, isMine = item.senderPhone == "me", state = item.state, mediaUrl = item.mediaUrl, mediaType = item.type) }
                        }
                    }
                }
            }
        }
    }
}

private fun uploadMedia(uri: Uri, fileType: Int, label: String, context: android.content.Context, app: ToDusApp, chatJid: String, scope: CoroutineScope, onStart: () -> Unit, onEnd: () -> Unit, onProgress: (String) -> Unit) {
    onStart(); onProgress("Comprimiendo $label...")
    scope.launch(Dispatchers.IO) {
        val compressResult = ImageCompressor.compress(context, uri)
        compressResult.onSuccess { file ->
            onProgress("Subiendo $label...")
            val s3 = S3Uploader(app.xmppClient)
            s3.uploadCompressedFile(file, fileType).onSuccess { urls ->
                val msgXml = when (fileType) {
                    4 -> ToDusProtocol.buildImageMessage(chatJid, urls.getUrl, file.name, file.length())
                    3 -> ToDusProtocol.buildVideoMessage(chatJid, urls.getUrl, file.name, file.length(), 0)
                    2 -> ToDusProtocol.buildAudioMessage(chatJid, urls.getUrl, file.name, file.length(), 0)
                    else -> ToDusProtocol.buildFileMessage(chatJid, urls.getUrl, file.name, file.length())
                }
                app.xmppClient.sendIq(msgXml)
                withContext(Dispatchers.Main) { onEnd(); onProgress("") }
                file.delete()
            }.onFailure { e -> withContext(Dispatchers.Main) { onEnd(); onProgress("Error: ${e.message}") } }
        }.onFailure { e -> withContext(Dispatchers.Main) { onEnd(); onProgress("Error: ${e.message}") } }
    }
}

@Composable
fun AttachOption(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, onClick: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable(onClick = onClick).padding(8.dp)) {
        Box(modifier = Modifier.size(48.dp).clip(CircleShape).background(ToDusColors.Red.copy(alpha = 0.1f)), contentAlignment = Alignment.Center) { Icon(icon, label, tint = ToDusColors.Red, modifier = Modifier.size(22.dp)) }
        Spacer(modifier = Modifier.height(4.dp)); Text(label, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
