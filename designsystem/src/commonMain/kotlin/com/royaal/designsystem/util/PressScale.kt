package com.royaal.designsystem.util

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import kotlinx.coroutines.flow.collectLatest

/** Pressed elements shrink by 2%, as if pushed back into the background. */
const val PRESSED_SCALE = 0.98f

private const val PRESS_DURATION_MILLIS = 100
private const val RELEASE_DURATION_MILLIS = 200

/**
 * Shrinks the element to [PRESSED_SCALE] while [interactionSource] is pressed. Scales what comes
 * after it in the chain, so put it before the background and the border.
 *
 * A quick tap still shows the whole press: on release the element first reaches the pressed
 * scale, then comes back.
 */
@Composable
fun Modifier.pressScale(
    interactionSource: InteractionSource,
    enabled: Boolean = true,
): Modifier {
    if (!enabled) return this
    val scale = remember { Animatable(1f) }
    LaunchedEffect(interactionSource) {
        interactionSource.interactions.collectLatest { interaction ->
            when (interaction) {
                is PressInteraction.Press -> scale.animateTo(PRESSED_SCALE, tween(PRESS_DURATION_MILLIS))
                is PressInteraction.Release, is PressInteraction.Cancel -> {
                    scale.animateTo(PRESSED_SCALE, tween(PRESS_DURATION_MILLIS))
                    scale.animateTo(1f, tween(RELEASE_DURATION_MILLIS))
                }
            }
        }
    }
    // Read in the layer block only: animating doesn't recompose the element.
    return graphicsLayer {
        scaleX = scale.value
        scaleY = scale.value
    }
}
