package com.thoughts.app.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.thoughts.app.ui.theme.StudioTheme
import com.thoughts.app.ui.theme.StudioType
import androidx.compose.foundation.Canvas

/**
 * Pure vector implementation of the THOUGHTS brand logo mark.
 * Based on the calligraphic chisel quill / thought blade identity.
 * Renders at any density without raster artifacts or loading delays.
 */
@Composable
fun StudioBrandMark(
    modifier: Modifier = Modifier,
    tint: Color = StudioTheme.colors.ink
) {
    Canvas(modifier = modifier) {
        drawQuillMark(tint = tint)
    }
}

/**
 * Draws the signature chisel quill mark inside the caller's [DrawScope].
 */
fun DrawScope.drawQuillMark(tint: Color) {
    val w = size.width
    val h = size.height

    // Mathematical Bezier coordinates faithfully matching the vector identity
    val cx = w * 0.5f
    val cy = h * 0.5f

    // Proportional dimensions
    val markH = h * 0.82f
    val widthScale = markH / 600f
    val startY = cy - (markH * 0.5f)

    // Center optical alignment
    val offsetX = 10f * widthScale

    val topXRight = cx + (45f * widthScale) + offsetX
    val topXLeft = cx - (35f * widthScale) + offsetX
    val topYLeft = startY + (28f * widthScale)
    val bottomX = cx - (45f * widthScale) + offsetX
    val bottomY = startY + markH

    val path = Path().apply {
        // Top bevel cut
        moveTo(topXLeft, topYLeft)
        lineTo(topXRight, startY)

        // Smooth tapering calligraphic curves along the right edge
        cubicTo(
            x1 = cx + (45f * widthScale) + offsetX, y1 = startY + (markH * 0.25f),
            x2 = cx + (25f * widthScale) + offsetX, y2 = startY + (markH * 0.55f),
            x3 = cx + (5f * widthScale) + offsetX,  y3 = startY + (markH * 0.75f)
        )
        cubicTo(
            x1 = cx + (5f * widthScale) + offsetX,  y1 = startY + (markH * 0.75f),
            x2 = cx - (15f * widthScale) + offsetX, y2 = startY + (markH * 0.90f),
            x3 = bottomX,                          y3 = bottomY
        )

        // Clean vertical spine along the left edge
        lineTo(topXLeft, topYLeft)
        close()
    }

    drawPath(path = path, color = tint)
}

/**
 * Sleek luxury studio badge containing the vector quill mark.
 */
@Composable
fun StudioBrandBadge(
    modifier: Modifier = Modifier,
    size: Dp = 40.dp,
    dark: Boolean = com.thoughts.app.ui.theme.StudioTheme.colors == com.thoughts.app.ui.theme.DarkStudioPalette
) {
    val bgColor = if (dark) Color(0xFF141418) else Color.White
    val markColor = if (dark) Color.White else Color(0xFF0D0D11)
    val borderColor = if (dark) Color(0xFF26262B) else Color(0xFFE5E5EA)

    Box(
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape(size * 0.28f))
            .background(bgColor)
            .border(1.dp, borderColor, RoundedCornerShape(size * 0.28f)),
        contentAlignment = Alignment.Center
    ) {
        StudioBrandMark(
            modifier = Modifier.size(size * 0.62f),
            tint = markColor
        )
    }
}

/**
 * Full editorial brand header: Vector mark + THOUGHTS typography.
 */
@Composable
fun StudioBrandHeader(
    modifier: Modifier = Modifier,
    compact: Boolean = false,
    dark: Boolean = com.thoughts.app.ui.theme.StudioTheme.colors == com.thoughts.app.ui.theme.DarkStudioPalette,
    subtitle: String = "OFFLINE SCREENPLAY STUDIO"
) {
    val colors = StudioTheme.colors

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        StudioBrandBadge(
            size = if (compact) 36.dp else 42.dp,
            dark = dark
        )

        Column {
            Text(
                text = "thoughts",
                style = if (compact) MaterialTheme.typography.titleMedium else MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                color = colors.ink
            )
            if (subtitle.isNotBlank()) {
                Text(
                    text = subtitle,
                    style = StudioType.sectionLabel,
                    color = colors.inkTertiary,
                    letterSpacing = 1.2.sp
                )
            }
        }
    }
}
