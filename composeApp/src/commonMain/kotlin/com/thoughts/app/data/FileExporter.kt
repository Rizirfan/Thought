package com.thoughts.app.data

import androidx.compose.runtime.Composable

interface FileExporter {
    fun save(fileName: String, mimeType: String, content: String)
    fun saveBytes(fileName: String, mimeType: String, bytes: ByteArray)
}

/** Platform file picker. Android uses the Storage Access Framework, desktop a native dialog. */
@Composable
expect fun rememberFileExporter(): FileExporter
