package com.rakibofc.cibltask.util

import android.content.Context
import android.content.Intent
import android.graphics.pdf.PdfDocument
import android.os.Environment
import android.os.Handler
import android.widget.Toast
import androidx.core.content.FileProvider
import com.rakibofc.cibltask.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class FileHandler {

    companion object {
        suspend fun savePdfFile(context: Context, document: PdfDocument, isShareable: Boolean) {
            // Check for external storage availability
            if (!isExternalStorageWritable()) {
                showToast(context, "External storage not available")
                return
            }

            // Define file details
            val fileName = generateRandomFileName()
            val downloadsDir =
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            val filePath = File(downloadsDir, fileName)

            try {

                // Save the document to a file asynchronously
                withContext(Dispatchers.IO) {
                    val fos = FileOutputStream(filePath)
                    document.writeTo(fos)
                    fos.close()

                    // Switch back to the main thread to show the toast message
                    Handler(context.mainLooper).post {
                        // Show success message to the user
                        showToast(
                            context,
                            context.getString(R.string.save_message, filePath.absolutePath)
                        )
                    }

                    // Share the saved file with other apps if shareable
                    if (isShareable)
                        shareFile(context, filePath)
                }

            } catch (e: IOException) {
                showToast(context, e.message.toString())
            }
        }

        private fun generateRandomFileName(): String {
            val timestamp = SimpleDateFormat("yyyy-MM-dd_HHmmss", Locale.ENGLISH).format(Date())
            return "TransactionReceipt_$timestamp.pdf"
        }

        private fun isExternalStorageWritable(): Boolean {
            val state = Environment.getExternalStorageState()
            return Environment.MEDIA_MOUNTED == state
        }

        private fun showToast(context: Context, message: String) {
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }

        private fun shareFile(context: Context, file: File) {
            val uri =
                FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(shareIntent, "Share PDF using..."))
        }
    }
}