package cu.todus.app.data.local

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.net.URL
import java.security.MessageDigest

object ImageCache {
    private const val MAX_CACHE_SIZE = 50 * 1024 * 1024 // 50MB
    private const val MAX_FILE_AGE = 7 * 24 * 60 * 60 * 1000L // 7 días

    private fun getCacheDir(context: Context): File {
        val dir = File(context.cacheDir, "images")
        if (!dir.exists()) dir.mkdirs()
        return dir
    }

    private fun getFileName(url: String): String {
        val hash = MessageDigest.getInstance("MD5").digest(url.toByteArray()).joinToString("") { "%02x".format(it) }
        return "$hash.jpg"
    }

    suspend fun getBitmap(context: Context, url: String): Bitmap? = withContext(Dispatchers.IO) {
        try {
            val file = File(getCacheDir(context), getFileName(url))
            
            // Si existe y no expiró, devolver del cache
            if (file.exists() && System.currentTimeMillis() - file.lastModified() < MAX_FILE_AGE) {
                return@withContext BitmapFactory.decodeFile(file.absolutePath)
            }
            
            // Descargar y guardar
            val connection = URL(url).openConnection()
            connection.connectTimeout = 10000
            connection.readTimeout = 10000
            val input = connection.getInputStream()
            val bitmap = BitmapFactory.decodeStream(input)
            input.close()
            
            if (bitmap != null) {
                FileOutputStream(file).use { out ->
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 85, out)
                }
            }
            
            bitmap
        } catch (e: Exception) {
            null
        }
    }

    suspend fun preload(context: Context, urls: List<String>) = withContext(Dispatchers.IO) {
        urls.forEach { url -> getBitmap(context, url) }
    }

    fun clear(context: Context) {
        val dir = getCacheDir(context)
        dir.listFiles()?.forEach { it.delete() }
    }

    fun getCacheSize(context: Context): Long {
        var size = 0L
        getCacheDir(context).listFiles()?.forEach { size += it.length() }
        return size
    }

    fun trimCache(context: Context) {
        val dir = getCacheDir(context)
        var totalSize = getCacheSize(context)
        
        if (totalSize > MAX_CACHE_SIZE) {
            dir.listFiles()?.sortedBy { it.lastModified() }?.forEach { file ->
                if (totalSize <= MAX_CACHE_SIZE) return
                totalSize -= file.length()
                file.delete()
            }
        }
    }
}
