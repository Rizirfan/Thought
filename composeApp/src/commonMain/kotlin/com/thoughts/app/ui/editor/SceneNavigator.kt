package com.thoughts.app.ui.editor

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.ArrowDownward
import androidx.compose.material.icons.outlined.ArrowUpward
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.thoughts.app.domain.model.ScriptScene
import com.thoughts.app.ui.common.Hairline
import com.thoughts.app.ui.common.IconAction
import com.thoughts.app.ui.common.SectionLabel
import com.thoughts.app.ui.theme.*

@Composable
fun SceneNavigator(
    scenes: List<ScriptScene>,
    activeIndex: Int,
    onSelect: (Int) -> Unit,
    onAdd: () -> Unit,
    onDelete: (Int) -> Unit,
    onMove: (from: Int, to: Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = StudioTheme.colors
    var query by remember { mutableStateOf("") }

    val filtered = remember(scenes, query) {
        if (query.isBlank()) scenes
        else scenes.filter {
            it.slugline.contains(query, true) || it.synopsis.contains(query, true)
        }
    }

    Column(
        modifier = modifier
            .background(colors.chrome)
            .padding(Spacing.md)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                SectionLabel("Scenes")
                Spacer(Modifier.width(Spacing.sm))
                Text(
                    text = "${scenes.size}",
                    style = StudioType.metric,
                    color = colors.inkTertiary
                )
            }
            IconAction(
                icon = Icons.Outlined.Add,
                contentDescription = "Add scene",
                onClick = onAdd
            )
        }

        Spacer(Modifier.height(Spacing.sm))

        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Find a scene", style = MaterialTheme.typography.bodySmall) },
            singleLine = true,
            shape = InputShape,
            textStyle = MaterialTheme.typography.bodySmall,
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

        Spacer(Modifier.height(Spacing.md))

        val acts = filtered.map { it.actNumber }.distinct().sorted()
        LazyColumn(
            modifier = Modifier.weight(1f).fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            acts.forEach { act ->
                item(key = "act-$act") {
                    ActHeader(
                        act = act,
                        sceneCount = filtered.count { it.actNumber == act }
                    )
                }
                items(filtered.filter { it.actNumber == act }, key = { it.id }) { scene ->
                    val globalIndex = scenes.indexOf(scene)
                    SceneRow(
                        scene = scene,
                        isActive = globalIndex == activeIndex,
                        canMoveUp = globalIndex > 0,
                        canMoveDown = globalIndex < scenes.size - 1,
                        onClick = { onSelect(globalIndex) },
                        onMoveUp = { onMove(globalIndex, globalIndex - 1) },
                        onMoveDown = { onMove(globalIndex, globalIndex + 1) },
                        onDelete = { onDelete(globalIndex) }
                    )
                }
            }
            if (filtered.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(vertical = Spacing.xxl),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = if (scenes.isEmpty()) "No scenes yet" else "No scenes match",
                            style = MaterialTheme.typography.titleSmall,
                            color = colors.inkSecondary
                        )
                        Spacer(Modifier.height(Spacing.xs))
                        Text(
                            text = if (scenes.isEmpty()) {
                                "Add a scene to begin the draft."
                            } else {
                                "Try a location or a beat."
                            },
                            style = StudioType.caption,
                            color = colors.inkTertiary
                        )
                    }
                }
            }
        }

        Hairline()
        Spacer(Modifier.height(Spacing.sm))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${scenes.size} scenes",
                style = StudioType.caption,
                color = colors.inkTertiary
            )
            Text(
                text = "%.1f pages".format(scenes.sumOf { it.estimatedPageDuration.toDouble() }),
                style = StudioType.caption,
                color = colors.inkTertiary
            )
        }
    }
}

@Composable
private fun ActHeader(act: Int, sceneCount: Int) {
    val colors = StudioTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = Spacing.sm),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "ACT $act",
            style = StudioType.sectionLabel,
            color = colors.inkTertiary
        )
        Spacer(Modifier.width(Spacing.sm))
        Box(Modifier.weight(1f).height(1.dp).background(colors.border))
        Spacer(Modifier.width(Spacing.sm))
        Text(
            text = "$sceneCount",
            style = StudioType.caption,
            color = colors.inkTertiary
        )
    }
}

@Composable
private fun SceneRow(
    scene: ScriptScene,
    isActive: Boolean,
    canMoveUp: Boolean,
    canMoveDown: Boolean,
    onClick: () -> Unit,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
    onDelete: () -> Unit
) {
    val colors = StudioTheme.colors
    var menuOpen by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(InputShape)
            .background(if (isActive) colors.accentSoft else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(end = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            Modifier
                .width(2.dp)
                .height(38.dp)
                .background(if (isActive) colors.accent else Color.Transparent)
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = scene.sceneNumber.toString().padStart(2, '0'),
            style = StudioType.metric,
            color = if (isActive) colors.accent else colors.inkTertiary,
            modifier = Modifier.width(22.dp)
        )
        Spacer(Modifier.width(6.dp))
        Column(modifier = Modifier.weight(1f).padding(vertical = 7.dp)) {
            Text(
                text = scene.slugline,
                style = MaterialTheme.typography.labelMedium,
                fontFamily = FontFamily.Monospace,
                fontWeight = if (isActive) FontWeight.Bold else FontWeight.Medium,
                color = if (isActive) colors.ink else colors.inkSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (scene.synopsis.isNotBlank()) {
                Text(
                    text = scene.synopsis,
                    style = StudioType.caption,
                    color = colors.inkTertiary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
        Spacer(Modifier.width(6.dp))
        Box {
            IconAction(
                icon = Icons.Outlined.MoreVert,
                contentDescription = "Scene actions",
                onClick = { menuOpen = true }
            )
            DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                DropdownMenuItem(
                    text = { Text("Move up") },
                    enabled = canMoveUp,
                    leadingIcon = { Icon(Icons.Outlined.ArrowUpward, contentDescription = null) },
                    onClick = {
                        menuOpen = false
                        onMoveUp()
                    }
                )
                DropdownMenuItem(
                    text = { Text("Move down") },
                    enabled = canMoveDown,
                    leadingIcon = { Icon(Icons.Outlined.ArrowDownward, contentDescription = null) },
                    onClick = {
                        menuOpen = false
                        onMoveDown()
                    }
                )
                DropdownMenuItem(
                    text = { Text("Delete scene") },
                    leadingIcon = { Icon(Icons.Outlined.DeleteOutline, contentDescription = null) },
                    onClick = {
                        menuOpen = false
                        onDelete()
                    }
                )
            }
        }
    }
}
