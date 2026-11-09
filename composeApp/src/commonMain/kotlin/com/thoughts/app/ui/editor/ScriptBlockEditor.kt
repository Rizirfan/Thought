package com.thoughts.app.ui.editor

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.input.key.*
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.thoughts.app.domain.model.ElementType
import com.thoughts.app.domain.model.ScriptElementBlock
import com.thoughts.app.domain.model.ScriptScene
import com.thoughts.app.ui.EditorPreferences
import com.thoughts.app.ui.theme.Spacing
import com.thoughts.app.ui.theme.StudioTheme
import com.thoughts.app.ui.theme.StudioType

@Composable
fun ScriptBlockEditor(
    scene: ScriptScene,
    prefs: EditorPreferences,
    isFocusMode: Boolean,
    activeBlockId: String?,
    pendingFocusBlockId: String?,
    onFocusHandled: () -> Unit,
    onBlockFocused: (String) -> Unit,
    onTextChange: (blockId: String, text: String) -> Unit,
    onEnter: (ScriptElementBlock) -> Unit,
    onTab: (ScriptElementBlock, backwards: Boolean) -> Unit,
    onBackspaceEmpty: (ScriptElementBlock) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = StudioTheme.colors
    val listState = rememberLazyListState()
    val focusRequesters = remember(scene.id) { mutableMapOf<String, FocusRequester>() }

    val pageBreaks = remember(scene.blocks, prefs.showPageMarkers) {
        val result = mutableMapOf<String, Int>()
        if (prefs.showPageMarkers) {
            var lines = 0
            var page = 1
            scene.blocks.forEach { block ->
                lines += (block.text.length / 58) + 2
                if (lines >= 54) {
                    page += 1
                    result[block.id] = page
                    lines = 0
                }
            }
        }
        result
    }

    LaunchedEffect(pendingFocusBlockId) {
        val target = pendingFocusBlockId ?: return@LaunchedEffect
        val index = scene.blocks.indexOfFirst { it.id == target }
        if (index > 2) {
            val visible = listState.layoutInfo.visibleItemsInfo.map { it.index }
            if (index !in visible) {
                listState.animateScrollToItem((index - 1).coerceAtLeast(0))
            }
        }
        withFrameNanos { }
        focusRequesters[target]?.requestFocus()
        onFocusHandled()
    }

    LaunchedEffect(activeBlockId, prefs.typewriterScroll) {
        if (!prefs.typewriterScroll) return@LaunchedEffect
        val id = activeBlockId ?: return@LaunchedEffect
        val index = scene.blocks.indexOfFirst { it.id == id }
        if (index > 2) {
            withFrameNanos { }
            listState.animateScrollToItem((index - 2).coerceAtLeast(0))
        }
    }

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val contentWidth = maxWidth
        val isMobile = contentWidth < 600.dp

        val characterIndent = if (isMobile) 32.dp else (contentWidth * 0.30f)
        val parenIndent = if (isMobile) 20.dp else (contentWidth * 0.22f)
        val dialogueIndent = if (isMobile) 12.dp else (contentWidth * 0.14f)
        val transitionIndent = if (isMobile) 24.dp else (contentWidth * 0.25f)
        val parenEndIndent = if (isMobile) 20.dp else (contentWidth * 0.30f)
        val dialogueEndIndent = if (isMobile) 12.dp else (contentWidth * 0.14f)

        val statusBarTop = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
        val navBarBottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

        val horizontalPadding = if (isFocusMode) {
            if (isMobile) 24.dp else 40.dp
        } else {
            if (isMobile) 18.dp else 24.dp
        }

        val topPadding = if (isFocusMode) {
            if (isMobile) statusBarTop + 64.dp else 40.dp
        } else {
            if (isMobile) 16.dp else 24.dp
        }

        val bottomPadding = if (isFocusMode) {
            if (isMobile) navBarBottom + 96.dp else 80.dp
        } else {
            if (isMobile) 140.dp else 120.dp
        }

        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = horizontalPadding,
                end = horizontalPadding,
                top = topPadding,
                bottom = bottomPadding
            )
        ) {
            items(scene.blocks, key = { it.id }) { block ->
                val requester = focusRequesters.getOrPut(block.id) { FocusRequester() }
                val isActive = activeBlockId == block.id
                val alpha = if (isFocusMode && activeBlockId != null && !isActive) 0.45f else 1f

                val topSpacing = when (block.type) {
                    ElementType.SCENE_HEADING -> if (isMobile) 14.dp else 20.dp
                    ElementType.CHARACTER -> if (isMobile) 10.dp else 14.dp
                    ElementType.ACTION -> if (isMobile) 6.dp else 8.dp
                    ElementType.TRANSITION -> if (isMobile) 12.dp else 16.dp
                    else -> 2.dp
                }

                Column(modifier = Modifier.padding(top = topSpacing)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 1.dp)
                        .alphaCompat(alpha),
                    verticalAlignment = Alignment.Top
                ) {
                    if (!isMobile) {
                        ElementGutter(type = block.type)
                    }

                    val style = styleFor(block.type, prefs, colors.ink)
                    val startIndent = when (block.type) {
                        ElementType.CHARACTER -> characterIndent
                        ElementType.PARENTHETICAL -> parenIndent
                        ElementType.DIALOGUE -> dialogueIndent
                        ElementType.TRANSITION -> transitionIndent
                        else -> 0.dp
                    }
                    val endIndent = when (block.type) {
                        ElementType.PARENTHETICAL -> parenEndIndent
                        ElementType.DIALOGUE -> dialogueEndIndent
                        else -> 0.dp
                    }

                    BasicTextField(
                        value = block.text,
                        onValueChange = { newText ->
                            onTextChange(block.id, transformText(block.type, newText))
                        },
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = startIndent, end = endIndent)
                            .focusRequester(requester)
                            .onFocusChanged { if (it.isFocused) onBlockFocused(block.id) }
                            .onPreviewKeyEvent { event -> handleKey(event, block, onEnter, onTab, onBackspaceEmpty) },
                        textStyle = style,
                        cursorBrush = SolidColor(colors.ink),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                        keyboardActions = KeyboardActions(onNext = { onEnter(block) }),
                        decorationBox = { innerTextField ->
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .defaultMinSize(minHeight = prefs.scriptLineHeight.dp)
                            ) {
                                if (block.text.isEmpty()) {
                                    Text(
                                        text = placeholderFor(block.type),
                                        style = style.copy(color = colors.inkTertiary.copy(alpha = 0.55f))
                                    )
                                }
                                innerTextField()
                            }
                        }
                    )
                }
                pageBreaks[block.id]?.let { page -> PageMarker(page) }
                }
            }

            item {
                Spacer(Modifier.height(180.dp))
            }
        }
    }
}

@Composable
private fun PageMarker(page: Int) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp, horizontal = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
    ) {
        Text(
            text = "PAGE $page",
            style = StudioType.sectionLabel,
            color = StudioTheme.colors.inkTertiary
        )
        Box(
            Modifier
                .weight(1f)
                .height(1.dp)
                .background(StudioTheme.colors.border)
        )
    }
}

@Composable
private fun ElementGutter(type: ElementType) {
    Text(
        text = when (type) {
            ElementType.SCENE_HEADING -> "S"
            ElementType.ACTION -> "A"
            ElementType.CHARACTER -> "C"
            ElementType.PARENTHETICAL -> "P"
            ElementType.DIALOGUE -> "D"
            ElementType.TRANSITION -> "T"
            ElementType.SHOT -> "H"
        },
        style = StudioType.caption,
        color = StudioTheme.colors.inkTertiary.copy(alpha = 0.5f),
        modifier = Modifier.width(16.dp).padding(top = 3.dp)
    )
}

@Composable
private fun Text(
    text: String,
    style: TextStyle,
    color: Color = Color.Unspecified,
    modifier: Modifier = Modifier
) {
    androidx.compose.material3.Text(text = text, style = style, color = color, modifier = modifier)
}

private fun styleFor(type: ElementType, prefs: EditorPreferences, textColor: Color): TextStyle {
    val base = StudioType.script(prefs.scriptSize, prefs.scriptLineHeight).copy(color = textColor)
    return when (type) {
        ElementType.SCENE_HEADING -> StudioType.scriptHeading(prefs.scriptSize, prefs.scriptLineHeight).copy(color = textColor)
        ElementType.ACTION -> base
        ElementType.CHARACTER -> base.copy(fontWeight = FontWeight.Bold)
        ElementType.PARENTHETICAL -> base.copy(fontStyle = androidx.compose.ui.text.font.FontStyle.Italic)
        ElementType.DIALOGUE -> base
        ElementType.TRANSITION -> base.copy(fontWeight = FontWeight.Bold, textAlign = TextAlign.End)
        ElementType.SHOT -> StudioType.scriptHeading(prefs.scriptSize, prefs.scriptLineHeight).copy(color = textColor)
    }
}

private fun transformText(type: ElementType, text: String): String = when (type) {
    ElementType.SCENE_HEADING, ElementType.CHARACTER, ElementType.TRANSITION, ElementType.SHOT ->
        text.uppercase()
    else -> text
}

private fun placeholderFor(type: ElementType): String = when (type) {
    ElementType.SCENE_HEADING -> "INT. LOCATION - DAY"
    ElementType.ACTION -> "Action…"
    ElementType.CHARACTER -> "CHARACTER"
    ElementType.PARENTHETICAL -> "(beat)"
    ElementType.DIALOGUE -> "Dialogue…"
    ElementType.TRANSITION -> "CUT TO:"
    ElementType.SHOT -> "ANGLE ON:"
}

private fun handleKey(
    event: KeyEvent,
    block: ScriptElementBlock,
    onEnter: (ScriptElementBlock) -> Unit,
    onTab: (ScriptElementBlock, Boolean) -> Unit,
    onBackspaceEmpty: (ScriptElementBlock) -> Unit
): Boolean {
    if (event.type != KeyEventType.KeyDown) return false
    return when {
        event.key == Key.Enter && !event.isShiftPressed -> {
            onEnter(block)
            true
        }
        event.key == Key.Enter && event.isShiftPressed -> false
        event.key == Key.Tab -> {
            onTab(block, event.isShiftPressed)
            true
        }
        event.key == Key.Backspace && block.text.isEmpty() -> {
            onBackspaceEmpty(block)
            true
        }
        else -> false
    }
}

private fun Modifier.alphaCompat(value: Float): Modifier = this.alpha(value)
