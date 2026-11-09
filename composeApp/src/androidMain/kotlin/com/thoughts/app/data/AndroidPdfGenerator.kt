package com.thoughts.app.data

import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import java.io.ByteArrayOutputStream

/**
 * Android Native PDF Generator for screenplays.
 * Uses Android's built-in [PdfDocument] to produce genuine, valid vector PDF documents.
 */
object AndroidPdfGenerator {

    private const val PAGE_WIDTH = 612   // US Letter width in points (8.5 inches * 72 pt/in)
    private const val PAGE_HEIGHT = 792  // US Letter height in points (11 inches * 72 pt/in)
    private const val MARGIN_LEFT = 72f  // 1.0 inch left margin
    private const val MARGIN_TOP = 72f   // 1.0 inch top margin
    private const val LINE_HEIGHT = 13f  // 13 points line pitch (gives ~54 lines per page)

    fun createPdf(content: String): ByteArray {
        val document = PdfDocument()

        val textPaint = Paint().apply {
            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL)
            textSize = 10f
            color = Color.BLACK
            isAntiAlias = true
        }

        val allLines = content.lines()
        val pages = mutableListOf<List<String>>()
        val currentPageLines = mutableListOf<String>()

        for (line in allLines) {
            // Check for explicit page separator line
            if (line.startsWith("----------------------------------------") ||
                line.contains("--- PAGE BREAK ---")
            ) {
                if (currentPageLines.isNotEmpty()) {
                    pages.add(currentPageLines.toList())
                    currentPageLines.clear()
                }
                continue
            }

            currentPageLines.add(line)

            // 54 lines per page is standard screenplay standard (1 page = 1 minute)
            if (currentPageLines.size >= 54) {
                pages.add(currentPageLines.toList())
                currentPageLines.clear()
            }
        }

        if (currentPageLines.isNotEmpty()) {
            pages.add(currentPageLines.toList())
        }

        if (pages.isEmpty()) {
            pages.add(listOf(""))
        }

        // Render each page
        for ((pageIndex, pageLines) in pages.withIndex()) {
            val pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageIndex + 1).create()
            val page = document.startPage(pageInfo)
            val canvas = page.canvas

            var y = MARGIN_TOP + LINE_HEIGHT
            for (line in pageLines) {
                if (line.isNotEmpty()) {
                    canvas.drawText(line, MARGIN_LEFT, y, textPaint)
                }
                y += LINE_HEIGHT
            }

            document.finishPage(page)
        }

        val outputStream = ByteArrayOutputStream()
        document.writeTo(outputStream)
        document.close()

        return outputStream.toByteArray()
    }
}
