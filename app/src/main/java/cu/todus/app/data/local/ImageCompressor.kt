package cu.todus.app.data.local

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import androidx.exifinterface.media.ExifInterface
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

object ImageCompressor {
    private const val MAX_WIDTH = 1280
    private const val MAX_HEIGHT = 1280
    private const val MAX_SIZE_KB = 300
    private const val COMPRESS_QUALITY = 85

    suspend fun compress(context: Context, uri: Uri): Result<File> = withContext(Dispatchers.IO) {
        try {
            // 1. Leer orientación EXIF
            val rotation = getRotation(context, uri)
            
            // 2. Decodificar bitmap con tamaño reducido
            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            context.contentResolver.openInputStream(uri)?.use { input ->
                BitmapFactory.decodeStream(input, null, options)
            }
            
            // 3. Calcular escala
            options.inSampleSize = calculateInSampleSize(options.outWidth, options.outHeight)
            options.inJustDecodeBounds = false
            
            // 4. Decodificar bitmap escalado
            val originalBitmap = context.contentResolver.openInputStream(uri)?.use { input ->
                BitmapFactory.decodeStream(input, null, options)
            } ?: return@withContext Result.failure(Exception("No se pudo decodificar la imagen"))
            
            // 5. Corregir rotación
            val correctedBitmap = if (rotation != 0f) {
                val matrix = Matrix().apply { postRotate(rotation) }
                Bitmap.createBitmap(originalBitmap, 0, 0, originalBitmap.width, originalBitmap.height, matrix, true)
            } else originalBitmap
            
            // 6. Redimensionar si es necesario
            val scaledBitmap = if (correctedBitmap.width > MAX_WIDTH || correctedBitmap.height > MAX_HEIGHT) {
                val scale = minOf(MAX_WIDTH.toFloat() / correctedBitmap.width, MAX_HEIGHT.toFloat() / correctedBitmap.height)
                val newWidth = (correctedBitmap.width * scale).toInt()
                val newHeight = (correctedBitmap.height * scale).toInt()
                Bitmap.createScaledBitmap(correctedBitmap, newWidth, newHeight, true)
            } else correctedBitmap
            
            // 7. Guardar en caché
            val cacheDir = File(context.cacheDir, "compressed_images")
            if (!cacheDir.exists()) cacheDir.mkdirs()
            val outputFile = File(cacheDir, "img_${System.currentTimeMillis()}.jpg")
            
            // 8. Comprimir iterativamente hasta alcanzar MAX_SIZE_KB
            var quality = COMPRESS_QUALITY
            do {
                FileOutputStream(outputFile).use { out ->
                    scaledBitmap.compress(Bitmap.CompressFormat.JPEG, quality, out)
                }
                quality -= 5
            } while (outputFile.length() / 1024 > MAX_SIZE_KB && quality > 10)
            
            // 9. Liberar memoria
            if (correctedBitmap != originalBitmap) correctedBitmap.recycle()
            if (scaledBitmap != correctedBitmap) scaledBitmap.recycle()
            originalBitmap.recycle()
            
            Result.success(outputFile)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun getRotation(context: Context, uri: Uri): Float {
        return try {
            context.contentResolver.openInputStream(uri)?.use { input ->
                val exif = ExifInterface(input)
                when (exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)) {
                    ExifInterface.ORIENTATION_ROTATE_90 -> 90f
                    ExifInterface.ORIENTATION_ROTATE_180 -> 180f
                    ExifInterface.ORIENTATION_ROTATE_270 -> 270f
                    else -> 0f
                }
            } ?: 0f
        } catch (e: Exception) {
            0f
        }
    }

    private fun calculateInSampleSize(width: Int, height: Int): Int {
        var inSampleSize = 1
        if (height > MAX_HEIGHT || width > MAX_WIDTH) {
            val halfHeight = height / 2
            val halfWidth = width / 2
            while ((halfHeight / inSampleSize) >= MAX_HEIGHT && (halfWidth / inSampleSize) >= MAX_WIDTH) {
                inSampleSize *= 2
            }
        }
        return inSampleSize
    }
}
