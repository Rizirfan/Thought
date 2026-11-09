package com.thoughts.app

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.thoughts.app.data.repository.ScreenplayRepository
import com.thoughts.app.ui.AppState
import com.thoughts.app.ui.command.CommandPalette
import com.thoughts.app.ui.components.SceneDrawerSheet
import com.thoughts.app.ui.editor.SceneInspector
import com.thoughts.app.ui.editor.WritingRoomScreen
import com.thoughts.app.ui.export.ExportScreen
import com.thoughts.app.ui.home.ProjectHomeScreen
import com.thoughts.app.ui.navigation.ScreenplayNavDestination
import com.thoughts.app.ui.settings.SettingsScreen
import com.thoughts.app.ui.shell.AppShell
import com.thoughts.app.ui.theme.ThoughtsTheme

@Composable
fun App(
    repository: ScreenplayRepository = remember { ScreenplayRepository() },
    appState: AppState = remember { AppState() },
    modifier: Modifier = Modifier
) {
    var settingsLoaded by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        runCatching { appState.load() }
        settingsLoaded = true
    }
    LaunchedEffect(appState.themeMode, appState.editor) {
        if (settingsLoaded) runCatching { appState.save() }
    }

    val project by repository.currentProject.collectAsState()
    val activeIndex by repository.activeSceneIndex.collectAsState()
    val scenes = project.activeDraft?.scenes.orEmpty()
    val pageCount = scenes.sumOf { it.estimatedPageDuration.toDouble() }.toFloat()

    val inspectorSlot: (@Composable () -> Unit)? = when (appState.destination) {
        ScreenplayNavDestination.EDITOR -> ({ SceneInspector(repository, activeIndex) })
        else -> null
    }

    ThoughtsTheme(darkTheme = appState.isDark()) {
        Box(modifier = Modifier.fillMaxSize()) {
            AppShell(
                appState = appState,
                project = project,
                pageCount = pageCount,
                sceneCount = scenes.size,
                inspector = inspectorSlot,
                onTogglePalette = { appState.paletteOpen = true }
            ) {
                val destOrder = listOf(
                    ScreenplayNavDestination.HOME,
                    ScreenplayNavDestination.EDITOR,
                    ScreenplayNavDestination.EXPORT,
                    ScreenplayNavDestination.SETTINGS
                )
                AnimatedContent(
                    targetState = appState.destination,
                    transitionSpec = {
                        val fromIdx = destOrder.indexOf(initialState).coerceAtLeast(0)
                        val toIdx = destOrder.indexOf(targetState).coerceAtLeast(0)
                        val forward = toIdx > fromIdx
                        if (forward) {
                            (slideInHorizontally(tween(280)) { it / 4 } + fadeIn(tween(280))) togetherWith
                                (slideOutHorizontally(tween(280)) { -it / 4 } + fadeOut(tween(200)))
                        } else {
                            (slideInHorizontally(tween(280)) { -it / 4 } + fadeIn(tween(280))) togetherWith
                                (slideOutHorizontally(tween(280)) { it / 4 } + fadeOut(tween(200)))
                        }
                    },
                    label = "screenTransition"
                ) { destination ->
                    when (destination) {
                        ScreenplayNavDestination.HOME -> ProjectHomeScreen(repository, appState)
                        ScreenplayNavDestination.EDITOR -> WritingRoomScreen(repository, appState)
                        ScreenplayNavDestination.EXPORT -> ExportScreen(repository, appState)
                        ScreenplayNavDestination.SETTINGS -> SettingsScreen(appState)
                    }
                }
            }

            if (appState.paletteOpen) {
                CommandPalette(
                    appState = appState,
                    repository = repository,
                    onDismiss = { appState.paletteOpen = false }
                )
            }
        }

        if (appState.sceneSheetOpen) {
            SceneDrawerSheet(
                scenes = scenes,
                activeSceneIndex = activeIndex,
                onSelectScene = { index -> repository.selectScene(index) },
                onAddScene = { repository.addScene() },
                onDeleteScene = { index -> repository.deleteScene(index) },
                onMoveScene = { from, to -> repository.reorderScene(from, to) },
                onDismiss = { appState.sceneSheetOpen = false }
            )
        }
    }
}
