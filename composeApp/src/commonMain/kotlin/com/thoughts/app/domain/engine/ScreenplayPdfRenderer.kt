package com.thoughts.app.domain.engine

import com.thoughts.app.domain.model.ElementType
import com.thoughts.app.domain.model.ScriptProject

object ScreenplayPdfRenderer {

    /**
     * Generates a complete, industry-standard formatted plain text representation
     * honoring Courier 10-pitch margins for quick preview and export.
     */
    fun renderFormattedScript(project: ScriptProject, includeTitlePage: Boolean = true): String {
        val sb = StringBuilder()
        val draft = project.activeDraft ?: return ""

        if (includeTitlePage) {
            sb.append("\n\n\n\n")
            sb.append("          ${project.title.uppercase()}\n\n")
            sb.append("           written by\n\n")
            sb.append("          ${project.author.ifBlank { "Writer" }}\n\n\n\n")
            sb.append("Draft: ${draft.name}\n\n")
            sb.append("--------------------------------------------------------------------------------\n\n")
        }

        var lineCountOnPage = 0
        var currentPage = 1

        for (scene in draft.scenes) {
            // Scene Heading
            val heading = "${scene.sceneNumber}.  ${scene.slugline}"
            checkPageBreak(sb, 2, currentPage, lineCountOnPage).also { (p, l) -> currentPage = p; lineCountOnPage = l }
            sb.append(heading.uppercase()).append("\n\n")
            lineCountOnPage += 2

            for (block in scene.blocks) {
                when (block.type) {
                    ElementType.SCENE_HEADING -> {
                        // Handled above or interior slug
                        if (block.text != scene.slugline) {
                            checkPageBreak(sb, 2, currentPage, lineCountOnPage).also { (p, l) -> currentPage = p; lineCountOnPage = l }
                            sb.append(block.text.uppercase()).append("\n\n")
                            lineCountOnPage += 2
                        }
                    }
                    ElementType.ACTION -> {
                        val wrapped = wrapText(block.text, 60, indent = "")
                        checkPageBreak(sb, wrapped.size + 1, currentPage, lineCountOnPage).also { (p, l) -> currentPage = p; lineCountOnPage = l }
                        wrapped.forEach { sb.append(it).append("\n") }
                        sb.append("\n")
                        lineCountOnPage += wrapped.size + 1
                    }
                    ElementType.CHARACTER -> {
                        val ext = if (!block.characterExtension.isNullOrBlank()) " (${block.characterExtension})" else ""
                        val charCue = "${block.text.uppercase()}$ext"
                        checkPageBreak(sb, 3, currentPage, lineCountOnPage).also { (p, l) -> currentPage = p; lineCountOnPage = l }
                        sb.append(" ".repeat(22)).append(charCue).append("\n")
                        lineCountOnPage += 1
                    }
                    ElementType.PARENTHETICAL -> {
                        val paren = if (block.text.startsWith("(") && block.text.endsWith(")")) block.text else "(${block.text.removePrefix("(").removeSuffix(")")})"
                        checkPageBreak(sb, 1, currentPage, lineCountOnPage).also { (p, l) -> currentPage = p; lineCountOnPage = l }
                        sb.append(" ".repeat(16)).append(paren).append("\n")
                        lineCountOnPage += 1
                    }
                    ElementType.DIALOGUE -> {
                        val wrapped = wrapText(block.text, 35, indent = " ".repeat(10))
                        checkPageBreak(sb, wrapped.size + 1, currentPage, lineCountOnPage).also { (p, l) -> currentPage = p; lineCountOnPage = l }
                        wrapped.forEach { sb.append(it).append("\n") }
                        sb.append("\n")
                        lineCountOnPage += wrapped.size + 1
                    }
                    ElementType.TRANSITION -> {
                        checkPageBreak(sb, 2, currentPage, lineCountOnPage).also { (p, l) -> currentPage = p; lineCountOnPage = l }
                        sb.append(" ".repeat(45)).append(block.text.uppercase()).append("\n\n")
                        lineCountOnPage += 2
                    }
                    ElementType.SHOT -> {
                        checkPageBreak(sb, 2, currentPage, lineCountOnPage).also { (p, l) -> currentPage = p; lineCountOnPage = l }
                        sb.append(block.text.uppercase()).append("\n\n")
                        lineCountOnPage += 2
                    }
                }
            }
        }

        return sb.toString()
    }

    /**
     * Generates a real PDF document (PDF 1.4) with the formatted screenplay content.
     * Returns the PDF as a String (PDF is mostly ASCII text) that can be saved as .pdf.
     */
    fun renderToPdf(project: ScriptProject, includeTitlePage: Boolean = true): String {
        val draft = project.activeDraft ?: return ""

        // Collect all text lines for the document
        val textLines = mutableListOf<String>()

        if (includeTitlePage) {
            textLines.add("")
            textLines.add("")
            textLines.add("")
            textLines.add("")
            textLines.add(project.title.uppercase())
            textLines.add("")
            textLines.add("written by")
            textLines.add("")
            textLines.add(project.author)
            textLines.add("")
            textLines.add("Draft: ${draft.name}")
            textLines.add("")
            textLines.add("--------------------------------------------------------------------------------")
        }

        var lineCount = 0
        val maxLinesPerPage = 54

        for (scene in draft.scenes) {
            // Page break check
            if (lineCount + 2 > maxLinesPerPage) {
                textLines.add("") // page break
                lineCount = 0
            }
            textLines.add("${scene.sceneNumber}.  ${scene.slugline}".uppercase())
            lineCount += 2
            textLines.add("")
            lineCount++

            for (block in scene.blocks) {
                when (block.type) {
                    ElementType.SCENE_HEADING -> {
                        // Already handled above as scene heading
                    }
                    ElementType.ACTION -> {
                        val wrapped = wrapText(block.text, 60, indent = "")
                        for (w in wrapped) {
                            if (lineCount + 1 > maxLinesPerPage) {
                                textLines.add("")
                                lineCount = 0
                            }
                            textLines.add(w)
                            lineCount++
                        }
                        if (lineCount + 1 > maxLinesPerPage) {
                            textLines.add("")
                            lineCount = 0
                        }
                        textLines.add("")
                        lineCount++
                    }
                    ElementType.CHARACTER -> {
                        val ext = if (!block.characterExtension.isNullOrBlank()) " (${block.characterExtension})" else ""
                        val charCue = "${block.text.uppercase()}$ext"
                        if (lineCount + 1 > maxLinesPerPage) {
                            textLines.add("")
                            lineCount = 0
                        }
                        textLines.add(" ".repeat(22) + charCue)
                        lineCount++
                    }
                    ElementType.PARENTHETICAL -> {
                        val paren = if (block.text.startsWith("(") && block.text.endsWith(")")) block.text else "(${block.text.removePrefix("(").removeSuffix(")")})"
                        if (lineCount + 1 > maxLinesPerPage) {
                            textLines.add("")
                            lineCount = 0
                        }
                        textLines.add(" ".repeat(16) + paren)
                        lineCount++
                    }
                    ElementType.DIALOGUE -> {
                        val wrapped = wrapText(block.text, 35, indent = " ".repeat(10))
                        for (w in wrapped) {
                            if (lineCount + 1 > maxLinesPerPage) {
                                textLines.add("")
                                lineCount = 0
                            }
                            textLines.add(w)
                            lineCount++
                        }
                        if (lineCount + 1 > maxLinesPerPage) {
                            textLines.add("")
                            lineCount = 0
                        }
                        textLines.add("")
                        lineCount++
                    }
                    ElementType.TRANSITION -> {
                        if (lineCount + 2 > maxLinesPerPage) {
                            textLines.add("")
                            lineCount = 0
                        }
                        textLines.add(" ".repeat(45) + block.text.uppercase())
                        lineCount += 2
                        textLines.add("")
                        lineCount++
                    }
                    ElementType.SHOT -> {
                        if (lineCount + 2 > maxLinesPerPage) {
                            textLines.add("")
                            lineCount = 0
                        }
                        textLines.add(block.text.uppercase())
                        lineCount += 2
                        textLines.add("")
                        lineCount++
                    }
                }
            }
        }

        // Build PDF content stream
        val contentStream = buildString {
            append("BT\n")
            append("/F1 12 Tf\n")
            append("72 720 Td\n")
            var y = 720.0
            for (line in textLines) {
                val escaped = escapePdfString(line)
                append("($escaped) Tj\n")
                append("0 -13 Td\n")
                y -= 13
                if (y < 72) {
                    append("ET\n")
                    append("showpage\n")
                    append("BT\n")
                    append("/F1 12 Tf\n")
                    append("72 720 Td\n")
                    y = 720.0
                }
            }
            append("ET\n")
        }

        // Content stream (uncompressed - kept as ASCII text for compatibility)
        val contentBytes = contentStream.toByteArray(Charsets.US_ASCII)
        val contentLength = contentBytes.size

        // Build PDF objects
        val objOffsets = mutableListOf<Long>()
        var currentOffset = 0L

        // Object 1: Catalog
        val obj1 = "1 0 obj\n<< /Type /Catalog /Pages 2 0 R >>\nendobj\n"
        objOffsets.add(currentOffset)
        currentOffset += obj1.length.toLong()

        // Object 2: Pages
        val pageCount = textLines.count { it.isEmpty() } + 1
        val obj2 = "2 0 obj\n<< /Type /Pages /Kids [3 0 R] /Count $pageCount >>\nendobj\n"
        objOffsets.add(currentOffset)
        currentOffset += obj2.length.toLong()

        // Object 3: Page
        val obj3 = "3 0 obj\n<< /Type /Page /Parent 2 0 R /MediaBox [0 0 612 792] /Contents 4 0 R /Resources << /Font << /F1 5 0 R >> >> >>\nendobj\n"
        objOffsets.add(currentOffset)
        currentOffset += obj3.length.toLong()

        // Object 4: Content stream
        val obj4Header = "4 0 obj\n<< /Length $contentLength >>\nstream\n"
        val obj4Footer = "\nendstream\nendobj\n"
        objOffsets.add(currentOffset)
        currentOffset += obj4Header.length.toLong() + contentLength + obj4Footer.length.toLong()

        // Object 5: Font
        val obj5 = "5 0 obj\n<< /Type /Font /Subtype /Type1 /BaseFont /Courier >>\nendobj\n"
        objOffsets.add(currentOffset)
        currentOffset += obj5.length.toLong()

        val xrefOffset = currentOffset

        // Build xref
        val xref = buildString {
            append("xref\n")
            append("0 ${objOffsets.size + 1}\n")
            append("0000000000 65535 f \n")
            for (offset in objOffsets) {
                append("${offset.toString().padStart(10, '0')} 00000 n \n")
            }
        }

        val trailer = "trailer\n<< /Size ${objOffsets.size + 1} /Root 1 0 R >>\nstartxref\n$xrefOffset\n%%EOF\n"

        // Assemble full PDF (all ASCII, valid UTF-8)
        return buildString {
            append("%PDF-1.4\n")
            append(obj1)
            append(obj2)
            append(obj3)
            append(obj4Header)
            append(contentStream)
            append(obj4Footer)
            append(obj5)
            append(xref)
            append(trailer)
        }
    }

    private fun escapePdfString(text: String): String {
        return text
            .replace("\\", "\\\\")
            .replace("(", "\\(")
            .replace(")", "\\)")
            .replace("\n", "\\n")
            .replace("\r", "\\r")
            .replace("\t", "\\t")
    }

    private fun checkPageBreak(
        sb: StringBuilder,
        neededLines: Int,
        currentPage: Int,
        currentLineCount: Int
    ): Pair<Int, Int> {
        if (currentLineCount + neededLines > ScreenplayRules.LINES_PER_PAGE_AVERAGE) {
            val nextPage = currentPage + 1
            sb.append("\n\n")
            sb.append(" ".repeat(68)).append("$nextPage.").append("\n")
            sb.append("--------------------------------------------------------------------------------\n\n")
            return Pair(nextPage, 0)
        }
        return Pair(currentPage, currentLineCount)
    }

    private fun wrapText(text: String, maxWidth: Int, indent: String): List<String> {
        val words = text.split(Regex("\\s+"))
        val lines = mutableListOf<String>()
        val currentLine = StringBuilder(indent)

        for (word in words) {
            if (currentLine.length + word.length + 1 > maxWidth + indent.length) {
                if (currentLine.isNotBlank()) {
                    lines.add(currentLine.toString())
                    currentLine.clear()
                    currentLine.append(indent)
                }
            }
            if (currentLine.length > indent.length) {
                currentLine.append(" ")
            }
            currentLine.append(word)
        }
        if (currentLine.isNotBlank() && currentLine.toString() != indent) {
            lines.add(currentLine.toString())
        }
        return lines
    }

    /**
     * Exports complete project to Fountain format
     */
    fun exportToFountain(project: ScriptProject): String {
        val sb = StringBuilder()
        val draft = project.activeDraft ?: return ""

        sb.append("Title: ${project.title}\n")
        sb.append("Credit: written by\n")
        sb.append("Author: ${project.author}\n")
        sb.append("Draft date: ${draft.name}\n\n")
        sb.append("===\n\n")

        for (sc in draft.scenes) {
            sb.append(".${sc.slugline} #${sc.sceneNumber}#\n\n")
            sb.append(FountainParser.serializeBlocksToFountain(sc.blocks)).append("\n\n")
        }

        return sb.toString().trim()
    }
}
