package com.najm.statussaver

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.MediaStore
import androidx.documentfile.provider.DocumentFile

object StatusHelper {

    private const val WHATSAPP_STATUS_PATH = "Android%2Fmedia%2Fcom.whatsapp%2FWhatsApp%2FMedia%2F.Statuses"
    private const val WHATSAPP_BUSINESS_PATH = "Android%2Fmedia%2Fcom.whatsapp.w4b%2FWhatsApp%20Business%2FMedia%2F.Statuses"

    fun getStatusFolderIntent(isBusiness: Boolean = false): Intent {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)
        val path = if (isBusiness) WHATSAPP_BUSINESS_PATH else WHATSAPP_STATUS_PATH
        val initialUri = Uri.parse("content://com.android.externalstorage.documents/tree/primary%3A$path")

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            intent.putExtra(DocumentsContract.EXTRA_INITIAL_URI, initialUri)
        }
        intent.addFlags(
            Intent.FLAG_GRANT_READ_URI_PERMISSION or
            Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION
        )
        return intent
    }

    fun getStatusFiles(context: Context, treeUri: Uri): List<DocumentFile> {
        val rootDir = DocumentFile.fromTreeUri(context, treeUri) ?: return emptyList()
        return rootDir.listFiles()
            .filter { file ->
                val name = file.name?.lowercase() ?: ""
                file.isFile && (name.endsWith(".jpg") || name.endsWith(".jpeg") || name.endsWith(".png") || name.endsWith(".mp4"))
            }
            .sortedByDescending { it.lastModified() }
    }

    fun saveStatusToGallery(context: Context, file: DocumentFile): Boolean {
        val fileName = file.name ?: "Najm_Status_${System.currentTimeMillis()}"
        val isVideo = fileName.lowercase().endsWith(".mp4")
        val mimeType = if (isVideo) "video/mp4" else "image/jpeg"
        val relativePath = if (isVideo) {
            "${Environment.DIRECTORY_MOVIES}/NajmStatusSaver"
        } else {
            "${Environment.DIRECTORY_PICTURES}/NajmStatusSaver"
        }

        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
            put(MediaStore.MediaColumns.MIME_TYPE, mimeType)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.MediaColumns.RELATIVE_PATH, relativePath)
                put(MediaStore.MediaColumns.IS_PENDING, 1)
            }
        }

        val collection = if (isVideo) {
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI
        } else {
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        }

        val resolver = context.contentResolver
        val destinationUri = resolver.insert(collection, contentValues) ?: return false

        return try {
            resolver.openInputStream(file.uri)?.use { input ->
                resolver.openOutputStream(destinationUri)?.use { output ->
                    input.copyTo(output)
                }
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                contentValues.clear()
                contentValues.put(MediaStore.MediaColumns.IS_PENDING, 0)
                resolver.update(destinationUri, contentValues, null, null)
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
