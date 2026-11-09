package com.thoughts.app.ui.settings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.LightMode
import androidx.compose.material.icons.outlined.SettingsBrightness
import androidx.compose.material.icons.outlined.TextFields
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.thoughts.app.ui.AppState
import com.thoughts.app.ui.EditorPreferences
import com.thoughts.app.ui.ThemeMode
import com.thoughts.app.ui.common.*
import com.thoughts.app.ui.theme.*

@Composable
fun SettingsScreen(
    appState: AppState,
    modifier: Modifier = Modifier
) {
    val colors = StudioTheme.colors
    val editor = appState.editor
    var resetNotice by remember { mutableStateOf(false) }

    BoxWithConstraints(
        modifier = modifier.fillMaxSize().background(colors.canvas),
        contentAlignment = Alignment.TopCenter
    ) {
        val pagePadding = if (maxWidth < 640.dp) Spacing.screenSide else Spacing.xxl

        LazyColumn(
            modifier = Modifier
                .widthIn(max = 720.dp)
                .fillMaxWidth()
                .fillMaxHeight(),
            contentPadding = PaddingValues(horizontal = pagePadding, vertical = Spacing.section),
            verticalArrangement = Arrangement.spacedBy(Spacing.section)
        ) {
            item {
                PageHeader(
                    title = "Settings",
                    subtitle = "Preferences are saved automatically on this device"
                )
            }

            item {
                StudioSection("Writing room") {
                    StudioSliderRow(
                        title = "Script size",
                        valueLabel = "${editor.scriptSize.toInt()} pt",
                        value = editor.scriptSize,
                        onValueChange = { value ->
                            appState.editor = editor.copy(
                                scriptSize = value,
                                scriptLineHeight = (value * 1.5f).coerceIn(18f, 40f)
                            )
                        },
                        valueRange = 12f..24f,
                        steps = 11
                    )
                    StudioSliderRow(
                        title = "Page width",
                        valueLabel = "${editor.pageWidth.toInt()} dp",
                        value = editor.pageWidth,
                        onValueChange = { value -> appState.editor = editor.copy(pageWidth = value) },
                        valueRange = 520f..960f,
                        steps = 10
                    )
                    Spacer(Modifier.height(Spacing.xs))
                    StudioToggleRow(
                        title = "Typewriter scroll",
                        subtitle = "Keep the active line centred while writing",
                        checked = editor.typewriterScroll,
                        onCheckedChange = { value -> appState.editor = editor.copy(typewriterScroll = value) }
                    )
                    StudioToggleRow(
                        title = "Page markers",
                        subtitle = "Show subtle page-break indicators in the editor",
                        checked = editor.showPageMarkers,
                        onCheckedChange = { value -> appState.editor = editor.copy(showPageMarkers = value) }
                    )
                }
            }

            item { SectionBreak() }

            item {
                StudioSection("Focus mode") {
                    Text(
                        text = "Focus mode hides the shell so only the page remains. Leave it with the pill at the " +
                            "bottom of the screen, or the focus icon in the top bar.",
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.inkSecondary
                    )
                    Spacer(Modifier.height(Spacing.sm))
                    TextButton(
                        onClick = { appState.isFocusMode = true },
                        contentPadding = PaddingValues(horizontal = 0.dp, vertical = Spacing.xs)
                    ) {
                        Icon(Icons.Outlined.TextFields, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Enter focus mode")
                    }
                }
            }

            item { SectionBreak() }

            item {
                StudioSection("Editor defaults") {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f).padding(end = Spacing.md)) {
                            Text(
                                text = "Reset writing room preferences",
                                style = MaterialTheme.typography.bodyMedium,
                                color = colors.ink
                            )
                            Text(
                                text = "Restores size, width, scroll and focus defaults",
                                style = StudioType.caption,
                                color = colors.inkTertiary
                            )
                        }
                        TextButton(
                            onClick = {
                                appState.editor = EditorPreferences()
                                resetNotice = true
                            }
                        ) {
                            Text("Reset")
                        }
                    }
                    if (resetNotice) {
                        Spacer(Modifier.height(Spacing.xs))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Outlined.Check,
                                contentDescription = null,
                                tint = colors.success,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = "Writing room preferences restored to defaults.",
                                style = StudioType.caption,
                                color = colors.success
                            )
                        }
                    }
                }
            }

            item { SectionBreak() }

            item {
                StudioSection("About") {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = Spacing.sm),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(Spacing.md)
                    ) {
                        StudioBrandBadge(size = 48.dp, dark = appState.isDark())
                        Column {
                            Text(
                                text = "THOUGHTS",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = colors.ink
                            )
                            Text(
                                text = "Offline Screenwriting Studio",
                                style = StudioType.caption,
                                color = colors.inkTertiary
                            )
                        }
                    }
                    Hairline()
                    AboutRow("Identity", "Vector Calligraphic Quill")
                    Hairline()
                    AboutRow("Version", "2.0")
                    Hairline()
                    AboutRow("Storage", "Local device only")
                    Hairline()
                    AboutRow("Formats", "Courier script · Fountain")
                }
            }

            item { Spacer(Modifier.height(Spacing.xxl)) }
        }
    }
}

@Composable
private fun SectionBreak() {
    Column {
        Hairline()
    }
}

@Composable
private fun ThemeOption(
    label: String,
    icon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = StudioTheme.colors
    Surface(
        onClick = onClick,
        shape = CardShape,
        color = if (selected) colors.surfaceAlt else colors.surface,
        border = BorderStroke(1.dp, if (selected) colors.ink else colors.border),
        shadowElevation = if (selected) 3.dp else 1.dp,
        modifier = modifier.height(52.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxSize().padding(horizontal = Spacing.sm),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (selected) colors.ink else colors.inkSecondary,
                modifier = Modifier.size(Sizes.icon)
            )
            Spacer(Modifier.width(Spacing.sm))
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                color = if (selected) colors.ink else colors.inkSecondary
            )
        }
    }
}

@Composable
private fun AboutRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 9.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = StudioTheme.colors.inkSecondary
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            color = StudioTheme.colors.ink
        )
    }
}
