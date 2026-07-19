package cu.todus.app.data.local

import android.content.Context
import android.media.MediaRecorder
import android.os.Build
import kotlinx.coroutines.*
import java.io.File
import java.io.FileOutputStream

class AudioRecorder(private val context: Context) {
    private var mediaRecorder: MediaRecorder? = null
    private var outputFile: File? = null
    private var startTime = 0L
    private var isRecording = false
    
    private val _amplitudeFlow = kotlinx.coroutines.flow.MutableStateFlow(0f)
    val amplitudeFlow = _amplitudeFlow.asStateFlow()
    
    private var amplitudeJob: Job? = null

    fun startRecording(): Result<Unit> {
        return try {
            val cacheDir = File(context.cacheDir, "voice_notes")
            if (!cacheDir.exists()) cacheDir.mkdirs()
            outputFile = File(cacheDir, "voice_${System.currentTimeMillis()}.opus")
            
            mediaRecorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                MediaRecorder(context)
            } else {
                @Suppress("DEPRECATION")
                MediaRecorder()
            }
            
            mediaRecorder?.apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.OGG)
                setAudioEncoder(MediaRecorder.AudioEncoder.OPUS)
                setAudioSamplingRate(16000)
                setAudioBitRate(32000)
                setOutputFile(FileOutputStream(outputFile).fd)
                prepare()
                start()
            }
            
            startTime = System.currentTimeMillis()
            isRecording = true
            
            // Medir amplitud cada 100ms
            amplitudeJob = CoroutineScope(Dispatchers.IO).launch {
                while (isActive && isRecording) {
                    try {
                        val amp = mediaRecorder?.maxAmplitude ?: 0
                        _amplitudeFlow.value = (amp / 32767f).coerceIn(0f, 1f)
                    } catch (_: Exception) {}
                    delay(100)
                }
            }
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun stopRecording(): Result<Pair<File, Int>> {
        return try {
            mediaRecorder?.apply {
                try { stop() } catch (_: Exception) {}
                try { release() } catch (_: Exception) {}
            }
            mediaRecorder = null
            isRecording = false
            amplitudeJob?.cancel()
            
            val duration = ((System.currentTimeMillis() - startTime) / 1000).toInt()
            val file = outputFile ?: return Result.failure(Exception("No se encontró el archivo"))
            
            if (file.length() == 0L) {
                return Result.failure(Exception("Archivo vacío"))
            }
            
            Result.success(Pair(file, duration))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun cancelRecording() {
        try {
            mediaRecorder?.apply {
                try { stop() } catch (_: Exception) {}
                try { release() } catch (_: Exception) {}
            }
            mediaRecorder = null
            isRecording = false
            amplitudeJob?.cancel()
            outputFile?.delete()
        } catch (_: Exception) {}
    }

    fun getCurrentAmplitude(): Float = (mediaRecorder?.maxAmplitude ?: 0) / 32767f
    fun getDuration(): Int = ((System.currentTimeMillis() - startTime) / 1000).toInt()
    fun isRecordingActive(): Boolean = isRecording
}
