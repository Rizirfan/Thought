package com.thoughts.app.domain.engine

import com.thoughts.app.domain.model.ElementType

object ScreenplayRules {
    // Standard industry 8.5" x 11" page coordinates in Points (72 pt = 1 inch)
    const val PAGE_WIDTH_PT = 612f
    const val PAGE_HEIGHT_PT = 792f
    const val MARGIN_TOP_PT = 72f      // 1.0 inch
    const val MARGIN_BOTTOM_PT = 72f   // 1.0 inch
    const val MARGIN_LEFT_PT = 108f    // 1.5 inch (Binding margin)
    const val MARGIN_RIGHT_PT = 72f    // 1.0 inch

    // Indents from Left Margin (108 pt base)
    const val INDENT_ACTION_PT = 0f                   // at 1.5"
    const val WIDTH_ACTION_PT = 432f                  // 6.0" wide
    const val INDENT_CHARACTER_PT = 158f              // at 3.7" from edge (266pt - 108pt)
    const val INDENT_PARENTHETICAL_PT = 115f          // at 3.1" from edge (223pt - 108pt)
    const val WIDTH_PARENTHETICAL_PT = 180f           // ~2.5" wide
    const val INDENT_DIALOGUE_PT = 72f                // at 2.5" from edge (180pt - 108pt)
    const val WIDTH_DIALOGUE_PT = 252f                // 3.5" wide
    const val INDENT_TRANSITION_PT = 324f             // right-aligned ~6.0" from edge

    // Standard Font Dimensions
    const val FONT_SIZE_PT = 12f
    const val LINE_HEIGHT_PT = 14.4f                  // ~6 lines per vertical inch
    const val CHARS_PER_INCH = 10                     // 10 pitch Courier
    const val LINES_PER_PAGE_AVERAGE = 54             // 54-55 lines per page

    /**
     * Context-aware next element on pressing ENTER
     */
    fun getNextElementOnEnter(current: ElementType, isContentBlank: Boolean = false): ElementType {
        return when (current) {
            ElementType.SCENE_HEADING -> ElementType.ACTION
            ElementType.ACTION -> ElementType.ACTION
            ElementType.CHARACTER -> ElementType.DIALOGUE
            ElementType.PARENTHETICAL -> ElementType.DIALOGUE
            ElementType.DIALOGUE -> {
                if (isContentBlank) ElementType.ACTION else ElementType.CHARACTER
            }
            ElementType.TRANSITION -> ElementType.SCENE_HEADING
            ElementType.SHOT -> ElementType.ACTION
        }
    }

    /**
     * Context-aware next element on pressing TAB
     */
    fun getNextElementOnTab(current: ElementType): ElementType {
        return when (current) {
            ElementType.SCENE_HEADING -> ElementType.ACTION
            ElementType.ACTION -> ElementType.CHARACTER
            ElementType.CHARACTER -> ElementType.PARENTHETICAL
            ElementType.PARENTHETICAL -> ElementType.DIALOGUE
            ElementType.DIALOGUE -> ElementType.PARENTHETICAL
            ElementType.TRANSITION -> ElementType.SCENE_HEADING
            ElementType.SHOT -> ElementType.ACTION
        }
    }

    /**
     * Context-aware previous element on pressing SHIFT+TAB
     */
    fun getPreviousElementOnTab(current: ElementType): ElementType {
        return when (current) {
            ElementType.SCENE_HEADING -> ElementType.TRANSITION
            ElementType.ACTION -> ElementType.SCENE_HEADING
            ElementType.CHARACTER -> ElementType.ACTION
            ElementType.PARENTHETICAL -> ElementType.CHARACTER
            ElementType.DIALOGUE -> ElementType.PARENTHETICAL
            ElementType.TRANSITION -> ElementType.SHOT
            ElementType.SHOT -> ElementType.ACTION
        }
    }

    /**
     * Casing rules for formatted text
     */
    fun formatCasing(text: String, type: ElementType): String {
        return when (type) {
            ElementType.SCENE_HEADING -> text.uppercase()
            ElementType.CHARACTER -> text.uppercase()
            ElementType.TRANSITION -> text.uppercase()
            ElementType.SHOT -> text.uppercase()
            ElementType.PARENTHETICAL -> {
                val trimmed = text.trim()
                if (trimmed.startsWith("(") && trimmed.endsWith(")")) {
                    trimmed
                } else if (trimmed.startsWith("(")) {
                    "$trimmed)"
                } else {
                    "($trimmed)"
                }
            }
            ElementType.ACTION -> text
            ElementType.DIALOGUE -> text
        }
    }

    /**
     * Calculates estimated standard page count based on elements count and lines
     */
    fun calculatePageEstimate(elementCount: Int, totalCharacters: Int): Float {
        // Standard formula: approx 150-180 words per page or ~54 lines
        val estimatedLines = (totalCharacters / 55) + (elementCount * 2)
        val pages = estimatedLines.toFloat() / LINES_PER_PAGE_AVERAGE.toFloat()
        return if (pages < 0.1f) 0.1f else (kotlin.math.round(pages * 10f) / 10f)
    }
}
