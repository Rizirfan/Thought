package com.thoughts.app.ui.shell

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.FormatListBulleted
import androidx.compose.material.icons.automirrored.outlined.ViewSidebar
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.Fullscreen
import androidx.compose.material.icons.outlined.LightMode
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.thoughts.app.domain.model.ScriptProject
import com.thoughts.app.ui.AppState
import com.thoughts.app.ui.common.IconAction
import com.thoughts.app.ui.common.KeyboardHint
import com.thoughts.app.ui.common.StatusPill
import com.thoughts.app.ui.navigation.ScreenplayNavDestination
import com.thoughts.app.ui.theme.Sizes
import com.thoughts.app.ui.theme.Spacing
import com.thoughts.app.ui.theme.StudioTheme
import com.thoughts.app.ui.theme.StudioType

@Composable
fun StudioTopBar(
    appState: AppState,
    project: ScriptProject,
    pageCount: Float,
    sceneCount: Int,
    compact: Boolean,
    canShowInspector: Boolean,
    onTogglePalette: () -> Unit,
    onToggleTheme: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = StudioTheme.colors

    val isHome = appState.destination == ScreenplayNavDestination.HOME

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(colors.canvas)
            .windowInsetsPadding(WindowInsets.statusBars)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(if (isHome) 64.dp else Sizes.topBar)
                .padding(
                    start = if (isHome) 24.dp else Spacing.screenSide,
                    end = Spacing.screenSide,
                    top = if (isHome) 6.dp else 0.dp,
                    bottom = if (isHome) 4.dp else 0.dp
                ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
            ) {
                // Proper back arrow whenever on Write or Export
                if (!isHome) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) { appState.navigate(ScreenplayNavDestination.HOME) },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                            contentDescription = "Back to screenplays",
                            tint = colors.ink,
                            modifier = Modifier.size(Sizes.icon)
                        )
                    }
                }

                // Page Name on top - NO logo badge
                val pageTitle = when (appState.destination) {
                    ScreenplayNavDestination.HOME -> "Screenplays"
                    ScreenplayNavDestination.EDITOR -> project.title.ifBlank { "Screenplay" }
                    ScreenplayNavDestination.EXPORT -> "Export"
                    ScreenplayNavDestination.SETTINGS -> "Settings"
                }

                Text(
                    text = pageTitle,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontSize = if (isHome) 24.sp else 20.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = if (isHome) (-0.5).sp else 0.sp
                    ),
                    color = colors.ink,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = if (isHome) Modifier.padding(start = 2.dp) else Modifier
                )
            }

            // Mobile Action Icons (per-page clean, dedicated controls, zero 3-dots list menu)
            if (compact) {
                when (appState.destination) {
                    ScreenplayNavDestination.HOME -> {
                        // Separate Settings button on home page; plain, no list menu
                        IconAction(
                            icon = Icons.Outlined.Settings,
                            contentDescription = "Settings",
                            onClick = { appState.navigate(ScreenplayNavDestination.SETTINGS) }
                        )
                    }
                    ScreenplayNavDestination.EDITOR -> {
                        // In write page: direct scene list and fullscreen; list menu removed
                        IconAction(
                            icon = Icons.AutoMirrored.Outlined.FormatListBulleted,
                            contentDescription = "Scenes list",
                            onClick = { appState.sceneSheetOpen = true }
                        )
                        IconAction(
                            icon = Icons.Outlined.Fullscreen,
                            contentDescription = "Full screen",
                            onClick = { appState.isFocusMode = true }
                        )
                    }
                    ScreenplayNavDestination.EXPORT -> {
                        // Plain for export: back arrow and title, no list menu
                    }
                    ScreenplayNavDestination.SETTINGS -> {
                        // Plain for settings
                    }
                }
            }

            if (!compact) {
                MetricTopBar(value = "%.1f".format(pageCount), label = "pages")
                Spacer(Modifier.width(Spacing.md))
                MetricTopBar(value = "$sceneCount", label = "scenes")
                Spacer(Modifier.width(Spacing.lg))
            }

            val isHomeOrExport = appState.destination == ScreenplayNavDestination.HOME ||
                                 appState.destination == ScreenplayNavDestination.EXPORT

            if (!compact && !isHomeOrExport) {
                IconAction(
                    icon = Icons.Outlined.Search,
                    contentDescription = "Command palette",
                    onClick = onTogglePalette
                )
                Spacer(Modifier.width(Spacing.xs))
                KeyboardHint("⌘K")
                Spacer(Modifier.width(Spacing.xs))
            }

            if (canShowInspector && !compact) {
                IconAction(
                    icon = Icons.AutoMirrored.Outlined.ViewSidebar,
                    contentDescription = "Toggle inspector",
                    onClick = { appState.inspectorOpen = !appState.inspectorOpen },
                    selected = appState.inspectorOpen
                )
            }
            if (!compact && appState.destination == ScreenplayNavDestination.EDITOR) {
                IconAction(
                    icon = Icons.Outlined.Fullscreen,
                    contentDescription = "Full screen",
                    onClick = {
                        appState.isFocusMode = true
                    }
                )
            }
            if (!compact) {
                IconAction(
                    icon = Icons.Outlined.Settings,
                    contentDescription = "Settings",
                    onClick = { appState.navigate(ScreenplayNavDestination.SETTINGS) },
                    selected = appState.destination == ScreenplayNavDestination.SETTINGS
                )
            }
        }
    }
}

@Composable
private fun MetricTopBar(value: String, label: String) {
    Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(value, style = StudioType.metric, color = StudioTheme.colors.ink)
        Text(
            label.uppercase(),
            style = StudioType.sectionLabel,
            color = StudioTheme.colors.inkTertiary,
            fontWeight = FontWeight.Medium
        )
    }
}
