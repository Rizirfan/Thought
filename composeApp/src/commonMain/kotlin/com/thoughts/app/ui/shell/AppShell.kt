package com.thoughts.app.ui.shell

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.FullscreenExit
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.thoughts.app.domain.model.ScriptProject
import com.thoughts.app.ui.AppState
import com.thoughts.app.ui.common.IconAction
import com.thoughts.app.ui.common.KeyboardHint
import com.thoughts.app.ui.navigation.ScreenplayNavDestination
import com.thoughts.app.ui.theme.*

@Composable
fun AppShell(
    appState: AppState,
    project: ScriptProject,
    pageCount: Float,
    sceneCount: Int,
    modifier: Modifier = Modifier,
    inspector: (@Composable () -> Unit)? = null,
    onTogglePalette: () -> Unit,
    content: @Composable () -> Unit
) {
    val colors = StudioTheme.colors
    val showInspectorSlot = appState.destination == ScreenplayNavDestination.EDITOR

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(colors.canvas)
    ) {
        val width = maxWidth
        val compact = width < 760.dp
        val expanded = width >= 1024.dp
        val inspectorFits = width >= 1280.dp
        val showInspector = inspectorFits && appState.inspectorOpen && showInspectorSlot && inspector != null

        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            val showTopBar = !appState.isFocusMode && !appState.isExportPreviewOpen
            if (showTopBar) {
                StudioTopBar(
                    appState = appState,
                    project = project,
                    pageCount = pageCount,
                    sceneCount = sceneCount,
                    compact = compact,
                    canShowInspector = showInspectorSlot && inspectorFits,
                    onTogglePalette = onTogglePalette,
                    onToggleTheme = { appState.cycleTheme() }
                )
            }

            Row(modifier = Modifier.weight(1f).fillMaxWidth()) {
                if (!appState.isFocusMode) {
                    when {
                        expanded -> StudioSidebar(
                            appState = appState,
                            project = project,
                            pageCount = pageCount,
                            sceneCount = sceneCount
                        )
                        !compact -> StudioRail(appState = appState)
                    }
                    if (!compact) {
                        Box(Modifier.width(1.dp).fillMaxHeight().background(colors.border))
                    }
                }

                val destOrder = remember {
                    listOf(
                        ScreenplayNavDestination.HOME,
                        ScreenplayNavDestination.EDITOR,
                        ScreenplayNavDestination.EXPORT,
                        ScreenplayNavDestination.SETTINGS
                    )
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .pointerInput(appState.destination) {
                            if (appState.isFocusMode) return@pointerInput
                            var totalDrag = 0f
                            detectHorizontalDragGestures(
                                onDragStart = { totalDrag = 0f },
                                onHorizontalDrag = { _, dragAmount -> totalDrag += dragAmount },
                                onDragEnd = {
                                    val threshold = 120f
                                    if (kotlin.math.abs(totalDrag) > threshold) {
                                        val idx = destOrder.indexOf(appState.destination).coerceIn(0, destOrder.lastIndex)
                                        val nextIdx = if (totalDrag < 0) (idx + 1).coerceAtMost(destOrder.lastIndex)
                                                   else (idx - 1).coerceAtLeast(0)
                                        if (nextIdx != idx) {
                                            appState.navigate(destOrder[nextIdx])
                                        }
                                    }
                                }
                            )
                        }
                ) {
                    content()
                }

                if (showInspector && !appState.isFocusMode) {
                    Box(Modifier.width(1.dp).fillMaxHeight().background(colors.border))
                    InspectorPanel(
                        title = "Scene",
                        onClose = { appState.inspectorOpen = false },
                        content = inspector
                    )
                }
            }

            val imeBottom = WindowInsets.ime.getBottom(LocalDensity.current)
            val isKeyboardOpen = imeBottom > 0
            val showBottomDock = compact && !appState.isFocusMode && !appState.isExportPreviewOpen && !isKeyboardOpen
            if (showBottomDock) {
                StudioBottomDock(appState = appState)
            }
        }

        if (appState.isFocusMode) {
            if (compact) {
                // Mobile-dedicated exit fullscreen button:
                Surface(
                    onClick = { appState.isFocusMode = false },
                    shape = CircleShape,
                    color = colors.chrome,
                    shadowElevation = 8.dp,
                    border = BorderStroke(1.dp, colors.border),
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .windowInsetsPadding(WindowInsets.statusBars)
                        .padding(top = 12.dp, end = 16.dp)
                        .height(42.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.FullscreenExit,
                            contentDescription = "Exit full screen",
                            tint = colors.ink,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "Exit",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = colors.ink
                        )
                    }
                }
            } else {
                // Desktop escape hatch with Esc hint
                Surface(
                    onClick = { appState.isFocusMode = false },
                    shape = CircleShape,
                    color = colors.chrome,
                    shadowElevation = 12.dp,
                    border = BorderStroke(1.dp, colors.border),
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 24.dp)
                        .height(42.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.FullscreenExit,
                            contentDescription = "Exit full screen",
                            tint = colors.ink,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "Exit Fullscreen",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = colors.ink
                        )
                        KeyboardHint("Esc")
                    }
                }
            }
        }
    }
}

@Composable
fun InspectorPanel(
    title: String,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val colors = StudioTheme.colors
    Column(
        modifier = modifier
            .width(Sizes.inspector)
            .fillMaxHeight()
            .background(colors.chrome)
            .padding(Spacing.lg)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title.uppercase(),
                style = StudioType.sectionLabel,
                color = colors.inkTertiary
            )
            IconAction(
                icon = Icons.Outlined.Close,
                contentDescription = "Close inspector",
                onClick = onClose
            )
        }
        Spacer(Modifier.height(Spacing.md))
        content()
    }
}

private val CircleShape = androidx.compose.foundation.shape.CircleShape
