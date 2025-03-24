package com.insecureshop

import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.os.Parcelable
import android.provider.OpenableColumns
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import java.io.File
import java.io.InputStream
import java.io.OutputStream
import androidx.core.content.FileProvider


class ChooserActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chooser)
        if (intent.extras != null) {
            var uri = intent.getParcelableExtra<Parcelable>("android.intent.extra.STREAM") as Uri
            uri = Uri.fromFile(File(uri.toString()))
            makeTempCopy(uri, getFilename(uri))
        }

    }
    private fun makeTempCopy(fileUri: Uri, original_filename: String?): Uri? {
        try {
            // Create file in app's external files directory instead of root external storage
            val directory = getExternalFilesDir("InsecureShop")
            if (directory == null) {
                Log.e("ChooserActivity", "Failed to get external files directory")
                return null
            }
            
            if (!directory.exists()) {
                val directoryCreated = directory.mkdirs()
                if (!directoryCreated) {
                    Log.e("ChooserActivity", "Failed to create directory")
                    return null
                }
            }
            
            // Sanitize filename to prevent path traversal
            val safeFilename = original_filename?.replace(File.separatorChar, '_') ?: "unknown_file"
            val fileTemp = File(directory, safeFilename)
            
            // Use content:// URI with FileProvider instead of file:// URI
            val contentUri = FileProvider.getUriForFile(
                this,
                "com.insecureshop.file_provider",
                fileTemp
            )
            
            // Copy the file content
            contentResolver.openInputStream(fileUri)?.use { inputStream ->
                contentResolver.openOutputStream(contentUri)?.use { outputStream ->
                    val buffer = ByteArray(8192)
                    var bytesRead: Int
                    while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                        outputStream.write(buffer, 0, bytesRead)
                    }
                    outputStream.flush()
                }
            }
            
            return contentUri
        } catch (e: Exception) {
            Log.e("ChooserActivity", "Error copying file: ${e.message}")
            return null
        }
    }

    fun getFilename(uri : Uri): String? {
//        val uri = intent.data
        var fileName: String? = null
        val context = applicationContext
        val scheme = uri!!.scheme
        if (scheme == "file") {
            fileName = uri.lastPathSegment
        } else if (scheme == "content") {
            val proj =
                arrayOf(OpenableColumns.DISPLAY_NAME)
            val contentUri: Uri? = null
            val cursor: Cursor? = context.contentResolver.query(uri, proj, null, null, null)
            if (cursor != null && cursor.getCount() !== 0) {
                val columnIndex: Int = cursor.getColumnIndexOrThrow(OpenableColumns.DISPLAY_NAME)
                cursor.moveToFirst()
                fileName = cursor.getString(columnIndex)
            }
        }
        return fileName
    }

}
