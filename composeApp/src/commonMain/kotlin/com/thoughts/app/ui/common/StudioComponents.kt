package com.thoughts.app.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.thoughts.app.ui.theme.*

/** Small uppercase label that opens every flat section. */
@Composable
fun SectionLabel(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = StudioTheme.colors.inkTertiary,
    trailing: (@Composable () -> Unit)? = null
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = text.uppercase(),
            style = StudioType.sectionLabel,
            color = color
        )
        trailing?.invoke()
    }
}

/**
 * The single page heading used by every flat screen.
 * Left and right content share the same page padding, so edges line up across screens.
 */
@Composable
fun PageHeader(
    title: String,
    subtitle: String? = null,
    modifier: Modifier = Modifier,
    trailing: (@Composable RowScope.() -> Unit)? = null
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.displaySmall,
                color = StudioTheme.colors.ink
            )
            if (!subtitle.isNullOrBlank()) {
                Spacer(Modifier.height(Spacing.xs))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = StudioTheme.colors.inkSecondary
                )
            }
        }
        if (trailing != null) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(Spacing.xs),
                verticalAlignment = Alignment.CenterVertically,
                content = trailing
            )
        }
    }
}

/** A titled block with no container: label, breathing room, then content. */
@Composable
fun StudioSection(
    title: String,
    modifier: Modifier = Modifier,
    trailing: (@Composable () -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(modifier = modifier.fillMaxWidth()) {
        SectionLabel(title, trailing = trailing)
        Spacer(Modifier.height(Spacing.sm))
        content()
    }
}

/**
 * Flat interactive row used for lists. No borders, no rounded cover art:
 * selection is communicated with a quiet tint and type weight.
 */
@Composable
fun StudioRow(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    selected: Boolean = false,
    verticalPadding: Dp = Spacing.md,
    leading: (@Composable () -> Unit)? = null,
    trailing: (@Composable RowScope.() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val colors = StudioTheme.colors
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(InputShape)
            .background(if (selected) colors.accentSoft else Color.Transparent)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(horizontal = 0.dp, vertical = verticalPadding),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (leading != null) {
            leading()
            Spacer(Modifier.width(Spacing.md))
        }
        Column(modifier = Modifier.weight(1f), content = content)
        if (trailing != null) {
            Spacer(Modifier.width(Spacing.sm))
            Row(
                horizontalArrangement = Arrangement.spacedBy(Spacing.xs),
                verticalAlignment = Alignment.CenterVertically,
                content = trailing
            )
        }
    }
}

/** Thin vertical rule for inline stat groups. */
@Composable
fun VerticalRule(modifier: Modifier = Modifier, height: Dp = 28.dp) {
    Box(
        modifier = modifier
            .width(1.dp)
            .height(height)
            .background(StudioTheme.colors.border)
    )
}

@Composable
fun IconAction(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    selected: Boolean = false,
    enabled: Boolean = true,
    tint: Color = if (selected) StudioTheme.colors.accent else StudioTheme.colors.inkSecondary
) {
    Box(
        modifier = modifier
            .size(38.dp)
            .clip(CircleShape)
            .background(if (selected) StudioTheme.colors.accentSoft else Color.Transparent)
            .clickable(
                enabled = enabled,
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = if (enabled) tint else StudioTheme.colors.inkTertiary,
            modifier = Modifier.size(Sizes.icon)
        )
    }
}

@Composable
fun StudioChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    leadingIcon: ImageVector? = null
) {
    val colors = StudioTheme.colors
    Row(
        modifier = modifier
            .clip(InputShape)
            .background(if (selected) colors.accentSoft else colors.surfaceAlt)
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (leadingIcon != null) {
            Icon(
                imageVector = leadingIcon,
                contentDescription = null,
                tint = if (selected) colors.accent else colors.inkSecondary,
                modifier = Modifier.size(15.dp)
            )
        }
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
            color = if (selected) colors.accent else colors.inkSecondary,
            maxLines = 1
        )
    }
}

@Composable
fun StatusPill(
    text: String,
    color: Color,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null
) {
    Row(
        modifier = modifier
            .clip(CircleShape)
            .background(color.copy(alpha = 0.12f))
            .padding(horizontal = 8.dp, vertical = 3.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (icon != null) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(12.dp))
        }
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = color,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1
        )
    }
}

@Composable
fun MetricInline(
    value: String,
    label: String,
    modifier: Modifier = Modifier,
    valueColor: Color = StudioTheme.colors.ink
) {
    Column(modifier = modifier) {
        Text(text = value, style = StudioType.metric, color = valueColor)
        Text(text = label.uppercase(), style = StudioType.sectionLabel, color = StudioTheme.colors.inkTertiary)
    }
}

@Composable
fun GradientButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null
) {
    val colors = StudioTheme.colors
    Row(
        modifier = modifier
            .clip(ButtonShape)
            .background(Brush.horizontalGradient(listOf(colors.gradientStart, colors.gradientEnd)))
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (icon != null) {
            Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(Sizes.iconSmall))
        }
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            color = Color.White
        )
    }
}

@Composable
fun StudioTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null,
    placeholder: String? = null,
    singleLine: Boolean = true,
    minLines: Int = 1,
    enabled: Boolean = true
) {
    val colors = StudioTheme.colors
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = Sizes.inputHeight),
        label = label?.let { { Text(it, style = MaterialTheme.typography.labelMedium) } },
        placeholder = placeholder?.let {
            {
                Text(
                    it,
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.inkTertiary
                )
            }
        },
        singleLine = singleLine,
        minLines = minLines,
        enabled = enabled,
        shape = InputShape,
        textStyle = MaterialTheme.typography.bodyMedium,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = colors.accent,
            unfocusedBorderColor = colors.border,
            focusedContainerColor = colors.surface,
            unfocusedContainerColor = colors.surface,
            disabledContainerColor = colors.surfaceAlt,
            focusedTextColor = colors.ink,
            unfocusedTextColor = colors.ink,
            focusedLabelColor = colors.accent,
            unfocusedLabelColor = colors.inkTertiary,
            cursorColor = colors.accent
        )
    )
}

@Composable
fun StudioToggleRow(
    title: String,
    subtitle: String? = null,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth().padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f).padding(end = Spacing.md)) {
            Text(title, style = MaterialTheme.typography.bodyMedium, color = StudioTheme.colors.ink)
            if (subtitle != null) {
                Text(subtitle, style = StudioType.caption, color = StudioTheme.colors.inkTertiary)
            }
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = StudioTheme.colors.onAccent,
                checkedTrackColor = StudioTheme.colors.accent,
                uncheckedThumbColor = StudioTheme.colors.inkTertiary,
                uncheckedTrackColor = StudioTheme.colors.surfaceAlt,
                uncheckedBorderColor = StudioTheme.colors.border
            )
        )
    }
}

@Composable
fun StudioSliderRow(
    title: String,
    valueLabel: String,
    value: Float,
    onValueChange: (Float) -> Unit,
    valueRange: ClosedFloatingPointRange<Float>,
    modifier: Modifier = Modifier,
    steps: Int = 0
) {
    Column(modifier = modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(title, style = MaterialTheme.typography.bodyMedium, color = StudioTheme.colors.ink)
            Text(valueLabel, style = StudioType.metric, color = StudioTheme.colors.accent)
        }
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange,
            steps = steps,
            colors = SliderDefaults.colors(
                thumbColor = StudioTheme.colors.accent,
                activeTrackColor = StudioTheme.colors.accent,
                inactiveTrackColor = StudioTheme.colors.border
            )
        )
    }
}

@Composable
fun TensionMeter(
    value: Int,
    onValueChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = StudioTheme.colors
    val tint = when {
        value >= 9 -> colors.danger
        value >= 7 -> colors.warning
        value >= 4 -> colors.accent
        else -> colors.success
    }
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Tension", style = MaterialTheme.typography.labelMedium, color = colors.inkSecondary)
            Text("$value / 10", style = StudioType.metric, color = tint)
        }
        Slider(
            value = value.toFloat(),
            onValueChange = { onValueChange(it.toInt().coerceIn(1, 10)) },
            valueRange = 1f..10f,
            steps = 8,
            colors = SliderDefaults.colors(
                thumbColor = tint,
                activeTrackColor = tint,
                inactiveTrackColor = colors.border
            )
        )
    }
}

@Composable
fun EmptyState(
    icon: ImageVector,
    title: String,
    message: String,
    modifier: Modifier = Modifier,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null
) {
    Column(
        modifier = modifier.fillMaxWidth().padding(Spacing.xxl),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(StudioTheme.colors.accentSoft),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = StudioTheme.colors.accent, modifier = Modifier.size(24.dp))
        }
        Spacer(Modifier.height(Spacing.md))
        Text(title, style = MaterialTheme.typography.titleMedium, color = StudioTheme.colors.ink)
        Spacer(Modifier.height(Spacing.xs))
        Text(
            message,
            style = MaterialTheme.typography.bodySmall,
            color = StudioTheme.colors.inkSecondary,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        if (actionLabel != null && onAction != null) {
            Spacer(Modifier.height(Spacing.md))
            Button(onClick = onAction, shape = ButtonShape) { Text(actionLabel) }
        }
    }
}

/**
 * A contained surface for content that genuinely needs edges: option cards,
 * selected-scene briefs, export previews. Uses Apple-style soft diffused shadow and continuous rounded corners.
 */
@Composable
fun StudioCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    selected: Boolean = false,
    padding: Dp = Spacing.screenSide,
    shadowElevation: Dp = 4.dp,
    content: @Composable ColumnScope.() -> Unit
) {
    val colors = StudioTheme.colors
    Surface(
        onClick = onClick ?: {},
        enabled = onClick != null,
        shape = CardShape,
        color = if (selected) colors.accentSoft else colors.surface,
        border = androidx.compose.foundation.BorderStroke(
            width = 1.dp,
            color = if (selected) colors.accent else colors.border
        ),
        shadowElevation = shadowElevation,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(padding),
            content = content
        )
    }
}

/** Thin, quiet progress rail used for page targets. */
@Composable
fun StudioProgressBar(
    progress: Float,
    modifier: Modifier = Modifier,
    height: Dp = 4.dp,
    color: Color = StudioTheme.colors.accent
) {
    val fraction = progress.coerceIn(0f, 1f)
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .clip(CircleShape)
            .background(StudioTheme.colors.border)
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(fraction)
                .clip(CircleShape)
                .background(color)
        )
    }
}

@Composable
fun Hairline(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(StudioTheme.colors.border)
    )
}

@Composable
fun KeyboardHint(text: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShapeCompat)
            .background(StudioTheme.colors.surfaceAlt)
            .border(1.dp, StudioTheme.colors.border, RoundedCornerShapeCompat)
            .padding(horizontal = 5.dp, vertical = 2.dp)
    ) {
        Text(
            text = text,
            style = StudioType.caption,
            color = StudioTheme.colors.inkTertiary
        )
    }
}

private val RoundedCornerShapeCompat = androidx.compose.foundation.shape.RoundedCornerShape(5.dp)
