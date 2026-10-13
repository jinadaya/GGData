package com.royaal.designsystem.util

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.ScrollableState
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onVisibilityChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withTimeoutOrNull

private val FadeWidth = 32.dp
private val NudgeDistance = 40.dp
private const val NudgeDelayMillis = 500L
private const val MinVisibleFraction = 0.8f

/**
 * Hints that a horizontal row scrolls: the edge with more content behind it fades out. With
 * [nudge], the row also nudges forward and back once, the first time it is on screen and can
 * scroll: pass `false` while it shows skeletons. Put it before `horizontalScroll`, or on the
 * LazyRow itself.
 */
@Composable
fun Modifier.horizontalScrollHint(
    state: ScrollableState,
    nudge: Boolean = false,
): Modifier {
    val start by animateFloatAsState(if (state.canScrollBackward) 1f else 0f)
    val end by animateFloatAsState(if (state.canScrollForward) 1f else 0f)

    var isNudged by rememberSaveable { mutableStateOf(false) }
    // A lazy list composes rows ahead of the screen: nudge only once the user can see it.
    var isOnScreen by remember { mutableStateOf(false) }
    val nudgePx = with(LocalDensity.current) { NudgeDistance.toPx() }
    LaunchedEffect(state, nudge) {
        if (!nudge || isNudged) return@LaunchedEffect
        val canNudge = snapshotFlow { isOnScreen && state.canScrollForward }
        // Waits until the row has stayed on screen for the whole delay.
        do {
            canNudge.first { it }
            val isLeft = withTimeoutOrNull(NudgeDelayMillis) { canNudge.first { !it } } != null
        } while (isLeft)
        isNudged = true
        // The user got there first: no need to show them.
        if (state.isScrollInProgress || state.canScrollBackward) return@LaunchedEffect
        state.animateScrollBy(nudgePx, tween(durationMillis = 350))
        state.animateScrollBy(-nudgePx, spring(dampingRatio = 0.6f, stiffness = 300f))
    }

    return this
        .conditional(nudge && !isNudged) {
            onVisibilityChanged(minFractionVisible = MinVisibleFraction) { isOnScreen = it }
        }
        .graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
        .drawWithContent {
            drawContent()
            val fade = FadeWidth.toPx().coerceAtMost(size.width / 2)
            val isLtr = layoutDirection == LayoutDirection.Ltr
            val left = if (isLtr) start else end
            val right = if (isLtr) end else start
            if (left > 0f) {
                drawRect(
                    brush = Brush.horizontalGradient(
                        0f to Color.Black.copy(alpha = left),
                        1f to Color.Transparent,
                        startX = 0f,
                        endX = fade,
                    ),
                    size = Size(fade, size.height),
                    blendMode = BlendMode.DstOut,
                )
            }
            if (right > 0f) {
                drawRect(
                    brush = Brush.horizontalGradient(
                        0f to Color.Transparent,
                        1f to Color.Black.copy(alpha = right),
                        startX = size.width - fade,
                        endX = size.width,
                    ),
                    topLeft = Offset(size.width - fade, 0f),
                    size = Size(fade, size.height),
                    blendMode = BlendMode.DstOut,
                )
            }
        }
}
