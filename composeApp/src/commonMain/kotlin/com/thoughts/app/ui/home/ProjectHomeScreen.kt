package com.thoughts.app.ui.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.itemsIndexed as lazyListItemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material.icons.outlined.ViewAgenda
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.thoughts.app.data.repository.ScreenplayRepository
import com.thoughts.app.domain.model.ScriptProject
import com.thoughts.app.ui.AppState
import com.thoughts.app.ui.common.Hairline
import com.thoughts.app.ui.common.StudioTextField
import com.thoughts.app.ui.navigation.ScreenplayNavDestination
import com.thoughts.app.ui.theme.ButtonShape
import com.thoughts.app.ui.theme.CardShape
import com.thoughts.app.ui.theme.PanelShape
import com.thoughts.app.ui.theme.Sizes
import com.thoughts.app.ui.theme.Spacing
import com.thoughts.app.ui.theme.StudioTheme
import com.thoughts.app.ui.theme.StudioType
import org.jetbrains.compose.resources.painterResource
import thoughts.composeapp.generated.resources.Res
import thoughts.composeapp.generated.resources.ic_script_folder

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProjectHomeScreen(
    repository: ScreenplayRepository,
    appState: AppState,
    modifier: Modifier = Modifier
) {
    val projects by repository.projects.collectAsState()
    val active by repository.currentProject.collectAsState()
    val colors = StudioTheme.colors

    var isGridView by remember { mutableStateOf(true) }

    // Dialog state
    var showCreateDialog by remember { mutableStateOf(false) }
    var createTitle by remember { mutableStateOf("") }
    var createAuthor by remember { mutableStateOf("") }

    var renamingProject by remember { mutableStateOf<ScriptProject?>(null) }
    var renameText by remember { mutableStateOf("") }
    var renameAuthor by remember { mutableStateOf("") }

    var projectToDelete by remember { mutableStateOf<ScriptProject?>(null) }

    // Long press action sheet state
    var actionSheetProject by remember { mutableStateOf<ScriptProject?>(null) }

    BoxWithConstraints(
        modifier = modifier.fillMaxSize().background(colors.canvas),
        contentAlignment = Alignment.TopCenter
    ) {
        val compact = maxWidth < 640.dp
        val pagePadding = if (compact) Spacing.screenSide else Spacing.xxl

        if (isGridView) {
            LazyVerticalGrid(
                columns = if (compact) GridCells.Fixed(2) else GridCells.Adaptive(minSize = 180.dp),
                modifier = Modifier
                    .widthIn(max = 960.dp)
                    .fillMaxWidth()
                    .fillMaxHeight(),
                contentPadding = PaddingValues(
                    start = pagePadding,
                    top = Spacing.section,
                    end = pagePadding,
                    bottom = if (compact) 96.dp else Spacing.xxl
                ),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Top Action Bar for Desktop / Tablet
                if (!compact) {
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = Spacing.xs, bottom = Spacing.xs),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Button(
                                onClick = {
                                    createTitle = ""
                                    showCreateDialog = true
                                },
                                shape = ButtonShape,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = colors.ink,
                                    contentColor = colors.canvas
                                ),
                                modifier = Modifier.height(38.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Add,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(Modifier.width(6.dp))
                                Text("New Screenplay", fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }

                if (projects.isEmpty()) {
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        EmptyScreenplaysPlaceholder(
                            onCreateClick = {
                                createTitle = ""
                                showCreateDialog = true
                            }
                        )
                    }
                } else {
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        HomeScreenHeader(
                            projectsCount = projects.size,
                            isGridView = isGridView,
                            onToggleView = { isGridView = it }
                        )
                    }

                    // Minimal Monochrome Script Folder items (Natural placement)
                    itemsIndexed(projects, key = { _, project -> project.id }) { _, project ->
                        NaturalScriptFolderGridItem(
                            project = project,
                            isActive = project.id == active.id,
                            onOpen = {
                                repository.openProject(project.id)
                                appState.navigate(ScreenplayNavDestination.EDITOR)
                            },
                            onLongPress = {
                                actionSheetProject = project
                            }
                        )
                    }
                }

                item(span = { GridItemSpan(maxLineSpan) }) {
                    Spacer(Modifier.height(Spacing.xxl))
                }
            }
        } else {
            // List View (Minimal monochrome file rows)
            LazyColumn(
                modifier = Modifier
                    .widthIn(max = 840.dp)
                    .fillMaxWidth()
                    .fillMaxHeight(),
                contentPadding = PaddingValues(
                    start = pagePadding,
                    top = Spacing.section,
                    end = pagePadding,
                    bottom = if (compact) 96.dp else Spacing.xxl
                ),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (!compact) {
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = Spacing.xs, bottom = Spacing.xs),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Button(
                                onClick = {
                                    createTitle = ""
                                    showCreateDialog = true
                                },
                                shape = ButtonShape,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = colors.ink,
                                    contentColor = colors.canvas
                                ),
                                modifier = Modifier.height(38.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Add,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(Modifier.width(6.dp))
                                Text("New Screenplay", fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }

                if (projects.isEmpty()) {
                    item {
                        EmptyScreenplaysPlaceholder(
                            onCreateClick = {
                                createTitle = ""
                                showCreateDialog = true
                            }
                        )
                    }
                } else {
                    item {
                        HomeScreenHeader(
                            projectsCount = projects.size,
                            isGridView = isGridView,
                            onToggleView = { isGridView = it }
                        )
                    }

                    lazyListItemsIndexed(projects, key = { _, project -> project.id }) { _, project ->
                        NaturalScriptFolderListItem(
                            project = project,
                            isActive = project.id == active.id,
                            onOpen = {
                                repository.openProject(project.id)
                                appState.navigate(ScreenplayNavDestination.EDITOR)
                            },
                            onLongPress = {
                                actionSheetProject = project
                            }
                        )
                    }
                }

                item {
                    Spacer(Modifier.height(Spacing.xxl))
                }
            }
        }

        if (compact) {
            // Minimal Monochrome Floating Action Button (+ New Script)
            Surface(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = Spacing.screenSide, bottom = Spacing.section)
                    .height(Sizes.primaryButton)
                    .clip(CircleShape)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        createTitle = ""
                        showCreateDialog = true
                    },
                shape = CircleShape,
                color = colors.ink,
                shadowElevation = 0.dp
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 22.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Add,
                        contentDescription = "New Screenplay",
                        tint = colors.canvas,
                        modifier = Modifier.size(Sizes.icon)
                    )
                    Text(
                        text = "New Script",
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = colors.canvas
                    )
                }
            }
        }
    }

    // Modal Action Sheet on Long Press: Minimal Monochrome (Zero Colors)
    actionSheetProject?.let { project ->
        ModalBottomSheet(
            onDismissRequest = { actionSheetProject = null },
            containerColor = colors.surface,
            scrimColor = colors.canvas.copy(alpha = 0.65f),
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            dragHandle = {
                Box(
                    modifier = Modifier
                        .padding(vertical = 12.dp)
                        .width(36.dp)
                        .height(4.dp)
                        .clip(CircleShape)
                        .background(colors.inkTertiary.copy(alpha = 0.35f))
                )
            }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Spacing.screenSide, vertical = 6.dp)
                    .navigationBarsPadding()
            ) {
                // Header with the script folder and title (Monochrome)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    modifier = Modifier.padding(bottom = 14.dp)
                ) {
                    Image(
                        painter = painterResource(Res.drawable.ic_script_folder),
                        contentDescription = null,
                        modifier = Modifier.size(52.dp),
                        contentScale = ContentScale.Fit
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = project.title,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 17.sp
                            ),
                            color = colors.ink,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (project.author.isNotBlank()) {
                            Spacer(Modifier.height(2.dp))
                            Text(
                                text = "by ${project.author}",
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 13.sp),
                                color = colors.inkSecondary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                Hairline()
                Spacer(Modifier.height(8.dp))

                // Action: Open
                ActionMenuItem(
                    icon = Icons.AutoMirrored.Outlined.ArrowForward,
                    text = "Open",
                    onClick = {
                        actionSheetProject = null
                        repository.openProject(project.id)
                        appState.navigate(ScreenplayNavDestination.EDITOR)
                    }
                )

                // Action: Rename
                ActionMenuItem(
                    icon = Icons.Outlined.Edit,
                    text = "Rename",
                    onClick = {
                        actionSheetProject = null
                        repository.openProject(project.id)
                        renamingProject = project
                        renameText = project.title
                        renameAuthor = project.author
                    }
                )

                // Action: Duplicate
                ActionMenuItem(
                    icon = Icons.Outlined.ContentCopy,
                    text = "Duplicate",
                    onClick = {
                        actionSheetProject = null
                        repository.duplicateProject(project.id)
                    }
                )

                // Action: Delete
                ActionMenuItem(
                    icon = Icons.Outlined.Delete,
                    text = "Delete",
                    onClick = {
                        actionSheetProject = null
                        projectToDelete = project
                    }
                )

                Spacer(Modifier.height(18.dp))
            }
        }
    }

    // Dialog: Delete Confirmation
    projectToDelete?.let { project ->
        AlertDialog(
            onDismissRequest = { projectToDelete = null },
            shape = PanelShape,
            containerColor = colors.surface,
            title = {
                Text(
                    text = "Delete?",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = colors.ink
                )
            },
            text = {
                Text(
                    text = "Delete \"${project.title}\"? This cannot be undone.",
                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = 15.sp),
                    color = colors.inkSecondary
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        repository.deleteProject(project.id)
                        projectToDelete = null
                    },
                    modifier = Modifier.height(Sizes.primaryButton),
                    shape = ButtonShape,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colors.ink,
                        contentColor = colors.canvas
                    )
                ) {
                    Text("Delete", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { projectToDelete = null }) {
                    Text("Cancel", color = colors.inkSecondary)
                }
            }
        )
    }

    // Dialog: Create New Screenplay (Monochrome)
    if (showCreateDialog) {
        AlertDialog(
            onDismissRequest = { showCreateDialog = false },
            shape = PanelShape,
            containerColor = colors.surface,
            title = {
                Text(
                    text = "New Screenplay",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = colors.ink
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(Spacing.md)) {
                    StudioTextField(
                        value = createTitle,
                        onValueChange = { createTitle = it },
                        label = "Screenplay Title",
                        placeholder = "e.g. The Midnight Express"
                    )
                    StudioTextField(
                        value = createAuthor,
                        onValueChange = { createAuthor = it },
                        label = "Written by (Author Name)",
                        placeholder = "e.g. Quentin Tarantino or your name"
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val name = createTitle.trim().ifBlank { "New Screenplay" }
                        val author = createAuthor.trim().ifBlank { "Writer" }
                        repository.createNamedProject(title = name, author = author)
                        createTitle = ""
                        createAuthor = ""
                        showCreateDialog = false
                        appState.navigate(ScreenplayNavDestination.EDITOR)
                    },
                    modifier = Modifier.height(Sizes.primaryButton),
                    shape = ButtonShape,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colors.ink,
                        contentColor = colors.canvas
                    )
                ) {
                    Text("Create & Open", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateDialog = false }) {
                    Text("Cancel", color = colors.inkSecondary)
                }
            }
        )
    }

    // Dialog: Edit Screenplay Info (Monochrome)
    renamingProject?.let { project ->
        AlertDialog(
            onDismissRequest = { renamingProject = null },
            shape = PanelShape,
            containerColor = colors.surface,
            title = {
                Text(
                    text = "Edit Screenplay Info",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = colors.ink
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(Spacing.md)) {
                    StudioTextField(
                        value = renameText,
                        onValueChange = { renameText = it },
                        label = "Screenplay Title"
                    )
                    StudioTextField(
                        value = renameAuthor,
                        onValueChange = { renameAuthor = it },
                        label = "Written by (Author)"
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val newName = renameText.trim().ifBlank { project.title }
                        val newAuthor = renameAuthor.trim().ifBlank { project.author }
                        repository.updateProjectDetails(newName, newAuthor, project.logline)
                        renamingProject = null
                    },
                    modifier = Modifier.height(Sizes.primaryButton),
                    shape = ButtonShape,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colors.ink,
                        contentColor = colors.canvas
                    )
                ) {
                    Text("Save", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { renamingProject = null }) {
                    Text("Cancel", color = colors.inkSecondary)
                }
            }
        )
    }
}

@Composable
private fun HomeScreenHeader(
    projectsCount: Int,
    isGridView: Boolean,
    onToggleView: (Boolean) -> Unit
) {
    val colors = StudioTheme.colors

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 4.dp, bottom = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "SCRIPTS",
                style = StudioType.sectionLabel.copy(
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.4.sp
                ),
                color = colors.ink
            )
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = colors.surfaceAlt,
                border = BorderStroke(1.dp, colors.border)
            ) {
                Text(
                    text = "$projectsCount",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = colors.inkSecondary,
                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                )
            }
        }

        // View Toggle: Grid vs List (Minimal monochrome capsule)
        Surface(
            shape = RoundedCornerShape(10.dp),
            color = colors.surfaceAlt.copy(alpha = 0.5f),
            border = BorderStroke(1.dp, colors.border.copy(alpha = 0.6f))
        ) {
            Row(
                modifier = Modifier.padding(3.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isGridView) colors.surfaceAlt else Color.Transparent)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { onToggleView(true) },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.GridView,
                        contentDescription = "Grid View",
                        tint = if (isGridView) colors.ink else colors.inkTertiary,
                        modifier = Modifier.size(17.dp)
                    )
                }

                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (!isGridView) colors.surfaceAlt else Color.Transparent)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { onToggleView(false) },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.ViewAgenda,
                        contentDescription = "List View",
                        tint = if (!isGridView) colors.ink else colors.inkTertiary,
                        modifier = Modifier.size(17.dp)
                    )
                }
            }
        }
    }
}

/**
 * Empty state featuring the script folder graphic with minimal monochrome styling.
 */
@Composable
private fun EmptyScreenplaysPlaceholder(
    onCreateClick: () -> Unit
) {
    val colors = StudioTheme.colors

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(CardShape)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { onCreateClick() }
            .padding(horizontal = Spacing.section, vertical = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Box(
            modifier = Modifier
                .size(130.dp)
                .clip(CircleShape)
                .background(colors.surfaceAlt.copy(alpha = 0.5f)),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(Res.drawable.ic_script_folder),
                contentDescription = "Script Folder",
                modifier = Modifier.size(110.dp),
                contentScale = ContentScale.Fit
            )
        }
        Text(
            text = "No screenplays yet",
            style = MaterialTheme.typography.titleLarge.copy(
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            ),
            color = colors.ink
        )
    }
}

/**
 * Minimal Monochrome Natural Script Folder Item (Grid mode)
 * Sits naturally on the canvas with NO card borders.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun NaturalScriptFolderGridItem(
    project: ScriptProject,
    isActive: Boolean,
    onOpen: () -> Unit,
    onLongPress: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = StudioTheme.colors
    val haptic = LocalHapticFeedback.current
    val scenes = project.activeDraft?.scenes.orEmpty()
    val pages = scenes.sumOf { it.estimatedPageDuration.toDouble() }.toFloat()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .combinedClickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onOpen,
                onLongClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onLongPress()
                }
            )
            .padding(horizontal = 6.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Natural 3D script folder visual (Fixed position, never shifts or moves down)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(116.dp),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(Res.drawable.ic_script_folder),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(2.dp),
                contentScale = ContentScale.Fit
            )

            // Minimal Monochrome Status Badge overlaid on top corner - folder never moves down
            if (isActive) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = colors.ink,
                    contentColor = colors.canvas,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(end = 4.dp, top = 2.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(4.dp)
                                .clip(CircleShape)
                                .background(colors.canvas)
                        )
                        Text(
                            text = "ACTIVE",
                            style = StudioType.sectionLabel.copy(
                                fontSize = 8.5.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.6.sp
                            ),
                            color = colors.canvas
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(8.dp))

        // Screenplay Title - High Contrast, Bold Monochrome
        Text(
            text = project.title,
            style = MaterialTheme.typography.titleMedium.copy(
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            ),
            color = colors.ink,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center
        )

        // Author (only if specified)
        if (project.author.isNotBlank()) {
            Spacer(Modifier.height(2.dp))
            Text(
                text = project.author,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Normal,
                    color = colors.inkSecondary
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
            )
        }

        Spacer(Modifier.height(6.dp))

        // Monochrome Metrics Chip
        Surface(
            shape = RoundedCornerShape(6.dp),
            color = colors.surfaceAlt,
            border = BorderStroke(1.dp, colors.border)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
            ) {
                Text(
                    text = "${scenes.size} sc",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        fontFamily = FontFamily.Monospace
                    ),
                    color = colors.inkSecondary
                )
                Text(
                    text = "·",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                    color = colors.inkTertiary
                )
                Text(
                    text = "%.1f pp".format(pages),
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        fontFamily = FontFamily.Monospace
                    ),
                    color = colors.inkSecondary
                )
            }
        }
    }
}

/**
 * Minimal Monochrome Natural Script Folder Item (List mode)
 * Sits naturally on the canvas with NO card borders.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun NaturalScriptFolderListItem(
    project: ScriptProject,
    isActive: Boolean,
    onOpen: () -> Unit,
    onLongPress: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = StudioTheme.colors
    val haptic = LocalHapticFeedback.current
    val scenes = project.activeDraft?.scenes.orEmpty()
    val pages = scenes.sumOf { it.estimatedPageDuration.toDouble() }.toFloat()

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .combinedClickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onOpen,
                onLongClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onLongPress()
                }
            )
            .padding(vertical = 10.dp, horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Natural folder graphic on the left
        Box(
            modifier = Modifier.size(width = 88.dp, height = 76.dp),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(Res.drawable.ic_script_folder),
                contentDescription = "Script File",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit
            )
        }

        // Script Details
        Column(modifier = Modifier.weight(1f)) {
            // Title + Active badge in the same row so vertical layout never shifts
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = project.title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = colors.ink,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false)
                )

                if (isActive) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = colors.ink,
                        contentColor = colors.canvas
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(4.dp)
                                    .clip(CircleShape)
                                    .background(colors.canvas)
                            )
                            Text(
                                text = "ACTIVE",
                                style = StudioType.sectionLabel.copy(
                                    fontSize = 8.5.sp,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = colors.canvas
                            )
                        }
                    }
                }
            }

            if (project.author.isNotBlank()) {
                Spacer(Modifier.height(2.dp))
                Text(
                    text = "by ${project.author}",
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = colors.inkSecondary
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(Modifier.height(4.dp))

            // Metrics row
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = "${scenes.size} scene${if (scenes.size == 1) "" else "s"}",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace
                    ),
                    color = colors.inkSecondary
                )
                Text(
                    text = "·",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 12.sp),
                    color = colors.inkTertiary
                )
                Text(
                    text = "%.1f page${if (pages == 1.0f) "" else "s"}".format(pages),
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        fontFamily = FontFamily.Monospace
                    ),
                    color = colors.inkSecondary
                )
            }
        }
    }
}

/**
 * Pure Minimal Monochrome Action Menu Item (Zero Color Tints).
 */
@Composable
private fun ActionMenuItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String,
    onClick: () -> Unit
) {
    val colors = StudioTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { onClick() }
            .padding(horizontal = 12.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(colors.surfaceAlt),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = colors.ink,
                modifier = Modifier.size(20.dp)
            )
        }
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge.copy(
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium
            ),
            color = colors.ink
        )
    }
}
