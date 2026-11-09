package com.thoughts.app.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

/**
 * Studio Minimal Monochrome — pure black, charcoal, silver, and white palette.
 * The UI never reads raw hex values; it reads these tokens through [StudioTheme].
 */
data class StudioPalette(
    val canvas: Color,
    val surface: Color,
    val surfaceAlt: Color,
    val elevated: Color,
    val chrome: Color,
    val ink: Color,
    val inkSecondary: Color,
    val inkTertiary: Color,
    val border: Color,
    val borderStrong: Color,
    val accent: Color,
    val accentHover: Color,
    val accentSoft: Color,
    val onAccent: Color,
    val gradientStart: Color,
    val gradientEnd: Color,
    val secondaryGradient: Brush,
    val success: Color,
    val warning: Color,
    val danger: Color,
    val selection: Color,
    val isDark: Boolean = false,
)

val LightStudioPalette = StudioPalette(
    canvas = Color(0xFFF9FAFB),
    surface = Color(0xFFFFFFFF),
    surfaceAlt = Color(0xFFF3F4F6),
    elevated = Color(0xFFFFFFFF),
    chrome = Color(0xFFF9FAFB),
    ink = Color(0xFF111827),
    inkSecondary = Color(0xFF4B5563),
    inkTertiary = Color(0xFF9CA3AF),
    border = Color(0xFFE5E7EB),
    borderStrong = Color(0xFFD1D5DB),
    accent = Color(0xFF111827),
    accentHover = Color(0xFF374151),
    accentSoft = Color(0x14000000),
    onAccent = Color(0xFFFFFFFF),
    gradientStart = Color(0xFF111827),
    gradientEnd = Color(0xFF4B5563),
    secondaryGradient = Brush.linearGradient(
        colors = listOf(Color(0xFF111827), Color(0xFF4B5563)),
        start = Offset(0f, 0.5f),
        end = Offset(1f, 0.5f)
    ),
    success = Color(0xFF111827),
    warning = Color(0xFF4B5563),
    danger = Color(0xFFDC2626),
    selection = Color(0x14000000),
    isDark = false,
)

val DarkStudioPalette = StudioPalette(
    canvas = Color(0xFF090A0C),
    surface = Color(0xFF131417),
    surfaceAlt = Color(0xFF1D1E22),
    elevated = Color(0xFF1D1E22),
    chrome = Color(0xFF090A0C),
    ink = Color(0xFFFFFFFF),
    inkSecondary = Color(0xFFA1A1AA),
    inkTertiary = Color(0xFF71717A),
    border = Color(0xFF27272A),
    borderStrong = Color(0xFF3F3F46),
    accent = Color(0xFFFFFFFF),
    accentHover = Color(0xFFE4E4E7),
    accentSoft = Color(0x1AFFFFFF),
    onAccent = Color(0xFF090A0C),
    gradientStart = Color(0xFFFFFFFF),
    gradientEnd = Color(0xFFA1A1AA),
    secondaryGradient = Brush.linearGradient(
        colors = listOf(Color(0xFFFFFFFF), Color(0xFFA1A1AA)),
        start = Offset(0f, 0.5f),
        end = Offset(1f, 0.5f)
    ),
    success = Color(0xFFFFFFFF),
    warning = Color(0xFFA1A1AA),
    danger = Color(0xFFEF4444),
    selection = Color(0x26FFFFFF),
    isDark = true,
)

val LocalStudioPalette = staticCompositionLocalOf { LightStudioPalette }

object StudioTheme {
    val colors: StudioPalette
        @Composable get() = LocalStudioPalette.current
}

val StudioPaperBackground = LightStudioPalette.canvas
val StudioPaperSurface = LightStudioPalette.surface
val StudioPaperSurfaceSubtle = LightStudioPalette.surfaceAlt
val StudioTextPrimary = LightStudioPalette.ink
val StudioTextSecondary = LightStudioPalette.inkSecondary
val StudioBorderHairline = LightStudioPalette.border
val StudioAccentSlate = LightStudioPalette.accent

val NightBackground = DarkStudioPalette.canvas
val NightSurface = DarkStudioPalette.surface
val NightSurfaceSubtle = DarkStudioPalette.surfaceAlt
val NightTextPrimary = DarkStudioPalette.ink
val NightTextSecondary = DarkStudioPalette.inkSecondary
val NightBorderHairline = DarkStudioPalette.border
val NightAccentSlate = DarkStudioPalette.accent

val TensionCurveBlue = LightStudioPalette.accent
val TensionCurveBlueNight = DarkStudioPalette.accent
val TensionWarningAmber = LightStudioPalette.warning
val TensionAlertRed = LightStudioPalette.danger
val TensionSuccessGreen = LightStudioPalette.success
