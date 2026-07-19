package cu.todus.app.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.background
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cu.todus.app.ui.theme.ToDusColors
import kotlinx.coroutines.delay

@Composable
fun VoiceRecorderBar(
    isRecording: Boolean,
    duration: Int,
    amplitude: Float,
    onStartRecording: () -> Unit,
    onStopRecording: () -> Unit,
    onCancelRecording: () -> Unit,
    onSendVoice: () -> Unit
) {
    AnimatedVisibility(
        visible = isRecording,
        enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
        exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 8.dp
        ) {
            Column {
                // Barra de grabación
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Botón cancelar
                    IconButton(
                        onClick = onCancelRecording,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(Icons.Default.Delete, "Cancelar", tint = ToDusColors.Error, modifier = Modifier.size(24.dp))
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // Indicador de grabación y duración
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Punto rojo parpadeante
                        var showDot by remember { mutableStateOf(true) }
                        LaunchedEffect(Unit) {
                            while (true) {
                                showDot = !showDot
                                delay(500)
                            }
                        }
                        AnimatedVisibility(visible = showDot) {
                            Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(ToDusColors.Error))
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            formatVoiceDuration(duration),
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    // Waveform (barras de amplitud)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(2.dp),
                        modifier = Modifier.height(32.dp)
                    ) {
                        for (i in 0..10) {
                            val barHeight = (amplitude * 32 * (0.5f + (i % 3) * 0.3f)).coerceIn(4f, 32f)
                            Box(
                                modifier = Modifier
                                    .width(2.dp)
                                    .height(barHeight.dp)
                                    .clip(RoundedCornerShape(1.dp))
                                    .background(ToDusColors.Red)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    // Botón detener y enviar
                    IconButton(
                        onClick = {
                            onStopRecording()
                            onSendVoice()
                        },
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(Icons.Default.Send, "Enviar", tint = ToDusColors.Red, modifier = Modifier.size(24.dp))
                    }
                }

                // Texto "Desliza para cancelar"
                Text(
                    "Desliza el dedo para cancelar",
                    modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp),
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        }
    }
}

fun formatVoiceDuration(seconds: Int): String {
    val mins = seconds / 60
    val secs = seconds % 60
    return "${mins}:${secs.toString().padStart(2, '0')}"
}
