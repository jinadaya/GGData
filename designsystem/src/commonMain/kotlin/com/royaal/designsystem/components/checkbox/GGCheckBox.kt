package com.royaal.designsystem.components.checkbox

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.royaal.designsystem.icons.AppIcons
import com.royaal.designsystem.theme.AppTheme
import com.royaal.designsystem.theme.CheckboxColorsVariant
import com.royaal.designsystem.theme.checkboxColors
import com.royaal.designsystem.theme.textColors
import com.royaal.designsystem.util.pressScale

private val CheckboxSize = 20.dp
private val CheckIconSize = 16.dp
private val CheckboxCornerRadius = 4.dp
private val BorderWidth = 1.5.dp
private val TextSpacing = 8.dp

/** Насколько уменьшена галочка в момент появления. */
private const val CheckInitialScale = 0.6f
private const val DisabledTextAlpha = 0.38f

private const val AnimationDurationMs = 150
private val colorAnimationSpec: AnimationSpec<Color> =
    tween(AnimationDurationMs, easing = FastOutSlowInEasing)
private val checkAnimationSpec: AnimationSpec<Float> =
    tween(AnimationDurationMs, easing = FastOutSlowInEasing)

/**
 * @param text если задан, кликабельна вся строка (чекбокс + текст).
 * @param important вариант [com.royaal.designsystem.theme.CheckboxColors.important];
 * при enabled = false игнорируется, используется disabled.
 */
@Composable
fun GGCheckbox(
    checked: Boolean,
    modifier: Modifier = Modifier,
    text: String? = null,
    enabled: Boolean = true,
    important: Boolean = false,
    customIcon: ImageVector? = null,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    onValueChanged: (Boolean) -> Unit,
) {
    val colors = AppTheme.checkboxColors
    val variant = when {
        !enabled -> colors.disabled
        important -> colors.important
        else -> colors.enabled
    }

    val containerColor by animateColorAsState(
        targetValue = variant.containerColor(checked),
        animationSpec = colorAnimationSpec,
        label = "CheckboxContainer",
    )
    val borderColor by animateColorAsState(
        targetValue = variant.borderColor(checked),
        animationSpec = colorAnimationSpec,
        label = "CheckboxBorder",
    )
    val contentColor by animateColorAsState(
        targetValue = variant.contentColor,
        animationSpec = colorAnimationSpec,
        label = "CheckboxContent",
    )
    val checkProgress by animateFloatAsState(
        targetValue = if (checked) 1f else 0f,
        animationSpec = checkAnimationSpec,
        label = "CheckboxCheck",
    )

    Row(
        modifier = modifier.pressScale(interactionSource, enabled).toggleable(
            value = checked,
            onValueChange = onValueChanged,
            enabled = enabled,
            role = Role.Checkbox,
            interactionSource = interactionSource,
            indication = null,
        ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(TextSpacing),
    ) {
        // Анимированные значения читаются в drawBehind / graphicsLayer,
        // поэтому анимация идёт без рекомпозиции.
        Box(
            modifier = Modifier
                .size(CheckboxSize)
                .drawBehind {
                    val radius = CheckboxCornerRadius.toPx()
                    drawRoundRect(
                        color = containerColor,
                        cornerRadius = CornerRadius(radius),
                    )

                    val borderPx = BorderWidth.toPx()
                    val innerRadius = (radius - borderPx / 2f).coerceAtLeast(0f)
                    drawRoundRect(
                        color = borderColor,
                        topLeft = Offset(borderPx / 2f, borderPx / 2f),
                        size = Size(size.width - borderPx, size.height - borderPx),
                        cornerRadius = CornerRadius(innerRadius),
                        style = Stroke(width = borderPx),
                    )
                },
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = customIcon ?: AppIcons.check,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier
                    .size(CheckIconSize)
                    .graphicsLayer {
                        val scale = CheckInitialScale + (1f - CheckInitialScale) * checkProgress
                        scaleX = scale
                        scaleY = scale
                        alpha = checkProgress
                    },
            )
        }

        if (text != null) {
            Text(
                text = text,
                color = if (enabled) AppTheme.textColors.primary
                else AppTheme.textColors.disabled,
            )
        }
    }
}

private fun CheckboxColorsVariant.containerColor(checked: Boolean): Color =
    if (checked) checkedContainer.transparentToward(uncheckedContainer)
    else uncheckedContainer.transparentToward(checkedContainer)

private fun CheckboxColorsVariant.borderColor(checked: Boolean): Color =
    if (checked) checkedBorder.transparentToward(uncheckedBorder)
    else uncheckedBorder.transparentToward(checkedBorder)

/**
 * Color.Transparent — это чёрный с alpha = 0, и анимация от него к светлому цвету
 * проходит через серый. Берём RGB у второго цвета, чтобы менялась только прозрачность.
 */
private fun Color.transparentToward(other: Color): Color =
    if (alpha == 0f) other.copy(alpha = 0f) else this

@Preview(showBackground = true)
@Composable
private fun GGCheckboxPreview() = AppTheme {
    Column(
        modifier = Modifier.padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        var checked by remember { mutableStateOf(false) }
        GGCheckbox(checked = checked, onValueChanged = { checked = it }, text = "Интерактивный")

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            GGCheckbox(checked = false, onValueChanged = {})
            GGCheckbox(checked = true, onValueChanged = {})
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            GGCheckbox(checked = false, onValueChanged = {}, important = true)
            GGCheckbox(checked = true, onValueChanged = {}, important = true)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            GGCheckbox(checked = false, onValueChanged = {}, enabled = false)
            GGCheckbox(checked = true, onValueChanged = {}, enabled = false)
        }
        GGCheckbox(checked = true, onValueChanged = {}, text = "Отключён", enabled = false)
    }
}