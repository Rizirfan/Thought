package com.thoughts.app.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp

object Spacing {
    val xs = 4.dp
    val sm = 8.dp              // Small spacing: 8 dp
    val md = 12.dp
    val lg = 16.dp
    val screenSide = 20.dp      // Screen side padding: 20 dp
    val section = 24.dp         // Screen section spacing: 20–24 dp
    val xl = 24.dp
    val xxl = 32.dp
    val xxxl = 48.dp
}

object Radius {
    val input = 12.dp           // Apple squircle input radius
    val button = 12.dp          // Apple button radius
    val card = 18.dp            // Apple continuous rounded corners
    val panel = 20.dp
    val dock = 24.dp
    val sheet = 28.dp
}

object Sizes {
    val topBar = 56.dp          // Top app bar: 56 dp
    val bottomNav = 72.dp       // Bottom navigation: 64–80 dp (+ system inset)
    val primaryButton = 50.dp   // Primary buttons: 48–52 dp height
    val inputHeight = 54.dp     // Input fields: 52–56 dp height
    val icon = 24.dp            // Standard icons: 24 dp
    val iconSmall = 18.dp
    val sidebar = 232.dp
    val rail = 64.dp
    val inspector = 300.dp
    val scenePanel = 264.dp
    val control = 36.dp
    val controlLarge = 44.dp
    val scriptPageMaxWidth = 720.dp
    val focusColumn = 680.dp
}

val InputShape = RoundedCornerShape(Radius.input)
val ButtonShape = RoundedCornerShape(Radius.button)
val CardShape = RoundedCornerShape(Radius.card)
val PanelShape = RoundedCornerShape(Radius.panel)
val DockShape = RoundedCornerShape(Radius.dock)
val SheetShape = RoundedCornerShape(Radius.sheet)
