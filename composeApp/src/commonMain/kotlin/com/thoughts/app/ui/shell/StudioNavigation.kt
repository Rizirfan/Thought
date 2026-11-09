package com.thoughts.app.ui.shell

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.LightMode
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.thoughts.app.domain.model.ScriptProject
import com.thoughts.app.ui.AppState
import com.thoughts.app.ui.common.Hairline
import com.thoughts.app.ui.common.IconAction
import com.thoughts.app.ui.common.SectionLabel
import com.thoughts.app.ui.navigation.ScreenplayNavDestination
import com.thoughts.app.ui.theme.*

@Composable
fun StudioSidebar(
    appState: AppState,
    project: ScriptProject,
    pageCount: Float,
    sceneCount: Int,
    modifier: Modifier = Modifier
) {
    val colors = StudioTheme.colors

    Column(
        modifier = modifier
            .width(Sizes.sidebar)
            .fillMaxHeight()
            .background(colors.chrome)
            .padding(horizontal = Spacing.md, vertical = Spacing.md)
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp),
            color = colors.surfaceAlt,
            border = BorderStroke(1.dp, colors.border)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Spacing.md, vertical = Spacing.sm)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${project.activeDraft?.scenes?.size ?: 0} scenes",
                        style = StudioType.caption,
                        color = colors.inkTertiary
                    )
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    text = project.title.ifBlank { "My Screenplay" },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = colors.ink,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = project.activeDraft?.name ?: "First Draft",
                    style = StudioType.caption,
                    color = colors.inkSecondary,
                    maxLines = 1
                )
            }
        }

        Spacer(Modifier.height(Spacing.md))
        Hairline()
        Spacer(Modifier.height(Spacing.md))

        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            ScreenplayNavDestination.entries
                .filter { it.primary }
                .forEach { destination ->
                    SidebarNavItem(
                        destination = destination,
                        selected = appState.destination == destination,
                        onClick = { appState.navigate(destination) }
                    )
                }
        }

        Spacer(Modifier.height(Spacing.lg))
        Hairline()
        Spacer(Modifier.height(Spacing.lg))

        Spacer(Modifier.weight(1f))

        Hairline()
        Spacer(Modifier.height(Spacing.sm))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(24.dp))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { appState.navigate(ScreenplayNavDestination.SETTINGS) }
                    .padding(horizontal = Spacing.sm, vertical = Spacing.xs),
                horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Outlined.Settings,
                    contentDescription = null,
                    tint = if (appState.destination == ScreenplayNavDestination.SETTINGS) colors.accent else colors.inkSecondary,
                    modifier = Modifier.size(Sizes.iconSmall)
                )
                Text(
                    text = "Settings",
                    style = MaterialTheme.typography.labelMedium,
                    color = if (appState.destination == ScreenplayNavDestination.SETTINGS) colors.accent else colors.inkSecondary
                )
            }
        }
    }
}

@Composable
private fun SidebarNavItem(
    destination: ScreenplayNavDestination,
    selected: Boolean,
    onClick: () -> Unit
) {
    val colors = StudioTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.sm, vertical = 9.dp)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            ),
        horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = destination.icon,
                contentDescription = null,
                tint = if (selected) colors.accent else colors.inkSecondary,
                modifier = Modifier
                    .size(Sizes.iconSmall)
            )
            Text(
                text = destination.label,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
                color = if (selected) colors.accent else colors.inkSecondary,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun StudioRail(
    appState: AppState,
    modifier: Modifier = Modifier
) {
    val colors = StudioTheme.colors
    Column(
        modifier = modifier
            .width(Sizes.rail)
            .fillMaxHeight()
            .background(colors.chrome)
            .padding(vertical = Spacing.md),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(Spacing.xs)
    ) {
        com.thoughts.app.ui.common.StudioBrandBadge(
            size = 36.dp,
            dark = appState.isDark()
        )
        Spacer(Modifier.height(Spacing.xs))

        ScreenplayNavDestination.entries
            .filter { it.primary }
            .forEach { destination ->
                val selected = appState.destination == destination
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { appState.navigate(destination) },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = destination.icon,
                        contentDescription = destination.label,
                        tint = if (selected) colors.accent else colors.inkSecondary,
                        modifier = Modifier
                            .size(22.dp)
                    )
                }
            }

        Spacer(Modifier.weight(1f))

        IconAction(
            icon = Icons.Outlined.Settings,
            contentDescription = "Settings",
            onClick = { appState.navigate(ScreenplayNavDestination.SETTINGS) },
            selected = appState.destination == ScreenplayNavDestination.SETTINGS
        )
    }
}

@Composable
fun StudioBottomDock(
    appState: AppState,
    modifier: Modifier = Modifier
) {
    val colors = StudioTheme.colors
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(colors.canvas)
            .windowInsetsPadding(WindowInsets.navigationBars)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(Sizes.bottomNav)
                .padding(horizontal = Spacing.sm),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            ScreenplayNavDestination.entries
                .filter { it.primary }
                .forEach { destination ->
                    val selected = appState.destination == destination
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) { appState.navigate(destination) }
                            .padding(vertical = 4.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = destination.icon,
                            contentDescription = destination.label,
                            tint = if (selected) colors.ink else colors.inkTertiary,
                            modifier = Modifier.size(Sizes.icon)
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = destination.label,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 12.sp,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                            ),
                            color = if (selected) colors.ink else colors.inkTertiary,
                            maxLines = 1
                        )
                    }
                }
        }
    }
}

private val CircleShapeCompat = androidx.compose.foundation.shape.CircleShape
