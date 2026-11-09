package com.thoughts.app.ui.editor

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.thoughts.app.domain.model.CharacterProfile
import com.thoughts.app.domain.model.ElementType
import com.thoughts.app.ui.common.Hairline
import com.thoughts.app.ui.common.IconAction
import com.thoughts.app.ui.common.StudioChip
import com.thoughts.app.ui.theme.Spacing
import com.thoughts.app.ui.theme.StudioTheme
import com.thoughts.app.ui.theme.StudioType

@Composable
fun EditorToolbar(
    characters: List<CharacterProfile>,
    activeType: ElementType?,
    onSelectType: (ElementType) -> Unit,
    onInsertCharacter: (CharacterProfile) -> Unit,
    onAddScene: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = StudioTheme.colors

    Column(modifier = modifier.fillMaxWidth()) {
        Hairline()

        if (characters.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = Spacing.md, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "SPEAKER",
                    style = StudioType.sectionLabel,
                    color = colors.inkTertiary,
                    modifier = Modifier.padding(end = 2.dp)
                )
                characters.take(10).forEach { character ->
                    StudioChip(
                        label = "${character.name} · ${character.dialogueCount}",
                        selected = false,
                        onClick = { onInsertCharacter(character) }
                    )
                }
            }
            Hairline()
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(colors.chrome)
                .padding(horizontal = Spacing.md, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "ELEMENT",
                style = StudioType.sectionLabel,
                color = colors.inkTertiary,
                modifier = Modifier.padding(end = Spacing.sm)
            )

            Row(
                modifier = Modifier
                    .weight(1f)
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(5.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ElementType.entries.forEach { type ->
                    StudioChip(
                        label = type.displayName,
                        selected = activeType == type,
                        onClick = { onSelectType(type) }
                    )
                }
            }

            Spacer(Modifier.width(Spacing.xs))
            toolbarDivider(colors.border)
            Spacer(Modifier.width(Spacing.xs))

            IconAction(
                icon = Icons.Outlined.Add,
                contentDescription = "Add scene",
                onClick = onAddScene
            )
        }
    }
}

@Composable
private fun toolbarDivider(color: Color) {
    Box(
        Modifier
            .width(1.dp)
            .height(20.dp)
            .background(color)
    )
}
