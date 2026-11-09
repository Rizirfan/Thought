package com.thoughts.app.domain.engine

import com.thoughts.app.domain.model.ElementType
import com.thoughts.app.domain.model.HeadingType
import com.thoughts.app.domain.model.ScriptElementBlock
import com.thoughts.app.domain.model.ScriptScene

object FountainParser {

    /**
     * Parse raw script text into structured blocks
     */
    fun parseToBlocks(rawText: String): List<ScriptElementBlock> {
        val lines = rawText.lines()
        val blocks = mutableListOf<ScriptElementBlock>()
        var previousType: ElementType? = null

        var i = 0
        while (i < lines.size) {
            val line = lines[i].trimEnd()
            val trimmed = line.trim()

            if (trimmed.isEmpty()) {
                i++
                continue
            }

            // 1. Scene Heading check
            if (isSceneHeading(trimmed)) {
                blocks.add(
                    ScriptElementBlock(
                        id = generateId(),
                        type = ElementType.SCENE_HEADING,
                        text = trimmed.uppercase()
                    )
                )
                previousType = ElementType.SCENE_HEADING
                i++
                continue
            }

            // 2. Transition check (ends with TO: or starts with > or common transitions)
            if (isTransition(trimmed)) {
                val clean = trimmed.removePrefix(">").trim().uppercase()
                blocks.add(
                    ScriptElementBlock(
                        id = generateId(),
                        type = ElementType.TRANSITION,
                        text = clean
                    )
                )
                previousType = ElementType.TRANSITION
                i++
                continue
            }

            // 3. Parenthetical check (starts with '(' and ends with ')')
            if (trimmed.startsWith("(") && trimmed.endsWith(")")) {
                blocks.add(
                    ScriptElementBlock(
                        id = generateId(),
                        type = ElementType.PARENTHETICAL,
                        text = trimmed
                    )
                )
                previousType = ElementType.PARENTHETICAL
                i++
                continue
            }

            // 4. Character check (all caps, not a transition or heading)
            if (isCharacterCue(trimmed, previousType)) {
                val (name, ext) = extractCharacterAndExtension(trimmed)
                blocks.add(
                    ScriptElementBlock(
                        id = generateId(),
                        type = ElementType.CHARACTER,
                        text = name,
                        characterExtension = ext
                    )
                )
                previousType = ElementType.CHARACTER
                i++
                continue
            }

            // 5. Dialogue check (follows Character or Parenthetical)
            if (previousType == ElementType.CHARACTER || previousType == ElementType.PARENTHETICAL) {
                blocks.add(
                    ScriptElementBlock(
                        id = generateId(),
                        type = ElementType.DIALOGUE,
                        text = trimmed
                    )
                )
                previousType = ElementType.DIALOGUE
                i++
                continue
            }

            // 6. Default to Action
            blocks.add(
                ScriptElementBlock(
                    id = generateId(),
                    type = ElementType.ACTION,
                    text = trimmed
                )
            )
            previousType = ElementType.ACTION
            i++
        }

        return blocks
    }

    /**
     * Serializes structured blocks back to standard Fountain text
     */
    fun serializeBlocksToFountain(blocks: List<ScriptElementBlock>): String {
        val sb = StringBuilder()

        for (block in blocks) {
            when (block.type) {
                ElementType.SCENE_HEADING -> {
                    sb.append("\n\n").append(block.text.uppercase()).append("\n\n")
                }
                ElementType.ACTION -> {
                    sb.append(block.text).append("\n\n")
                }
                ElementType.CHARACTER -> {
                    val ext = if (!block.characterExtension.isNullOrBlank()) " (${block.characterExtension})" else ""
                    sb.append(block.text.uppercase()).append(ext).append("\n")
                }
                ElementType.PARENTHETICAL -> {
                    val text = if (block.text.startsWith("(") && block.text.endsWith(")")) block.text else "(${block.text.removePrefix("(").removeSuffix(")")})"
                    sb.append(text).append("\n")
                }
                ElementType.DIALOGUE -> {
                    sb.append(block.text).append("\n\n")
                }
                ElementType.TRANSITION -> {
                    sb.append("> ").append(block.text.uppercase()).append("\n\n")
                }
                ElementType.SHOT -> {
                    sb.append(block.text.uppercase()).append("\n\n")
                }
            }
        }

        return sb.toString().trim()
    }

    private fun isSceneHeading(line: String): Boolean {
        val upper = line.uppercase()
        return upper.startsWith("INT.") ||
                upper.startsWith("EXT.") ||
                upper.startsWith("INT./EXT.") ||
                upper.startsWith("I/E.") ||
                upper.startsWith(".INT") ||
                upper.startsWith(".EXT")
    }

    private fun isTransition(line: String): Boolean {
        val upper = line.uppercase()
        return upper.endsWith("TO:") ||
                upper == "FADE OUT." ||
                upper == "FADE IN:" ||
                upper == "DISSOLVE TO:" ||
                line.startsWith(">")
    }

    private fun isCharacterCue(line: String, previousType: ElementType?): Boolean {
        if (line.length > 35) return false
        val clean = line.replace(Regex("\\s*\\(.*?\\)"), "").trim()
        if (clean.isEmpty()) return false
        val isAllUpper = clean.all { it.isUpperCase() || it.isWhitespace() || it == '.' || it == '\'' }
        return isAllUpper && !isSceneHeading(line) && !isTransition(line)
    }

    private fun extractCharacterAndExtension(line: String): Pair<String, String?> {
        val extRegex = Regex("\\((.*?)\\)")
        val match = extRegex.find(line)
        val ext = match?.groupValues?.getOrNull(1)?.trim()
        val name = line.replace(extRegex, "").trim().uppercase()
        return Pair(name, ext)
    }

    private fun generateId(): String = "blk_${kotlin.random.Random.nextInt(100000, 999999)}"
}
