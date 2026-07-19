package cu.todus.app.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import cu.todus.app.ui.theme.ToDusColors
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun MessageBubble(
    text: String,
    time: Long,
    isMine: Boolean,
    state: String = "sent",
    mediaUrl: String? = null,
    mediaType: String = "text",
    mediaSize: Long = 0,
    mediaDuration: Int = 0,
    isUploading: Boolean = false,
    uploadProgress: Float = 0f,
    onCancelUpload: (() -> Unit)? = null,
    onImageClick: (() -> Unit)? = null
) {
    val bubbleColor = if (isMine) ToDusColors.White else ToDusColors.Red
    val textColor = if (isMine) Color.Black else Color.White
    val accentColor = if (isMine) ToDusColors.Red else Color.White
    val borderColor = if (isMine) MaterialTheme.colorScheme.outline.copy(alpha = 0.15f) else Color.Transparent
    val alignment = if (isMine) Alignment.End else Alignment.Start
    val shape = if (isMine) RoundedCornerShape(12.dp, 4.dp, 12.dp, 12.dp) else RoundedCornerShape(4.dp, 12.dp, 12.dp, 12.dp)

    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 2.dp),
        horizontalAlignment = alignment
    ) {
        Box(
            modifier = Modifier
                .widthIn(max = 280.dp)
                .clip(shape)
                .background(bubbleColor)
                .then(if (isMine) Modifier.border(1.dp, borderColor, shape) else Modifier)
                .padding(if (mediaType == "text" || mediaType == "sticker") 4.dp else 8.dp)
        ) {
            Column {
                when {
                    // ⭐ IMAGEN: progreso circular + X + tamaño
                    mediaType == "image" -> {
                        Box(modifier = Modifier.fillMaxWidth().heightIn(min = 100.dp, max = 250.dp).clip(RoundedCornerShape(8.dp))) {
                            if (isUploading) {
                                Box(modifier = Modifier.fillMaxSize().background(Color.LightGray.copy(alpha = 0.3f)), contentAlignment = Alignment.Center) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Box(contentAlignment = Alignment.Center) {
                                            CircularProgressIndicator(progress = uploadProgress, modifier = Modifier.size(48.dp), color = accentColor, strokeWidth = 3.dp, trackColor = Color.White.copy(alpha = 0.5f))
                                            IconButton(onClick = { onCancelUpload?.invoke() }, modifier = Modifier.size(24.dp)) {
                                                Icon(Icons.Default.Close, "Cancelar", tint = Color.White, modifier = Modifier.size(16.dp))
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(formatSize(mediaSize), fontSize = 12.sp, color = Color.White, fontWeight = FontWeight.Medium)
                                    }
                                }
                            } else if (mediaUrl != null) {
                                SubcomposeAsyncImage(
                                    model = ImageRequest.Builder(LocalContext.current).data(mediaUrl).crossfade(300).build(),
                                    contentDescription = "Imagen",
                                    modifier = Modifier.fillMaxSize().then(if (onImageClick != null) Modifier.clickable { onImageClick() } else Modifier),
                                    contentScale = ContentScale.Crop,
                                    loading = { Box(modifier = Modifier.fillMaxSize().background(Color.LightGray.copy(alpha = 0.3f)), contentAlignment = Alignment.Center) { CircularProgressIndicator(modifier = Modifier.size(24.dp), color = accentColor, strokeWidth = 2.dp) } },
                                    error = { Box(modifier = Modifier.fillMaxSize().background(Color.LightGray.copy(alpha = 0.3f)), contentAlignment = Alignment.Center) { Icon(Icons.Default.BrokenImage, "Error", tint = Color.Gray, modifier = Modifier.size(32.dp)) } }
                                )
                            }
                        }
                    }

                    // ⭐ VIDEO: progreso circular + X + tamaño en subida, duración en esquina al cargar
                    mediaType == "video" -> {
                        Box(modifier = Modifier.fillMaxWidth().height(200.dp).clip(RoundedCornerShape(8.dp))) {
                            if (isUploading) {
                                Box(modifier = Modifier.fillMaxSize().background(Color.DarkGray), contentAlignment = Alignment.Center) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Box(contentAlignment = Alignment.Center) {
                                            CircularProgressIndicator(progress = uploadProgress, modifier = Modifier.size(48.dp), color = accentColor, strokeWidth = 3.dp, trackColor = Color.White.copy(alpha = 0.5f))
                                            IconButton(onClick = { onCancelUpload?.invoke() }, modifier = Modifier.size(24.dp)) {
                                                Icon(Icons.Default.Close, "Cancelar", tint = Color.White, modifier = Modifier.size(16.dp))
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(formatSize(mediaSize), fontSize = 12.sp, color = Color.White)
                                    }
                                }
                            } else if (mediaUrl != null) {
                                SubcomposeAsyncImage(
                                    model = ImageRequest.Builder(LocalContext.current).data(mediaUrl).crossfade(true).build(),
                                    contentDescription = "Video", modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop,
                                    loading = { Box(modifier = Modifier.fillMaxSize().background(Color.DarkGray), contentAlignment = Alignment.Center) { CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White, strokeWidth = 2.dp) } }
                                )
                                Box(modifier = Modifier.size(48.dp).clip(CircleShape).background(Color.Black.copy(alpha = 0.6f)).align(Alignment.Center)) {
                                    Icon(Icons.Default.PlayArrow, "Reproducir", tint = Color.White, modifier = Modifier.fillMaxSize().padding(8.dp))
                                }
                                if (mediaDuration > 0) {
                                    Box(modifier = Modifier.align(Alignment.BottomEnd).padding(6.dp).clip(RoundedCornerShape(4.dp)).background(Color.Black.copy(alpha = 0.7f)).padding(horizontal = 6.dp, vertical = 2.dp)) {
                                        Text(formatDuration(mediaDuration), fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Medium)
                                    }
                                }
                            }
                        }
                    }

                    // ⭐ AUDIO: progreso circular + X en subida, play + nombre + duración + tamaño al cargar
                    mediaType == "audio" || mediaType == "voice" -> {
                        Row(modifier = Modifier.padding(4.dp), verticalAlignment = Alignment.CenterVertically) {
                            if (isUploading) {
                                Box(contentAlignment = Alignment.Center, modifier = Modifier.size(40.dp)) {
                                    CircularProgressIndicator(progress = uploadProgress, modifier = Modifier.size(40.dp), color = accentColor, strokeWidth = 3.dp, trackColor = accentColor.copy(alpha = 0.3f))
                                    Icon(Icons.Default.Close, "Cancelar", tint = accentColor, modifier = Modifier.size(16.dp))
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(text.ifEmpty { "Audio" }, color = textColor, fontSize = 14.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                    Text("Subiendo... ${formatSize(mediaSize)}", color = textColor.copy(alpha = 0.6f), fontSize = 11.sp)
                                }
                            } else {
                                IconButton(onClick = {}, modifier = Modifier.size(40.dp)) {
                                    Icon(Icons.Default.PlayArrow, "Reproducir", tint = accentColor, modifier = Modifier.size(28.dp))
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(text.ifEmpty { "Audio" }, color = textColor, fontSize = 14.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(formatDuration(mediaDuration), color = textColor.copy(alpha = 0.6f), fontSize = 11.sp)
                                        Text(" · ", color = textColor.copy(alpha = 0.4f), fontSize = 11.sp)
                                        Text(formatSize(mediaSize), color = textColor.copy(alpha = 0.6f), fontSize = 11.sp)
                                    }
                                }
                                Icon(Icons.Default.Mic, "Audio", tint = textColor.copy(alpha = 0.3f), modifier = Modifier.size(16.dp))
                            }
                        }
                    }

                    // ⭐ ARCHIVO: progreso circular + X en subida, icono + nombre + tamaño al cargar
                    mediaType == "file" -> {
                        Row(modifier = Modifier.padding(4.dp), verticalAlignment = Alignment.CenterVertically) {
                            if (isUploading) {
                                Box(contentAlignment = Alignment.Center, modifier = Modifier.size(40.dp)) {
                                    CircularProgressIndicator(progress = uploadProgress, modifier = Modifier.size(40.dp), color = accentColor, strokeWidth = 3.dp, trackColor = accentColor.copy(alpha = 0.3f))
                                    Icon(Icons.Default.Close, "Cancelar", tint = accentColor, modifier = Modifier.size(16.dp))
                                }
                            } else {
                                Icon(Icons.Default.InsertDriveFile, "Archivo", tint = accentColor, modifier = Modifier.size(40.dp))
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text.ifEmpty { "Archivo" }, color = textColor, fontSize = 14.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                Text(if (isUploading) "Subiendo..." else formatSize(mediaSize), color = textColor.copy(alpha = 0.6f), fontSize = 11.sp)
                            }
                        }
                    }

                    // ⭐ STICKER: progreso circular + tamaño en subida, imagen sin burbuja al cargar
                    mediaType == "sticker" -> {
                        Box(modifier = Modifier.size(120.dp)) {
                            if (isUploading) {
                                Box(modifier = Modifier.fillMaxSize().background(Color.LightGray.copy(alpha = 0.2f), RoundedCornerShape(8.dp)), contentAlignment = Alignment.Center) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Box(contentAlignment = Alignment.Center) {
                                            CircularProgressIndicator(progress = uploadProgress, modifier = Modifier.size(40.dp), color = accentColor, strokeWidth = 3.dp, trackColor = accentColor.copy(alpha = 0.3f))
                                            IconButton(onClick = { onCancelUpload?.invoke() }, modifier = Modifier.size(20.dp)) {
                                                Icon(Icons.Default.Close, "Cancelar", tint = accentColor, modifier = Modifier.size(12.dp))
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(formatSize(mediaSize), fontSize = 10.sp, color = textColor)
                                    }
                                }
                            } else if (mediaUrl != null) {
                                SubcomposeAsyncImage(
                                    model = ImageRequest.Builder(LocalContext.current).data(mediaUrl).crossfade(200).build(),
                                    contentDescription = "Sticker", modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Fit,
                                    loading = { Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator(modifier = Modifier.size(20.dp), color = accentColor, strokeWidth = 2.dp) } }
                                )
                            }
                        }
                    }

                    // ⭐ CONTACTO
                    mediaType == "contact" -> {
                        Row(modifier = Modifier.padding(4.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Person, "Contacto", tint = accentColor, modifier = Modifier.size(40.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text.ifEmpty { "Contacto" }, color = textColor, fontSize = 14.sp)
                        }
                    }

                    // ⭐ TEXTO normal
                    else -> {
                        if (text.isNotEmpty()) Text(text = text, color = textColor, fontSize = 15.sp, lineHeight = 20.sp)
                    }
                }

                // Hora y checks
                if ((text.isNotEmpty() || mediaType != "text") && mediaType != "sticker") {
                    Spacer(modifier = Modifier.height(if (mediaType == "text") 2.dp else 4.dp))
                    Row(
                        modifier = Modifier.align(Alignment.End),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Text(SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(time)), fontSize = 11.sp, color = if (isMine) Color.Gray else Color.White.copy(alpha = 0.7f))
                        if (isMine) {
                            when (state) {
                                "sending" -> Icon(Icons.Default.Schedule, "Enviando", modifier = Modifier.size(14.dp), tint = Color.Gray)
                                "sent" -> Icon(Icons.Default.Check, "Enviado", modifier = Modifier.size(14.dp), tint = Color.Gray)
                                "delivered" -> Icon(Icons.Default.DoneAll, "Entregado", modifier = Modifier.size(14.dp), tint = Color.Gray)
                                "read" -> Icon(Icons.Default.DoneAll, "Leído", modifier = Modifier.size(14.dp), tint = ToDusColors.Green)
                                else -> Icon(Icons.Default.Check, "Enviado", modifier = Modifier.size(14.dp), tint = Color.Gray)
                            }
                        }
                    }
                }
            }
        }
    }
}

fun formatSize(bytes: Long): String = when {
    bytes < 1024 -> "$bytes B"
    bytes < 1024 * 1024 -> "${bytes / 1024} KB"
    bytes < 1024 * 1024 * 1024 -> "${"%.1f".format(bytes / (1024.0 * 1024.0))} MB"
    else -> "${"%.1f".format(bytes / (1024.0 * 1024.0 * 1024.0))} GB"
}

fun formatDuration(seconds: Int): String {
    val mins = seconds / 60
    val secs = seconds % 60
    return "${mins}:${secs.toString().padStart(2, '0')}"
}
