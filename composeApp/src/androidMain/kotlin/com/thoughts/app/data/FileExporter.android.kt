package com.thoughts.app.data

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext

@Composable
actual fun rememberFileExporter(): FileExporter {
    val context = LocalContext.current
    var pendingBytes by remember { mutableStateOf<ByteArray?>(null) }

    // Dedicated launcher for real PDF documents
    val pdfLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/pdf")
    ) { uri ->
        val bytes = pendingBytes
        if (uri != null && bytes != null) {
            runCatching {
                context.contentResolver.openOutputStream(uri)?.use { stream ->
                    stream.write(bytes)
                    stream.flush()
                }
            }
        }
        pendingBytes = null
    }

    // Dedicated launcher for plain text documents
    val textLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/plain")
    ) { uri ->
        val bytes = pendingBytes
        if (uri != null && bytes != null) {
            runCatching {
                context.contentResolver.openOutputStream(uri)?.use { stream ->
                    stream.write(bytes)
                    stream.flush()
                }
            }
        }
        pendingBytes = null
    }

    return remember(pdfLauncher, textLauncher) {
        object : FileExporter {
            override fun save(fileName: String, mimeType: String, content: String) {
                if (mimeType == "application/pdf" || fileName.endsWith(".pdf", ignoreCase = true)) {
                    // Generate authentic vector PDF bytes using Android's native PdfDocument
                    val pdfBytes = AndroidPdfGenerator.createPdf(content)
                    saveBytes(fileName, "application/pdf", pdfBytes)
                } else {
                    saveBytes(fileName, mimeType, content.toByteArray(Charsets.UTF_8))
                }
            }

            override fun saveBytes(fileName: String, mimeType: String, bytes: ByteArray) {
                pendingBytes = bytes
                if (mimeType == "application/pdf" || fileName.endsWith(".pdf", ignoreCase = true)) {
                    pdfLauncher.launch(fileName)
                } else {
                    textLauncher.launch(fileName)
                }
            }
        }
    }
}
