package com.thoughts.app.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.thoughts.app.data.SettingsStorage
import com.thoughts.app.ui.navigation.ScreenplayNavDestination
import kotlin.math.roundToInt

enum class ThemeMode(val label: String) {
    LIGHT("Light"),
    DARK("Dark"),
    SYSTEM("System")
}

data class EditorPreferences(
    val scriptSize: Float = 17f,
    val scriptLineHeight: Float = 26f,
    val pageWidth: Float = 720f,
    val showPageMarkers: Boolean = false,
    val typewriterScroll: Boolean = true,
    val defaultFocusOnOpen: Boolean = false,
)

/**
 * Single UI state holder shared by the shell and every screen.
 * Persisted preferences are loaded from and written to [SettingsStorage].
 */
class AppState {
    var destination by mutableStateOf(ScreenplayNavDestination.HOME)
    var themeMode by mutableStateOf(ThemeMode.LIGHT)
    var isFocusMode by mutableStateOf(false)
    var paletteOpen by mutableStateOf(false)
    var inspectorOpen by mutableStateOf(true)
    var sceneSheetOpen by mutableStateOf(false)
    var isExportPreviewOpen by mutableStateOf(false)
    var editor by mutableStateOf(EditorPreferences())

    @Composable
    fun isDark(): Boolean = false

    fun navigate(destination: ScreenplayNavDestination) {
        this.destination = destination
        this.isFocusMode = false
        this.isExportPreviewOpen = false
        this.paletteOpen = false
    }

    fun cycleTheme() {
        themeMode = ThemeMode.LIGHT
    }

    fun load() {
        themeMode = ThemeMode.LIGHT
        editor = EditorPreferences(
            scriptSize = SettingsStorage.getFloat("script_size", 16f).coerceIn(12f, 26f),
            scriptLineHeight = SettingsStorage.getFloat("script_line_height", 24f).coerceIn(18f, 40f),
            pageWidth = SettingsStorage.getFloat("page_width", 720f).coerceIn(480f, 980f),
            showPageMarkers = SettingsStorage.getBoolean("page_markers", false),
            typewriterScroll = SettingsStorage.getBoolean("typewriter_scroll", true),
            defaultFocusOnOpen = false
        )
        isFocusMode = false
    }

    fun save() {
        SettingsStorage.putString("theme_mode", themeMode.name)
        SettingsStorage.putFloat("script_size", editor.scriptSize)
        SettingsStorage.putFloat("script_line_height", editor.scriptLineHeight)
        SettingsStorage.putFloat("page_width", editor.pageWidth)
        SettingsStorage.putBoolean("page_markers", editor.showPageMarkers)
        SettingsStorage.putBoolean("typewriter_scroll", editor.typewriterScroll)
        SettingsStorage.putBoolean("default_focus", editor.defaultFocusOnOpen)
    }
}

fun Float.toWhole(): Int = this.roundToInt()
