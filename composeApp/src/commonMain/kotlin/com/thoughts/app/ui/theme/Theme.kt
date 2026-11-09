package com.thoughts.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp

private fun StudioPalette.toLightColorScheme() = lightColorScheme(
    primary = accent,
    onPrimary = onAccent,
    primaryContainer = accentSoft,
    onPrimaryContainer = accent,
    secondary = accent,
    onSecondary = onAccent,
    secondaryContainer = accentSoft,
    onSecondaryContainer = accent,
    background = canvas,
    onBackground = ink,
    surface = surface,
    onSurface = ink,
    surfaceVariant = surfaceAlt,
    onSurfaceVariant = inkSecondary,
    surfaceContainer = surfaceAlt,
    surfaceContainerHigh = elevated,
    surfaceContainerHighest = elevated,
    outline = border,
    outlineVariant = borderStrong,
    error = danger,
    onError = onAccent,
)

private fun StudioPalette.toDarkColorScheme() = darkColorScheme(
    primary = accent,
    onPrimary = onAccent,
    primaryContainer = accentSoft,
    onPrimaryContainer = accent,
    secondary = accent,
    onSecondary = onAccent,
    secondaryContainer = accentSoft,
    onSecondaryContainer = accent,
    background = canvas,
    onBackground = ink,
    surface = surface,
    onSurface = ink,
    surfaceVariant = surfaceAlt,
    onSurfaceVariant = inkSecondary,
    surfaceContainer = surfaceAlt,
    surfaceContainerHigh = elevated,
    surfaceContainerHighest = elevated,
    outline = border,
    outlineVariant = borderStrong,
    error = danger,
    onError = onAccent,
)

val StudioShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(12.dp),
    large = RoundedCornerShape(16.dp),
    extraLarge = RoundedCornerShape(20.dp),
)

@Composable
fun ThoughtsTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    val palette = LightStudioPalette
    val scheme = palette.toLightColorScheme()

    CompositionLocalProvider(LocalStudioPalette provides palette) {
        MaterialTheme(
            colorScheme = scheme,
            typography = ThoughtsTypography,
            shapes = StudioShapes,
            content = content
        )
    }
}

@Suppress("unused")
private val defaultTypography: Typography = Typography()
