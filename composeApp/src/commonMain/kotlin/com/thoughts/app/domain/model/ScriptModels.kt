package com.thoughts.app.domain.model

import kotlinx.serialization.Serializable

@Serializable
enum class ElementType(val displayName: String, val shortcutNum: Int) {
    SCENE_HEADING("SCENE", 1),
    ACTION("ACTION", 2),
    CHARACTER("CHARACTER", 3),
    PARENTHETICAL("PAREN", 4),
    DIALOGUE("DIALOGUE", 5),
    TRANSITION("TRANSITION", 6),
    SHOT("SHOT", 7)
}

@Serializable
enum class HeadingType {
    INT, EXT, INT_EXT
}

@Serializable
data class ScriptElementBlock(
    val id: String,
    val type: ElementType,
    val text: String,
    val characterExtension: String? = null, // e.g. "V.O.", "O.S.", "CONT'D"
    val isDualDialogue: Boolean = false
)

@Serializable
data class ScriptScene(
    val id: String,
    val sceneNumber: Int,
    val headingType: HeadingType = HeadingType.INT,
    val location: String = "",
    val timeOfDay: String = "DAY",
    val synopsis: String = "",
    val actNumber: Int = 1,
    val blocks: List<ScriptElementBlock> = emptyList(),
    val estimatedPageDuration: Float = 1.0f
) {
    val slugline: String
        get() {
            val prefix = when (headingType) {
                HeadingType.INT -> "INT."
                HeadingType.EXT -> "EXT."
                HeadingType.INT_EXT -> "INT./EXT."
            }
            val loc = if (location.isNotBlank()) location.uppercase() else "LOCATION"
            val time = if (timeOfDay.isNotBlank()) timeOfDay.uppercase() else "DAY"
            return "$prefix $loc - $time"
        }
}

@Serializable
data class ScriptDraft(
    val id: String,
    val name: String = "First Draft",
    val versionNumber: Int = 1,
    val isLocked: Boolean = false,
    val scenes: List<ScriptScene> = emptyList(),
    val lastModifiedTimestamp: Long = 0L
)

@Serializable
data class CharacterProfile(
    val id: String,
    val name: String,
    val description: String = "",
    val dialogueCount: Int = 0
)

@Serializable
data class ScriptProject(
    val id: String,
    val title: String,
    val author: String,
    val logline: String = "",
    val drafts: List<ScriptDraft> = emptyList(),
    val activeDraftId: String = "",
    val characters: List<CharacterProfile> = emptyList(),
    val createdAt: Long = 0L,
    val updatedAt: Long = 0L
) {
    val activeDraft: ScriptDraft?
        get() = drafts.find { it.id == activeDraftId } ?: drafts.firstOrNull()
}
