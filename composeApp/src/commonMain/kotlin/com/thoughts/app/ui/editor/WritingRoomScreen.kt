package com.thoughts.app.ui.editor

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ViewList
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.EditNote
import androidx.compose.material.icons.outlined.Fullscreen
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.thoughts.app.data.repository.ScreenplayRepository
import com.thoughts.app.domain.engine.ScreenplayRules
import com.thoughts.app.domain.model.ElementType
import com.thoughts.app.domain.model.ScriptProject
import com.thoughts.app.ui.AppState
import com.thoughts.app.ui.common.Hairline
import com.thoughts.app.ui.common.IconAction
import com.thoughts.app.ui.common.SectionLabel
import com.thoughts.app.ui.common.StatusPill
import com.thoughts.app.ui.theme.*

@Composable
fun WritingRoomScreen(
    repository: ScreenplayRepository,
    appState: AppState,
    modifier: Modifier = Modifier
) {
    val project by repository.currentProject.collectAsState()
    val activeIndex by repository.activeSceneIndex.collectAsState()
    val draft = project.activeDraft
    val scenes = draft?.scenes.orEmpty()
    val scene = scenes.getOrNull(activeIndex)

    var activeBlockId by remember(scene?.id) { mutableStateOf<String?>(null) }
    var pendingFocusBlockId by remember(scene?.id) { mutableStateOf<String?>(null) }
    var showScenePanel by remember { mutableStateOf(true) }

    val activeBlock = scene?.blocks?.find { it.id == activeBlockId }
    val pagesBefore = scenes.take(activeIndex).sumOf { it.estimatedPageDuration.toDouble() }.toInt()
    val pageNumber = pagesBefore + 1

    BoxWithConstraints(modifier = modifier.fillMaxSize().background(StudioTheme.colors.canvas)) {
        val wide = maxWidth >= 960.dp
        val focus = appState.isFocusMode

        Row(modifier = Modifier.fillMaxSize()) {
            if (wide && showScenePanel && !focus) {
                SceneNavigator(
                    scenes = scenes,
                    activeIndex = activeIndex,
                    onSelect = { index ->
                        repository.selectScene(index)
                        activeBlockId = null
                    },
                    onAdd = { repository.addScene() },
                    onDelete = { index -> repository.deleteScene(index) },
                    onMove = { from, to -> repository.reorderScene(from, to) },
                    modifier = Modifier.width(Sizes.scenePanel).fillMaxHeight()
                )
                Box(
                    Modifier
                        .width(1.dp)
                        .fillMaxHeight()
                        .background(StudioTheme.colors.border)
                )
            }

            Column(modifier = Modifier.weight(1f).fillMaxHeight()) {
                if (!focus && wide) {
                    EditorHeader(
                        sceneNumber = if (scene == null) 0 else activeIndex + 1,
                        sceneCount = scenes.size,
                        slugline = scene?.slugline ?: "No scene selected",
                        act = scene?.actNumber ?: 1,
                        pageEstimate = scene?.estimatedPageDuration ?: 0f,
                        showPanelToggle = wide,
                        isPanelOpen = if (wide) showScenePanel else false,
                        onTogglePanel = {
                            if (wide) showScenePanel = !showScenePanel else appState.sceneSheetOpen = true
                        },
                        onOpenSceneSheet = { appState.sceneSheetOpen = true },
                        onFocus = { appState.isFocusMode = true }
                    )
                    Hairline()
                }

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                ) {
                    ScriptPage(
                        project = project,
                        pageNumber = pageNumber,
                        focus = focus,
                        compact = !wide,
                        modifier = Modifier
                            .then(
                                if (wide) {
                                    Modifier.widthIn(max = if (focus) 860.dp else appState.editor.pageWidth.dp)
                                        .fillMaxHeight()
                                        .padding(
                                            horizontal = if (focus) Spacing.xxl else Spacing.md,
                                            vertical = if (focus) Spacing.xl else Spacing.sm
                                        )
                                } else {
                                    Modifier
                                        .fillMaxWidth()
                                        .weight(1f)
                                }
                            )
                    ) {
                        when {
                            scene == null -> BlankPage(
                                title = "A blank screenplay",
                                message = "Add your first scene, then write the opening image.",
                                actionLabel = "Add first scene",
                                onAction = { repository.addScene() }
                            )

                            scene.blocks.isEmpty() -> BlankPage(
                                title = "A blank scene",
                                message = "Start with a slugline — INT. LOCATION - DAY.",
                                actionLabel = "Add first beat",
                                onAction = {
                                    val id = repository.appendBlock(
                                        activeIndex,
                                        ElementType.SCENE_HEADING,
                                        scene.slugline
                                    )
                                    pendingFocusBlockId = id
                                    activeBlockId = id
                                }
                            )

                            else -> ScriptBlockEditor(
                                scene = scene,
                                prefs = appState.editor,
                                isFocusMode = focus,
                                activeBlockId = activeBlockId,
                                pendingFocusBlockId = pendingFocusBlockId,
                                onFocusHandled = { pendingFocusBlockId = null },
                                onBlockFocused = { activeBlockId = it },
                                onTextChange = { blockId, text ->
                                    repository.updateBlock(activeIndex, blockId, text)
                                },
                                onEnter = { block ->
                                    val nextType = ScreenplayRules.getNextElementOnEnter(
                                        block.type,
                                        block.text.isBlank()
                                    )
                                    val newId = repository.insertBlockAfter(
                                        activeIndex,
                                        block.id,
                                        nextType
                                    )
                                    pendingFocusBlockId = newId
                                    activeBlockId = newId
                                },
                                onTab = { block, backwards ->
                                    val next = if (backwards) {
                                        ScreenplayRules.getPreviousElementOnTab(block.type)
                                    } else {
                                        ScreenplayRules.getNextElementOnTab(block.type)
                                    }
                                    repository.changeBlockType(activeIndex, block.id, next)
                                    pendingFocusBlockId = block.id
                                },
                                onBackspaceEmpty = { block ->
                                    val index = scene.blocks.indexOfFirst { it.id == block.id }
                                    val previous = scene.blocks.getOrNull(index - 1)
                                    repository.removeBlock(activeIndex, block.id)
                                    if (previous != null) {
                                        pendingFocusBlockId = previous.id
                                        activeBlockId = previous.id
                                    }
                                },
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }

                if (!wide) {
                    val isKeyboardAlive = WindowInsets.ime.getBottom(LocalDensity.current) > 0
                    if (isKeyboardAlive) {
                        // Mobile Screenplay Accessory Bar (Shown ONLY when keyboard is alive, docked directly flush above it)
                        MobileScreenplayAccessoryBar(
                        activeBlock = activeBlock,
                        characters = project.characters,
                        hasBottomDock = !focus,
                        onSelectType = { type ->
                            val block = activeBlock
                            if (block != null) {
                                repository.changeBlockType(activeIndex, block.id, type)
                                pendingFocusBlockId = block.id
                            } else {
                                val newId = repository.appendBlock(activeIndex, type)
                                pendingFocusBlockId = newId
                                activeBlockId = newId
                            }
                        },
                        onTab = {
                            val block = activeBlock ?: scene?.blocks?.lastOrNull()
                            if (block != null) {
                                val next = ScreenplayRules.getNextElementOnTab(block.type)
                                repository.changeBlockType(activeIndex, block.id, next)
                                pendingFocusBlockId = block.id
                            }
                        },
                        onEnter = {
                            val block = activeBlock ?: scene?.blocks?.lastOrNull()
                            if (block != null) {
                                val nextType = ScreenplayRules.getNextElementOnEnter(
                                    block.type,
                                    block.text.isBlank()
                                )
                                val newId = repository.insertBlockAfter(
                                    activeIndex,
                                    block.id,
                                    nextType
                                )
                                pendingFocusBlockId = newId
                                activeBlockId = newId
                            } else {
                                val newId = repository.appendBlock(activeIndex, ElementType.ACTION)
                                pendingFocusBlockId = newId
                                activeBlockId = newId
                            }
                        },
                        onInsertText = { text ->
                            val block = activeBlock
                            if (block != null) {
                                val current = block.text
                                val updated = if (current.isBlank()) text.trimStart() else "$current$text"
                                repository.updateBlock(activeIndex, block.id, updated)
                                pendingFocusBlockId = block.id
                            } else {
                                val type = if (text.startsWith("INT") || text.startsWith("EXT")) ElementType.SCENE_HEADING else ElementType.ACTION
                                val newId = repository.appendBlock(activeIndex, type, text.trimStart())
                                pendingFocusBlockId = newId
                                activeBlockId = newId
                            }
                        },
                        onInsertCharacter = { name ->
                            val block = activeBlock
                            if (block != null && block.type == ElementType.CHARACTER) {
                                repository.updateBlock(activeIndex, block.id, name)
                                val dialogueId = repository.insertBlockAfter(
                                    activeIndex,
                                    block.id,
                                    ElementType.DIALOGUE
                                )
                                pendingFocusBlockId = dialogueId
                                activeBlockId = dialogueId
                            } else {
                                val cueId = repository.appendBlock(
                                    activeIndex,
                                    ElementType.CHARACTER,
                                    name
                                )
                                val dialogueId = repository.insertBlockAfter(
                                    activeIndex,
                                    cueId,
                                    ElementType.DIALOGUE
                                )
                                pendingFocusBlockId = dialogueId
                                activeBlockId = dialogueId
                            }
                        },
                        onAddScene = { repository.addScene() },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            } else if (!focus) {
                    EditorToolbar(
                        characters = project.characters,
                        activeType = activeBlock?.type,
                        onSelectType = { type ->
                            val block = activeBlock
                            if (block != null) {
                                repository.changeBlockType(activeIndex, block.id, type)
                                pendingFocusBlockId = block.id
                            } else {
                                val newId = repository.appendBlock(activeIndex, type)
                                pendingFocusBlockId = newId
                            }
                        },
                        onInsertCharacter = { character ->
                            val cueId = repository.appendBlock(
                                activeIndex,
                                ElementType.CHARACTER,
                                character.name
                            )
                            val dialogueId = repository.insertBlockAfter(
                                activeIndex,
                                cueId,
                                ElementType.DIALOGUE
                            )
                            pendingFocusBlockId = dialogueId
                        },
                        onAddScene = { repository.addScene() }
                    )
                }
            }
            }
        }
    }
}

/** Paper-first page frame: project title, page number, then the actual writing surface. */
@Composable
private fun ScriptPage(
    project: ScriptProject,
    pageNumber: Int,
    focus: Boolean,
    compact: Boolean = false,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    val colors = StudioTheme.colors

    if (compact) {
        // Seamless edge-to-edge mobile canvas: no artificial paper margins, no duplicate page headers
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(colors.surface),
            content = content
        )
    } else {
        Surface(
            modifier = modifier.shadow(if (focus) 22.dp else 6.dp, CardShape),
            shape = CardShape,
            color = colors.surface,
            tonalElevation = 0.dp
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 22.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = project.title.ifBlank { "UNTITLED SCREENPLAY" },
                        style = StudioType.sectionLabel,
                        color = colors.inkTertiary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(Modifier.width(Spacing.md))
                    Text(
                        text = "PAGE $pageNumber",
                        style = StudioType.sectionLabel,
                        color = colors.inkTertiary
                    )
                }
                Hairline()
                Box(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    content = content
                )
            }
        }
    }
}

@Composable
private fun BlankPage(
    title: String,
    message: String,
    actionLabel: String,
    onAction: () -> Unit
) {
    val colors = StudioTheme.colors
    val statusBarTop = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val navBarBottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(
                start = Spacing.xxl,
                end = Spacing.xxl,
                top = Spacing.xxl + statusBarTop,
                bottom = Spacing.xxl + navBarBottom
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("PAGE 1", style = StudioType.sectionLabel, color = colors.inkTertiary)
        Spacer(Modifier.height(Spacing.md))
        Text(
            text = title,
            style = MaterialTheme.typography.displaySmall,
            color = colors.ink,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(Spacing.sm))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = colors.inkSecondary,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(Spacing.lg))
        Button(
            onClick = onAction,
            shape = ButtonShape,
            colors = ButtonDefaults.buttonColors(
                containerColor = colors.ink,
                contentColor = colors.canvas
            ),
            modifier = Modifier.height(44.dp)
        ) {
            Icon(
                imageVector = if (actionLabel.contains("scene", true)) Icons.Outlined.EditNote else Icons.Outlined.Add,
                contentDescription = null,
                modifier = Modifier.size(16.dp)
            )
            Spacer(Modifier.width(6.dp))
            Text(actionLabel, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun EditorHeader(
    sceneNumber: Int,
    sceneCount: Int,
    slugline: String,
    act: Int,
    pageEstimate: Float,
    showPanelToggle: Boolean,
    isPanelOpen: Boolean,
    onTogglePanel: () -> Unit,
    onOpenSceneSheet: () -> Unit,
    onFocus: () -> Unit
) {
    val colors = StudioTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.lg, vertical = Spacing.sm),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (showPanelToggle) {
            IconAction(
                icon = Icons.AutoMirrored.Outlined.ViewList,
                contentDescription = "Toggle scene list",
                onClick = onTogglePanel,
                selected = isPanelOpen
            )
            Spacer(Modifier.width(Spacing.sm))
        }

        Column(modifier = Modifier.weight(1f)) {
            SectionLabel(
                if (sceneCount == 0) "Blank screenplay"
                else "Scene $sceneNumber of $sceneCount  ·  Act $act"
            )
            Spacer(Modifier.height(2.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = slugline,
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    ),
                    color = colors.ink,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false).clickableNoIndication(onOpenSceneSheet)
                )
                if (sceneCount > 0) {
                    Spacer(Modifier.width(Spacing.sm))
                    Text(
                        text = "%.1f pgs".format(pageEstimate),
                        style = StudioType.caption,
                        color = colors.inkTertiary
                    )
                }
            }
        }

        IconAction(
            icon = Icons.Outlined.Tune,
            contentDescription = "Scene settings",
            onClick = onOpenSceneSheet
        )
        IconAction(
            icon = Icons.Outlined.Fullscreen,
            contentDescription = "Full screen",
            onClick = onFocus
        )
    }
}

private fun Modifier.clickableNoIndication(onClick: () -> Unit): Modifier =
    this.clickable(onClick = onClick)
