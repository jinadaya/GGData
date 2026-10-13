package com.royaal.designsystem.components.chips

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.EaseOut
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.text.BasicText
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
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.royaal.designsystem.icons.AppIcons
import com.royaal.designsystem.theme.AppTheme
import com.royaal.designsystem.theme.filterChipColors
import com.royaal.designsystem.util.pressScale

private val ChipHeight = 32.dp
private val HorizontalPadding = 12.dp
private val CornerRadiusDp = 8.dp
private val BorderWidth = 1.dp
private val IconSize = 16.dp
private val IconSpacing = 8.dp
private val CountSpacing = 4.dp
private val ChipRowSpacing = 8.dp

private const val AnimationDurationMs = 150
private val colorAnimationSpec: AnimationSpec<Color> = tween(AnimationDurationMs, easing = EaseOut)

/**
 * Фильтр-чип GGData.
 *
 * @param selected включён ли фильтр. У menu-чипа — применены ли в меню какие-то значения.
 * @param count необязательное число после подписи, например количество результатов
 * или выбранных в меню значений.
 * @param menu чип открывает меню вместо переключения: шеврон в конце, без галочки.
 */
@Composable
fun GGFilterChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    count: String? = null,
    menu: Boolean = false,
    enabled: Boolean = true,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
) {
    val colors = AppTheme.filterChipColors
    val isPressed by interactionSource.collectIsPressedAsState()

    val containerColor by animateColorAsState(
        targetValue = when {
            !enabled -> colors.disabledContainer
            selected -> colors.selectedContainer
            isPressed -> colors.pressedContainer
            else -> colors.container
        },
        animationSpec = colorAnimationSpec,
        label = "FilterChipContainer",
    )
    val borderColor by animateColorAsState(
        targetValue = when {
            !enabled -> colors.disabledBorder
            selected -> colors.selectedBorder
            else -> colors.border
        },
        animationSpec = colorAnimationSpec,
        label = "FilterChipBorder",
    )
    val contentColor by animateColorAsState(
        targetValue = when {
            !enabled -> colors.disabledContent
            selected -> colors.selectedContent
            else -> colors.content
        },
        animationSpec = colorAnimationSpec,
        label = "FilterChipContent",
    )
    val countColor by animateColorAsState(
        targetValue = when {
            !enabled -> colors.disabledContent
            selected -> colors.selectedCount
            else -> colors.count
        },
        animationSpec = colorAnimationSpec,
        label = "FilterChipCount",
    )

    val gestureModifier = if (menu) {
        Modifier.clickable(
            interactionSource = interactionSource,
            indication = null,
            enabled = enabled,
            role = Role.DropdownList,
            onClick = onClick,
        )
    } else {
        Modifier.toggleable(
            value = selected,
            interactionSource = interactionSource,
            indication = null,
            enabled = enabled,
            role = Role.Checkbox,
            onValueChange = { onClick() },
        )
    }

    Row(
        modifier = modifier
            .pressScale(interactionSource, enabled)
            .height(ChipHeight)
            .drawBehind {
                val radius = CornerRadiusDp.toPx()
                drawRoundRect(color = containerColor, cornerRadius = CornerRadius(radius))
                val borderPx = BorderWidth.toPx()
                drawRoundRect(
                    color = borderColor,
                    topLeft = Offset(borderPx / 2f, borderPx / 2f),
                    size = Size(size.width - borderPx, size.height - borderPx),
                    cornerRadius = CornerRadius((radius - borderPx / 2f).coerceAtLeast(0f)),
                    style = Stroke(width = borderPx),
                )
            }
            .then(gestureModifier)
            .padding(horizontal = HorizontalPadding),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AnimatedVisibility(
            visible = selected && !menu,
            enter = fadeIn(tween(AnimationDurationMs, easing = EaseOut)) +
                    expandHorizontally(tween(AnimationDurationMs, easing = EaseOut)),
            exit = fadeOut(tween(AnimationDurationMs, easing = EaseOut)) +
                    shrinkHorizontally(tween(AnimationDurationMs, easing = EaseOut)),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                CheckIcon(color = { contentColor })
                Spacer(Modifier.width(IconSpacing))
            }
        }

        BasicText(
            text = text,
            style = AppTheme.typography.bodyMedium,
            color = { contentColor },
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )

        if (count != null) {
            Spacer(Modifier.width(CountSpacing))
            BasicText(
                text = count,
                style = AppTheme.typography.bodySmall,
                color = { countColor },
                maxLines = 1,
            )
        }

        if (menu) {
            Spacer(Modifier.width(IconSpacing))
            ChevronIcon(color = { contentColor })
        }
    }
}

/**
 * Ряд чипов: прокручивается вбок и никогда не переносится на вторую строку.
 * [contentPadding] — обычно боковые поля экрана (space-16), чтобы чипы уезжали под край.
 */
@Composable
fun GGFilterChipRow(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(horizontal = 16.dp),
    content: @Composable RowScope.() -> Unit,
) {
    Row(
        modifier = modifier
            .horizontalScroll(rememberScrollState())
            .padding(contentPadding),
        horizontalArrangement = Arrangement.spacedBy(ChipRowSpacing),
        verticalAlignment = Alignment.CenterVertically,
        content = content,
    )
}

/** AppIcons.check, перекрашиваемая в draw-фазе. */
@Composable
private fun CheckIcon(color: () -> Color) {
    val painter = rememberVectorPainter(AppIcons.check)
    Canvas(Modifier.size(IconSize)) {
        with(painter) {
            draw(size = size, colorFilter = ColorFilter.tint(color()))
        }
    }
}

@Composable
private fun ChevronIcon(color: () -> Color) {
    Canvas(Modifier.size(IconSize)) {
        val unit = size.width / 24f
        val path = Path().apply {
            moveTo(6f * unit, 9f * unit)
            lineTo(12f * unit, 15f * unit)
            lineTo(18f * unit, 9f * unit)
        }
        drawPath(
            path = path,
            color = color(),
            style = Stroke(width = 1.5f * unit, cap = StrokeCap.Round, join = StrokeJoin.Round),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun GGFilterChipPreview() = AppTheme {
    Column(
        modifier = Modifier.padding(vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        var pc by remember { mutableStateOf(true) }
        var coop by remember { mutableStateOf(false) }
        var owned by remember { mutableStateOf(false) }
        GGFilterChipRow {
            GGFilterChip(text = "PC", selected = pc, onClick = { pc = !pc })
            GGFilterChip(text = "Co-op", selected = coop, onClick = { coop = !coop }, count = "128")
            GGFilterChip(text = "Owned", selected = owned, onClick = { owned = !owned })
            GGFilterChip(text = "Genre", selected = false, onClick = {}, menu = true)
            GGFilterChip(text = "Year", selected = true, onClick = {}, menu = true, count = "2")
        }
        GGFilterChipRow {
            GGFilterChip(text = "PC", selected = false, onClick = {}, enabled = false)
            GGFilterChip(text = "Co-op", selected = true, onClick = {}, enabled = false)
        }
    }
}