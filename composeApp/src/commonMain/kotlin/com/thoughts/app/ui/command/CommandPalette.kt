package com.thoughts.app.ui.command

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ViewSidebar
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.CenterFocusStrong
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.key.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.thoughts.app.data.repository.ScreenplayRepository
import com.thoughts.app.ui.AppState
import com.thoughts.app.ui.common.KeyboardHint
import com.thoughts.app.ui.navigation.ScreenplayNavDestination
import com.thoughts.app.ui.theme.*

data class CommandItem(
    val id: String,
    val title: String,
    val subtitle: String? = null,
    val category: String,
    val icon: ImageVector,
    val action: () -> Unit
)

@Composable
fun CommandPalette(
    appState: AppState,
    repository: ScreenplayRepository,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val project by repository.currentProject.collectAsState()
    val projects by repository.projects.collectAsState()
    val scenes = project.activeDraft?.scenes.orEmpty()
    val colors = StudioTheme.colors

    var query by remember { mutableStateOf("") }
    var selectedIndex by remember { mutableStateOf(0) }
    val focusRequester = remember { FocusRequester() }
    val listState = rememberLazyListState()

    val commands = remember(project.id, projects, scenes, appState.themeMode, appState.inspectorOpen) {
        buildCommands(appState, repository, project.title, projects.map { it.id to it.title }, scenes.map { it.sceneNumber to it.slugline })
    }
    val filtered = remember(commands, query) {
        if (query.isBlank()) commands
        else commands.filter {
            it.title.contains(query, true) ||
                it.subtitle?.contains(query, true) == true ||
                it.category.contains(query, true)
        }
    }

    LaunchedEffect(Unit) { focusRequester.requestFocus() }
    LaunchedEffect(query) {
        if (selectedIndex > filtered.lastIndex) selectedIndex = filtered.lastIndex.coerceAtLeast(0)
        if (filtered.isNotEmpty()) listState.animateScrollToItem(selectedIndex)
    }

    fun run(item: CommandItem) {
        item.action()
        onDismiss()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.36f))
            .clickable(onClick = onDismiss)
    ) {
        Surface(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 72.dp, start = Spacing.lg, end = Spacing.lg)
                .widthIn(max = 580.dp)
                .fillMaxWidth()
                .clickable(onClick = {}),
            shape = PanelShape,
            color = colors.elevated,
            border = BorderStroke(1.dp, colors.borderStrong),
            tonalElevation = 0.dp,
            shadowElevation = 24.dp
        ) {
            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = Spacing.md, vertical = Spacing.sm),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Search,
                        contentDescription = null,
                        tint = colors.inkTertiary,
                        modifier = Modifier.size(Sizes.icon)
                    )
                    Spacer(Modifier.width(Spacing.sm))
                    BasicTextField(
                        value = query,
                        onValueChange = {
                            query = it
                            selectedIndex = 0
                        },
                        modifier = Modifier
                            .weight(1f)
                            .focusRequester(focusRequester)
                            .onPreviewKeyEvent { event ->
                                if (event.type != KeyEventType.KeyDown) return@onPreviewKeyEvent false
                                when (event.key) {
                                    Key.Escape -> {
                                        onDismiss()
                                        true
                                    }
                                    Key.DirectionDown -> {
                                        if (filtered.isNotEmpty()) {
                                            selectedIndex = (selectedIndex + 1) % filtered.size
                                        }
                                        true
                                    }
                                    Key.DirectionUp -> {
                                        if (filtered.isNotEmpty()) {
                                            selectedIndex = (selectedIndex - 1 + filtered.size) % filtered.size
                                        }
                                        true
                                    }
                                    Key.Enter, Key.NumPadEnter -> {
                                        filtered.getOrNull(selectedIndex)?.let { run(it) }
                                        true
                                    }
                                    else -> false
                                }
                            },
                        singleLine = true,
                        textStyle = MaterialTheme.typography.bodyMedium.copy(color = colors.ink),
                        cursorBrush = androidx.compose.ui.graphics.SolidColor(colors.accent),
                        decorationBox = { inner ->
                            Box {
                                if (query.isEmpty()) {
                                    Text(
                                        "Search scenes, screens and actions…",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = colors.inkTertiary
                                    )
                                }
                                inner()
                            }
                        }
                    )
                    KeyboardHint("esc")
                }

                Box(Modifier.fillMaxWidth().height(1.dp).background(colors.border))

                if (filtered.isEmpty()) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(Spacing.xl),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "No matches",
                            style = MaterialTheme.typography.titleSmall,
                            color = colors.ink
                        )
                        Text(
                            text = "Try a scene number, screen name or action.",
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.inkTertiary
                        )
                    }
                } else {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxWidth().heightIn(max = 420.dp),
                        contentPadding = PaddingValues(vertical = Spacing.xs)
                    ) {
                        itemsIndexed(filtered, key = { _, item -> item.id }) { index, item ->
                            PaletteRow(
                                item = item,
                                selected = index == selectedIndex,
                                onClick = { run(item) }
                            )
                        }
                    }
                }

                Box(Modifier.fillMaxWidth().height(1.dp).background(colors.border))
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = Spacing.md, vertical = Spacing.sm),
                    horizontalArrangement = Arrangement.spacedBy(Spacing.md),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    HintPair("↑↓", "navigate")
                    HintPair("↵", "open")
                    HintPair("esc", "close")
                    Spacer(Modifier.weight(1f))
                    Text(
                        text = "${filtered.size} results",
                        style = StudioType.caption,
                        color = colors.inkTertiary
                    )
                }
            }
        }
    }
}

@Composable
private fun PaletteRow(
    item: CommandItem,
    selected: Boolean,
    onClick: () -> Unit
) {
    val colors = StudioTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.xs, vertical = 1.dp)
            .clip(InputShape)
            .background(if (selected) colors.accentSoft else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(horizontal = Spacing.sm, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .background(if (selected) colors.accent.copy(alpha = 0.16f) else colors.surfaceAlt),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = item.icon,
                contentDescription = null,
                tint = if (selected) colors.accent else colors.inkSecondary,
                modifier = Modifier.size(15.dp)
            )
        }
        Spacer(Modifier.width(Spacing.sm))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.title,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
                color = if (selected) colors.ink else colors.inkSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (item.subtitle != null) {
                Text(
                    text = item.subtitle,
                    style = StudioType.caption,
                    color = colors.inkTertiary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
        Text(
            text = item.category.uppercase(),
            style = StudioType.sectionLabel,
            color = if (selected) colors.accent else colors.inkTertiary
        )
    }
}

@Composable
private fun HintPair(keys: String, label: String) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        KeyboardHint(keys)
        Text(label, style = StudioType.caption, color = StudioTheme.colors.inkTertiary)
    }
}

private fun buildCommands(
    appState: AppState,
    repository: ScreenplayRepository,
    projectTitle: String,
    projects: List<Pair<String, String>>,
    scenes: List<Pair<Int, String>>
): List<CommandItem> {
    val items = mutableListOf<CommandItem>()

    ScreenplayNavDestination.entries.forEach { destination ->
        items += CommandItem(
            id = "nav-${destination.name}",
            title = when (destination) {
                ScreenplayNavDestination.EDITOR -> "Open writing room"
                else -> "Go to ${destination.label}"
            },
            subtitle = when (destination) {
                ScreenplayNavDestination.HOME -> "Project library"
                ScreenplayNavDestination.EDITOR -> projectTitle
                ScreenplayNavDestination.EXPORT -> "Copy or save the screenplay"
                ScreenplayNavDestination.SETTINGS -> "Theme, editor and preferences"
            },
            category = "Go",
            icon = destination.icon,
            action = { appState.navigate(destination) }
        )
    }

    items += CommandItem(
        id = "action-add-scene",
        title = "Add scene",
        subtitle = "Append a new scene to the active draft",
        category = "Action",
        icon = Icons.Outlined.Add,
        action = {
            repository.addScene()
            appState.navigate(ScreenplayNavDestination.EDITOR)
        }
    )
    items += CommandItem(
        id = "action-focus",
        title = "Enter focus mode",
        subtitle = "Hide the shell and write on the page",
        category = "Action",
        icon = Icons.Outlined.CenterFocusStrong,
        action = {
            appState.navigate(ScreenplayNavDestination.EDITOR)
            appState.isFocusMode = true
        }
    )
    items += CommandItem(
        id = "action-inspector",
        title = if (appState.inspectorOpen) "Hide inspector" else "Show inspector",
        subtitle = "Scene metadata panel on wide screens",
        category = "Action",
        icon = Icons.AutoMirrored.Outlined.ViewSidebar,
        action = { appState.inspectorOpen = !appState.inspectorOpen }
    )

    scenes.forEach { (number, slugline) ->
        items += CommandItem(
            id = "scene-$number",
            title = "Scene $number — $slugline",
            subtitle = "Jump to this scene",
            category = "Scene",
            icon = ScreenplayNavDestination.EDITOR.icon,
            action = {
                repository.selectScene(number - 1)
                appState.navigate(ScreenplayNavDestination.EDITOR)
            }
        )
    }

    projects.forEach { (id, title) ->
        items += CommandItem(
            id = "project-$id",
            title = title.ifBlank { "Untitled screenplay" },
            subtitle = "Open project",
            category = "Project",
            icon = ScreenplayNavDestination.HOME.icon,
            action = {
                repository.openProject(id)
                appState.navigate(ScreenplayNavDestination.EDITOR)
            }
        )
    }

    return items
}
