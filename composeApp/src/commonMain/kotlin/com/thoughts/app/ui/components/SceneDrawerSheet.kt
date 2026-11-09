package com.thoughts.app.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.thoughts.app.domain.model.ScriptScene
import com.thoughts.app.ui.editor.SceneNavigator
import com.thoughts.app.ui.theme.StudioTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SceneDrawerSheet(
    scenes: List<ScriptScene>,
    activeSceneIndex: Int,
    onSelectScene: (Int) -> Unit,
    onAddScene: () -> Unit,
    onDeleteScene: (Int) -> Unit,
    onMoveScene: (from: Int, to: Int) -> Unit,
    onDismiss: () -> Unit
) {
    val colors = StudioTheme.colors
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false),
        containerColor = colors.elevated,
        dragHandle = {
            BottomSheetDefaults.DragHandle(color = colors.borderStrong)
        }
    ) {
        SceneNavigator(
            scenes = scenes,
            activeIndex = activeSceneIndex,
            onSelect = { index ->
                onSelectScene(index)
                onDismiss()
            },
            onAdd = {
                onAddScene()
                onDismiss()
            },
            onDelete = onDeleteScene,
            onMove = onMoveScene,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 560.dp)
                .padding(bottom = 16.dp)
        )
    }
}
