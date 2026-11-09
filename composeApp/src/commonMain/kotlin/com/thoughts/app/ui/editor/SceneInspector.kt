package com.thoughts.app.ui.editor

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.thoughts.app.data.repository.ScreenplayRepository
import com.thoughts.app.domain.model.ElementType
import com.thoughts.app.domain.model.HeadingType
import com.thoughts.app.ui.common.SectionLabel
import com.thoughts.app.ui.common.StudioChip
import com.thoughts.app.ui.common.StudioTextField
import com.thoughts.app.ui.theme.Spacing
import com.thoughts.app.ui.theme.StudioTheme
import com.thoughts.app.ui.theme.StudioType
import com.thoughts.app.ui.theme.InputShape
import androidx.compose.foundation.shape.RoundedCornerShape

@Composable
fun SceneInspector(
    repository: ScreenplayRepository,
    sceneIndex: Int,
    modifier: Modifier = Modifier
) {
    val project by repository.currentProject.collectAsState()
    val scene = project.activeDraft?.scenes?.getOrNull(sceneIndex)
    val colors = StudioTheme.colors

    if (scene == null) {
        Text(
            text = "No scene selected.",
            style = MaterialTheme.typography.bodySmall,
            color = colors.inkTertiary,
            modifier = modifier
        )
        return
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(Spacing.md)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "SCENE ${scene.sceneNumber}",
                style = StudioType.sectionLabel,
                color = colors.accent
            )
            Text(
                text = "%.1f pgs".format(scene.estimatedPageDuration),
                style = StudioType.metric,
                color = colors.inkSecondary
            )
        }

        Text(
            text = scene.slugline,
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
            color = colors.ink
        )

        SectionLabel("Slugline")
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            HeadingType.entries.forEach { type ->
                StudioChip(
                    label = when (type) {
                        HeadingType.INT -> "INT"
                        HeadingType.EXT -> "EXT"
                        HeadingType.INT_EXT -> "INT/EXT"
                    },
                    selected = scene.headingType == type,
                    onClick = {
                        repository.updateSceneMeta(sceneIndex) { it.copy(headingType = type) }
                    }
                )
            }
        }

        StudioTextField(
            value = scene.location,
            onValueChange = { value ->
                repository.updateSceneMeta(sceneIndex) { it.copy(location = value) }
            },
            label = "Location",
            placeholder = "APARTMENT"
        )

        Row(
            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            listOf("DAY", "NIGHT", "DAWN", "DUSK", "CONTINUOUS").forEach { time ->
                StudioChip(
                    label = time,
                    selected = scene.timeOfDay.equals(time, true),
                    onClick = {
                        repository.updateSceneMeta(sceneIndex) { it.copy(timeOfDay = time) }
                    }
                )
            }
        }

        SectionLabel("Synopsis")
        OutlinedTextField(
            value = scene.synopsis,
            onValueChange = { value ->
                repository.updateSceneMeta(sceneIndex) { it.copy(synopsis = value) }
            },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3,
            textStyle = MaterialTheme.typography.bodySmall,
            shape = InputShape,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = colors.accent,
                unfocusedBorderColor = colors.border,
                focusedContainerColor = colors.surface,
                unfocusedContainerColor = colors.surface,
                focusedTextColor = colors.ink,
                unfocusedTextColor = colors.ink,
                cursorColor = colors.accent
            )
        )

        SectionLabel("Structure")
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            (1..5).forEach { act ->
                StudioChip(
                    label = "Act $act",
                    selected = scene.actNumber == act,
                    onClick = {
                        repository.updateSceneMeta(sceneIndex) { it.copy(actNumber = act) }
                    }
                )
            }
        }

        val cast = scene.blocks
            .filter { it.type == ElementType.CHARACTER }
            .map { it.text }
            .filter { it.isNotBlank() }
            .distinct()
        if (cast.isNotEmpty()) {
            SectionLabel("Cast in scene")
            Row(
                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                cast.forEach { name ->
                    StudioChip(label = name, selected = false, onClick = {})
                }
            }
        }

        Spacer(Modifier.height(Spacing.sm))
        OutlinedButton(
            onClick = { repository.deleteScene(sceneIndex) },
            modifier = Modifier.fillMaxWidth(),
            shape = InputShape,
            colors = ButtonDefaults.outlinedButtonColors(contentColor = colors.danger)
        ) {
            Icon(Icons.Outlined.DeleteOutline, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(6.dp))
            Text("Delete scene")
        }

        Text(
            text = "Tip: Tab changes the current element type. Enter creates the next beat.",
            style = StudioType.caption,
            color = colors.inkTertiary
        )
    }
}
