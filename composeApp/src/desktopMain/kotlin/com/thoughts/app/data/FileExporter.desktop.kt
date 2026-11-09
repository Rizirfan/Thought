package com.thoughts.app.data

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import java.awt.FileDialog
import java.awt.Frame
import java.io.File

@Composable
actual fun rememberFileExporter(): FileExporter = remember {
    object : FileExporter {
        override fun save(fileName: String, mimeType: String, content: String) {
            saveBytes(fileName, mimeType, content.toByteArray(Charsets.UTF_8))
        }

        override fun saveBytes(fileName: String, mimeType: String, bytes: ByteArray) {
            val dialog = FileDialog(null as Frame?, "Save screenplay", FileDialog.SAVE)
            dialog.file = fileName
            dialog.isVisible = true
            val chosen = dialog.file
            if (chosen != null) {
                runCatching {
                    File(dialog.directory ?: ".", chosen).writeBytes(bytes)
                }
            }
            dialog.dispose()
        }
    }
}
