package com.app.manfaattumbuhan.data.remote

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import android.webkit.MimeTypeMap
import com.app.manfaattumbuhan.data.remote.model.UploadResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.io.FileOutputStream
import java.util.UUID
import java.util.concurrent.TimeUnit

object FileUploadHelper {

    private const val SUPABASE_URL = "https://nbgjggkhubmpbxmjtpgt.supabase.co"
    private const val SUPABASE_ANON_KEY = "sb_publishable_F8OA4-H6GxP7g62aMxRe8A_lB8rd4Q2"
    private const val BUCKET = "uploads"

    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(120, TimeUnit.SECONDS)
        .writeTimeout(120, TimeUnit.SECONDS)
        .build()

    suspend fun uploadFile(
        context: Context,
        uri: Uri,
        type: String
    ): Result<UploadResponse> = withContext(Dispatchers.IO) {
        try {
            // Cek ukuran file sebelum proses
            val size = getFileSize(context, uri)
            val maxSize = if (type == "video") 30 * 1024 * 1024L else 5 * 1024 * 1024L
            if (size > maxSize) {
                val maxSizeMb = if (type == "video") "30 MB" else "5 MB"
                return@withContext Result.failure(Exception("Ukuran file terlalu besar. Maksimal $maxSizeMb."))
            }

            val file = uriToFile(context, uri)
            val mimeType = getMimeType(context, uri, file.name, type)

            val folder = if (type == "video") "video" else "foto"
            val ext = file.extension.ifBlank { if (type == "video") "mp4" else "jpg" }
            val filename = "${UUID.randomUUID()}.$ext"
            val filePath = "$folder/$filename"

            val uploadUrl = "$SUPABASE_URL/storage/v1/object/$BUCKET/$filePath"

            val requestBody = file.readBytes().toRequestBody(mimeType.toMediaTypeOrNull())

            val request = Request.Builder()
                .url(uploadUrl)
                .post(requestBody)
                .addHeader("apikey", SUPABASE_ANON_KEY)
                .addHeader("Authorization", "Bearer $SUPABASE_ANON_KEY")
                .addHeader("Content-Type", mimeType)
                .build()

            val response = client.newCall(request).execute()

            if (response.isSuccessful) {
                val publicUrl = "$SUPABASE_URL/storage/v1/object/public/$BUCKET/$filePath"
                Result.success(
                    UploadResponse(
                        url = publicUrl,
                        filename = filename,
                        type = type,
                        original_name = file.name,
                        size = file.length()
                    )
                )
            } else {
                val errorBody = response.body?.string() ?: "Upload gagal"
                Result.failure(Exception("Upload gagal (${response.code}): $errorBody"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun getMimeType(context: Context, uri: Uri, fileName: String, uploadType: String): String {
        var mimeType = context.contentResolver.getType(uri)

        if (mimeType == null || mimeType == "application/octet-stream") {
            val extension = MimeTypeMap.getFileExtensionFromUrl(fileName)
                ?: fileName.substringAfterLast('.', "")
            if (extension.isNotBlank()) {
                val fromExtension = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension.lowercase())
                if (fromExtension != null) {
                    mimeType = fromExtension
                }
            }
        }

        if (mimeType == null || mimeType == "application/octet-stream") {
            mimeType = when (uploadType) {
                "video" -> "video/mp4"
                "foto" -> "image/jpeg"
                else -> "application/octet-stream"
            }
        }

        return mimeType
    }

    private fun uriToFile(context: Context, uri: Uri): File {
        val fileName = getFileName(context, uri) ?: "upload_${System.currentTimeMillis()}"
        val tempFile = File(context.cacheDir, fileName)
        context.contentResolver.openInputStream(uri)?.use { input ->
            FileOutputStream(tempFile).use { output ->
                input.copyTo(output)
            }
        }
        return tempFile
    }

    private fun getFileName(context: Context, uri: Uri): String? {
        var name: String? = null
        context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (nameIndex >= 0 && cursor.moveToFirst()) {
                name = cursor.getString(nameIndex)
            }
        }
        return name
    }

    private fun getFileSize(context: Context, uri: Uri): Long {
        var size = 0L
        context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
            if (sizeIndex >= 0 && cursor.moveToFirst()) {
                size = cursor.getLong(sizeIndex)
            }
        }
        return size
    }
}
