package com.thoughts.app.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.FileDownload
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.ui.graphics.vector.ImageVector

enum class ScreenplayNavDestination(
    val label: String,
    val icon: ImageVector,
    val primary: Boolean = true
) {
    HOME("Home", Icons.Outlined.Home),
    EDITOR("Write", Icons.Outlined.Edit),
    EXPORT("Export", Icons.Outlined.FileDownload),
    SETTINGS("Settings", Icons.Outlined.Settings, primary = false)
}
