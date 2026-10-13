package com.royaal.designsystem.components.toggle

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.toggleable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.royaal.designsystem.theme.AppTheme
import com.royaal.designsystem.theme.SwitchColorsVariant
import com.royaal.designsystem.theme.switchColors
import com.royaal.designsystem.util.BasePreview
import com.royaal.designsystem.util.pressScale

private val SwitchWidth = 44.dp
private val SwitchHeight = 24.dp
private val ThumbSize = 18.dp
private val BorderWidth = 1.dp

/** Путь центра кружка: от height/2 до width - height/2. */
private val ThumbTravel = SwitchWidth - SwitchHeight

/** Быстрый «флик» решает направление независимо от того, где отпустили палец. */
private val FlingVelocityThreshold = 400.dp // в dp/с

private const val AnimationDurationMs = 200
private val colorAnimationSpec: AnimationSpec<Color> =
    tween(AnimationDurationMs, easing = FastOutSlowInEasing)
private val thumbAnimationSpec: AnimationSpec<Float> =
    tween(AnimationDurationMs, easing = FastOutSlowInEasing)

@Composable
fun GGSwitch(
    checked: Boolean,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    onCheckedChange: ((Boolean) -> Unit)?,
) {
    val colors = AppTheme.switchColors
    val variant = if (enabled) colors.enabled else colors.disabled

    val containerColor = animateColorAsState(
        targetValue = variant.containerColor(checked),
        animationSpec = colorAnimationSpec,
        label = "SwitchContainer",
    )
    val borderColor = animateColorAsState(
        targetValue = variant.borderColor(checked),
        animationSpec = colorAnimationSpec,
        label = "SwitchBorder",
    )
    val thumbColor = animateColorAsState(
        targetValue = variant.thumbColor(checked),
        animationSpec = colorAnimationSpec,
        label = "SwitchThumb",
    )

    val thumbAnimatable = remember { Animatable(if (checked) 1f else 0f) }
    var isDragging by remember { mutableStateOf(false) }
    var dragFraction by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(checked, isDragging) {
        if (!isDragging) {
            thumbAnimatable.animateTo(if (checked) 1f else 0f, thumbAnimationSpec)
        }
    }

    val density = LocalDensity.current
    val travelPx = with(density) { ThumbTravel.toPx() }
    val flingThresholdPx = with(density) { FlingVelocityThreshold.toPx() }
    val isRtl = LocalLayoutDirection.current == LayoutDirection.Rtl

    val currentChecked by rememberUpdatedState(checked)
    val currentOnCheckedChange by rememberUpdatedState(onCheckedChange)

    val draggableState = rememberDraggableState { delta ->
        dragFraction = (dragFraction + delta / travelPx).coerceIn(0f, 1f)
    }

    val interactive = enabled && onCheckedChange != null

    val gestureModifier = if (onCheckedChange != null) {
        Modifier
            .draggable(
                state = draggableState,
                orientation = Orientation.Horizontal,
                enabled = interactive,
                interactionSource = interactionSource,
                reverseDirection = isRtl,
                onDragStarted = {
                    thumbAnimatable.stop()
                    dragFraction = thumbAnimatable.value
                    isDragging = true
                },
                onDragStopped = { velocity ->
                    val newChecked = when {
                        velocity > flingThresholdPx -> true
                        velocity < -flingThresholdPx -> false
                        else -> dragFraction >= 0.5f
                    }
                    // Продолжаем анимацию с того места, где отпустили палец.
                    thumbAnimatable.snapTo(dragFraction)
                    if (newChecked != currentChecked) {
                        currentOnCheckedChange?.invoke(newChecked)
                    }
                    isDragging = false
                },
            )
            .toggleable(
                value = checked,
                onValueChange = onCheckedChange,
                enabled = enabled,
                role = Role.Switch,
                interactionSource = interactionSource,
                indication = null,
            )
    } else {
        Modifier
    }

    // Все анимированные значения читаются только внутри drawBehind,
    // поэтому во время анимации и перетаскивания идёт перерисовка без рекомпозиции.
    Box(
        modifier = modifier
            .pressScale(interactionSource, interactive)
            .then(gestureModifier)
            .size(SwitchWidth, SwitchHeight)
            .drawBehind {
                val thumbFraction = if (isDragging) dragFraction else thumbAnimatable.value

                val trackRadius = CornerRadius(size.height / 2f)
                drawRoundRect(color = containerColor.value, cornerRadius = trackRadius)

                val borderPx = BorderWidth.toPx()
                drawRoundRect(
                    color = borderColor.value,
                    topLeft = Offset(borderPx / 2f, borderPx / 2f),
                    size = Size(size.width - borderPx, size.height - borderPx),
                    cornerRadius = CornerRadius((size.height - borderPx) / 2f),
                    style = Stroke(width = borderPx),
                )

                val thumbRadius = ThumbSize.toPx() / 2f
                val inset = size.height / 2f - thumbRadius
                val startX = inset + thumbRadius
                val endX = size.width - inset - thumbRadius
                val ltrX = startX + (endX - startX) * thumbFraction
                val x = if (layoutDirection == LayoutDirection.Rtl) size.width - ltrX else ltrX
                val radiusFraction = thumbFraction.coerceAtLeast(0.7f)

                drawCircle(
                    color = thumbColor.value,
                    radius = thumbRadius * radiusFraction,
                    center = Offset(x, size.height / 2f),
                )
            },
    )
}

private fun SwitchColorsVariant.containerColor(checked: Boolean): Color =
    if (checked) checkedContainer.transparentToward(uncheckedContainer)
    else uncheckedContainer.transparentToward(checkedContainer)

private fun SwitchColorsVariant.borderColor(checked: Boolean): Color =
    if (checked) checkedBorder.transparentToward(uncheckedBorder)
    else uncheckedBorder.transparentToward(checkedBorder)

private fun SwitchColorsVariant.thumbColor(checked: Boolean): Color =
    if (checked) checkedThumb.transparentToward(uncheckedThumb)
    else uncheckedThumb.transparentToward(checkedThumb)

/**
 * Color.Transparent — это чёрный с alpha = 0, и анимация от него к светлому цвету
 * проходит через серый. Берём RGB у второго цвета, чтобы менялась только прозрачность.
 */
private fun Color.transparentToward(other: Color): Color =
    if (alpha == 0f) other.copy(alpha = 0f) else this

@Preview(showBackground = true)
@Composable
private fun GGSwitchPreview() = BasePreview {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        var checked by remember { mutableStateOf(false) }
        GGSwitch(checked = checked, onCheckedChange = { checked = it })
        GGSwitch(checked = true, onCheckedChange = {})
    }
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        GGSwitch(checked = false, onCheckedChange = {}, enabled = false)
        GGSwitch(checked = true, onCheckedChange = {}, enabled = false)
    }
}