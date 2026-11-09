package com.thoughts.app

import androidx.compose.runtime.remember
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.isCtrlPressed
import androidx.compose.ui.input.key.isMetaPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.type
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.application
import com.thoughts.app.ui.AppState

fun main() = application {
    val appState = remember { AppState() }

    Window(
        onCloseRequest = ::exitApplication,
        title = "THOUGHTS — Screenwriting Studio",
        icon = painterResource("icon.png"),
        state = WindowState(width = 1240.dp, height = 820.dp),
        onPreviewKeyEvent = { event ->
            if (event.type != KeyEventType.KeyDown) {
                false
            } else if ((event.isCtrlPressed || event.isMetaPressed) && event.key == Key.K) {
                appState.paletteOpen = !appState.paletteOpen
                true
            } else if (event.key == Key.Escape) {
                when {
                    appState.paletteOpen -> {
                        appState.paletteOpen = false
                        true
                    }
                    appState.sceneSheetOpen -> {
                        appState.sceneSheetOpen = false
                        true
                    }
                    appState.isFocusMode -> {
                        appState.isFocusMode = false
                        true
                    }
                    else -> false
                }
            } else {
                false
            }
        }
    ) {
        App(appState = appState)
    }
}
