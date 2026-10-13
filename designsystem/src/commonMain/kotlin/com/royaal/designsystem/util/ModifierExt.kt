package com.royaal.designsystem.util

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.material3.SliderDefaults.drawStopIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.IntrinsicMeasurable
import androidx.compose.ui.layout.IntrinsicMeasureScope
import androidx.compose.ui.layout.LayoutModifier
import androidx.compose.ui.layout.Measurable
import androidx.compose.ui.layout.MeasureResult
import androidx.compose.ui.layout.MeasureScope
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.collectLatest

fun Modifier.conditional(
    condition: () -> Boolean,
    `else`: (Modifier.() -> Modifier)? = null,
    then: Modifier.() -> Modifier,
) = if (condition()) then() else this

fun Modifier.conditional(
    condition: Boolean,
    `else`: (Modifier.() -> Modifier)? = null,
    then: Modifier.() -> Modifier,
) = if (condition) then() else `else`?.invoke(this) ?: this

/**
 * Click without a ripple. While pressed, the element shrinks a little, see [pressScale]: only
 * what comes after this modifier in the chain, so pass `pressScale = false` when calling it after
 * the background, and put [pressScale] at the start of the chain instead.
 */
@Composable
fun Modifier.click(
    enabled: Boolean = true,
    role: Role? = null,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    pressScale: Boolean = true,
    onClick: () -> Unit,
) = (if (pressScale) pressScale(interactionSource, enabled) else this)
    .clickable(
        interactionSource = interactionSource,
        indication = null,
        onClick = onClick,
        enabled = enabled,
        role = role,
    )

fun Modifier.colored(
    color: () -> Color,
) = this
    .graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
    .drawWithContent {
        drawContent()
        drawRect(
            color = color(),
            blendMode = BlendMode.SrcIn,
        )
    }

fun Modifier.ignoreIntrinsicWidth() = this then object : LayoutModifier {
    override fun MeasureScope.measure(
        measurable: Measurable,
        constraints: Constraints,
    ): MeasureResult {
        val placeable = measurable.measure(constraints)
        return layout(placeable.width, placeable.height) { placeable.place(0, 0) }
    }
    override fun IntrinsicMeasureScope.minIntrinsicWidth(measurable: IntrinsicMeasurable, height: Int) = 0
    override fun IntrinsicMeasureScope.maxIntrinsicWidth(measurable: IntrinsicMeasurable, height: Int) = 0
}

fun Modifier.ignoreParentHorizontalPadding(padding: Dp): Modifier = this.layout { measurable, constraints ->
    val paddingPx = padding.roundToPx()
    val expandedConstraints = constraints.copy(
        maxWidth = constraints.maxWidth + (paddingPx * 2)
    )

    val placeable = measurable.measure(expandedConstraints)

    // Position the element slightly to the left (-paddingPx) to align with screen edge
    layout(placeable.width, placeable.height) {
        placeable.place(-paddingPx, 0)
    }
}

/**
 * Тень карточки. С [click] карточка нажимается: тень пропадает, а сама она чуть уменьшается
 * ([pressScale]). С [pressInteractionSource] только тень пропадает на нажатиях, которые
 * обрабатывает кто-то другой (например, combinedClickable с долгим нажатием).
 */
@Composable
fun Modifier.baseShadow(
    elevation: () -> Dp = { AppConst.baseShadowElevationDp },
    shadowShape: () -> Shape = { AppConst.cardShape },
    click: (() -> Unit)? = null,
    pressInteractionSource: MutableInteractionSource? = null,
): Modifier {
    if (click == null && pressInteractionSource == null) {
        return this.graphicsLayer {
            shadowElevation = elevation().toPx()
            shape = shadowShape()
        }
    }

    // Анимируем долю от elevation (1f — в покое, 0f — нажато), чтобы и анимация,
    // и elevation() читались только в draw-фазе graphicsLayer{} без рекомпозиции.
    val elevationFraction = remember { Animatable(1f) }
    val interactionSource = pressInteractionSource ?: remember { MutableInteractionSource() }

    LaunchedEffect(interactionSource) {
        interactionSource.interactions.collectLatest { interaction ->
            when (interaction) {
                is PressInteraction.Press ->
                    elevationFraction.animateTo(0f, tween(durationMillis = 300))

                is PressInteraction.Release, is PressInteraction.Cancel ->
                    elevationFraction.animateTo(1f, tween(durationMillis = 350))

                else -> Unit
            }
        }
    }

    val shadow = Modifier.graphicsLayer {
        shadowElevation = elevation().toPx() * elevationFraction.value
        shape = shadowShape()
    }
    if (click == null) return this.then(shadow)
    // Масштаб до тени: уменьшается вся карточка, а не только то, что идёт после click.
    return this
        .pressScale(interactionSource)
        .then(shadow)
        .click(interactionSource = interactionSource, pressScale = false, onClick = click)
}