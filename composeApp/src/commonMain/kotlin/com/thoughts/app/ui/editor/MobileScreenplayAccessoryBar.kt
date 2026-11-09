package com.thoughts.app.ui.editor

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Subject
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.FormatQuote
import androidx.compose.material.icons.outlined.Movie
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Theaters
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.thoughts.app.domain.model.CharacterProfile
import com.thoughts.app.domain.model.ElementType
import com.thoughts.app.domain.model.ScriptElementBlock
import com.thoughts.app.ui.theme.StudioTheme
import com.thoughts.app.ui.theme.StudioType

private data class ElementMeta(
    val type: ElementType,
    val label: String,
    val icon: ImageVector
)

private val SCREENPLAY_ELEMENTS = listOf(
    ElementMeta(ElementType.SCENE_HEADING, "Scene", Icons.Outlined.Movie),
    ElementMeta(ElementType.ACTION, "Action", Icons.AutoMirrored.Outlined.Subject),
    ElementMeta(ElementType.CHARACTER, "Character", Icons.Outlined.Person),
    ElementMeta(ElementType.PARENTHETICAL, "Paren", Icons.Outlined.FormatQuote),
    ElementMeta(ElementType.DIALOGUE, "Dialogue", Icons.Outlined.ChatBubbleOutline),
    ElementMeta(ElementType.TRANSITION, "Transition", Icons.Outlined.Theaters)
)

/**
 * Mobile screenplay accessory toolbar — Pure Flat Design.
 *
 * Sits directly and flush above the soft keyboard with 0dp gap when typing,
 * and directly above the bottom navigation bar when keyboard is closed.
 *
 * Clean flat styling:
 * - Removed left/right movement arrow buttons.
 * - Removed all separation lines.
 * - Flat surfaces with zero borders.
 */
@Composable
fun MobileScreenplayAccessoryBar(
    activeBlock: ScriptElementBlock?,
    characters: List<CharacterProfile>,
    hasBottomDock: Boolean = true,
    onSelectType: (ElementType) -> Unit,
    onTab: () -> Unit = {},
    onEnter: () -> Unit = {},
    onInsertText: (String) -> Unit,
    onInsertCharacter: (String) -> Unit,
    onAddScene: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = StudioTheme.colors
    val density = LocalDensity.current
    val currentType = activeBlock?.type ?: ElementType.ACTION

    val imeBottom = WindowInsets.ime.getBottom(density)
    val bottomInset = with(density) { imeBottom.toDp() }

    val elementsScrollState = rememberScrollState()

    // Smoothly scroll active element into view when type changes
    LaunchedEffect(currentType) {
        val index = SCREENPLAY_ELEMENTS.indexOfFirst { it.type == currentType }
        if (index >= 0) {
            elementsScrollState.animateScrollTo((index * 80).coerceAtLeast(0))
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(colors.surface)
            .padding(bottom = bottomInset)
    ) {
        // 1. Contextual Quick Actions Strip (34dp) — Flat design, no separation line
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(34.dp)
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 8.dp, vertical = 3.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Context badge
            Text(
                text = currentType.name.replace("_", " "),
                style = StudioType.sectionLabel,
                color = colors.inkTertiary,
                modifier = Modifier.padding(end = 2.dp)
            )

            when (currentType) {
                ElementType.SCENE_HEADING -> {
                    AccessoryChip(label = "INT.", onClick = { onInsertText("INT. ") })
                    AccessoryChip(label = "EXT.", onClick = { onInsertText("EXT. ") })
                    AccessoryChip(label = "INT./EXT.", onClick = { onInsertText("INT./EXT. ") })
                    AccessoryChip(label = " - DAY", onClick = { onInsertText(" - DAY") })
                    AccessoryChip(label = " - NIGHT", onClick = { onInsertText(" - NIGHT") })
                    AccessoryChip(label = " - CONTINUOUS", onClick = { onInsertText(" - CONTINUOUS") })
                    AccessoryChip(label = " - LATER", onClick = { onInsertText(" - LATER") })
                    AccessoryChip(label = " - DAWN", onClick = { onInsertText(" - DAWN") })
                    AccessoryChip(label = " - DUSK", onClick = { onInsertText(" - DUSK") })
                }

                ElementType.CHARACTER -> {
                    characters.forEach { char ->
                        AccessoryChip(
                            label = char.name,
                            onClick = { onInsertCharacter(char.name) }
                        )
                    }
                    AccessoryChip(label = "(V.O.)", onClick = { onInsertText(" (V.O.)") })
                    AccessoryChip(label = "(O.S.)", onClick = { onInsertText(" (O.S.)") })
                    AccessoryChip(label = "(CONT'D)", onClick = { onInsertText(" (CONT'D)") })
                }

                ElementType.PARENTHETICAL -> {
                    AccessoryChip(label = "(beat)", onClick = { onInsertText("(beat)") })
                    AccessoryChip(label = "(pause)", onClick = { onInsertText("(pause)") })
                    AccessoryChip(label = "(whispers)", onClick = { onInsertText("(whispers)") })
                    AccessoryChip(label = "(smiling)", onClick = { onInsertText("(smiling)") })
                    AccessoryChip(label = "(quietly)", onClick = { onInsertText("(quietly)") })
                    AccessoryChip(label = "(to ...)", onClick = { onInsertText("(to )") })
                }

                ElementType.DIALOGUE -> {
                    AccessoryChip(label = "(beat)", onClick = { onInsertText("(beat)") })
                    AccessoryChip(label = "(pause)", onClick = { onInsertText("(pause)") })
                    AccessoryChip(label = "(whispers)", onClick = { onInsertText("(whispers)") })
                    characters.take(4).forEach { char ->
                        AccessoryChip(
                            label = char.name,
                            onClick = { onInsertCharacter(char.name) }
                        )
                    }
                }

                ElementType.TRANSITION -> {
                    AccessoryChip(label = "CUT TO:", onClick = { onInsertText("CUT TO:") })
                    AccessoryChip(label = "FADE OUT.", onClick = { onInsertText("FADE OUT.") })
                    AccessoryChip(label = "DISSOLVE TO:", onClick = { onInsertText("DISSOLVE TO:") })
                    AccessoryChip(label = "MATCH CUT TO:", onClick = { onInsertText("MATCH CUT TO:") })
                    AccessoryChip(label = "BACK TO SCENE.", onClick = { onInsertText("BACK TO SCENE.") })
                }

                else -> {
                    if (characters.isNotEmpty()) {
                        characters.take(6).forEach { char ->
                            AccessoryChip(
                                label = char.name,
                                onClick = { onInsertCharacter(char.name) }
                            )
                        }
                    }
                    AccessoryChip(
                        label = "+ Scene",
                        leadingIcon = Icons.Outlined.Add,
                        onClick = onAddScene
                    )
                    AccessoryChip(label = "CUT TO:", onClick = { onInsertText("CUT TO:") })
                }
            }
        }

        // 2. Primary Element Switcher Row (44dp) — Pure flat design, no movement arrows
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(44.dp)
                .horizontalScroll(elementsScrollState)
                .padding(horizontal = 8.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            SCREENPLAY_ELEMENTS.forEach { meta ->
                val selected = currentType == meta.type
                Surface(
                    onClick = { onSelectType(meta.type) },
                    shape = RoundedCornerShape(8.dp),
                    color = if (selected) colors.ink else colors.surfaceAlt,
                    border = null,
                    modifier = Modifier.height(36.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = meta.icon,
                            contentDescription = null,
                            tint = if (selected) colors.canvas else colors.ink,
                            modifier = Modifier.size(15.dp)
                        )
                        Text(
                            text = meta.label,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 12.sp,
                                letterSpacing = 0.2.sp
                            ),
                            color = if (selected) colors.canvas else colors.ink,
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AccessoryChip(
    label: String,
    onClick: () -> Unit,
    leadingIcon: ImageVector? = null
) {
    val colors = StudioTheme.colors
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        color = colors.surfaceAlt,
        border = null,
        modifier = Modifier.height(28.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            if (leadingIcon != null) {
                Icon(
                    imageVector = leadingIcon,
                    contentDescription = null,
                    tint = colors.inkSecondary,
                    modifier = Modifier.size(12.dp)
                )
            }
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                ),
                color = colors.ink,
                maxLines = 1
            )
        }
    }
}
