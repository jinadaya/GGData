package com.royaal.designsystem.components.skeleton

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawOutline
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.isSpecified
import com.royaal.designsystem.theme.AppTheme
import com.royaal.designsystem.theme.skeletonColors
import com.royaal.designsystem.util.BasePreview

private const val SHIMMER_DURATION_MILLIS = 1200
private const val TEXT_BAR_TO_FONT_SIZE = 0.75f
private const val DEFAULT_LINE_HEIGHT_TO_FONT_SIZE = 1.25f
private val DefaultFontSize = 14.dp

/** Shape of skeleton bars and blocks unless the content they stand in for has its own. */
val SkeletonShape: Shape = RoundedCornerShape(4.dp)

/**
 * Shimmer phase, `0..1`, looping. Remember one per skeleton group (a card, a list) and pass it to
 * every part, so that they all sweep in sync.
 */
@Composable
fun rememberSkeletonShimmer(): State<Float> =
    rememberInfiniteTransition(label = "SkeletonShimmer").animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(SHIMMER_DURATION_MILLIS, easing = LinearEasing)),
        label = "SkeletonShimmerPhase",
    )

/**
 * Paints a skeleton in [shape] behind the content: the base color with a highlight sweeping from
 * start to end. [shimmer] is read in the draw phase only, so the animation never recomposes.
 */
@Composable
fun Modifier.skeleton(
    shimmer: State<Float> = rememberSkeletonShimmer(),
    shape: Shape = SkeletonShape,
): Modifier {
    val colors = AppTheme.skeletonColors
    return drawBehind {
        // The highlight is as wide as the element and travels from fully before to fully after it.
        val bandWidth = size.width
        val startX = bandWidth * (2 * shimmer.value - 1)
        val brush = Brush.horizontalGradient(
            colors = listOf(colors.base, colors.highlight, colors.base),
            startX = startX,
            endX = startX + bandWidth,
        )
        drawOutline(shape.createOutline(size, layoutDirection, this), brush)
    }
}

/** A placeholder block: an image, a button, a tag. Size it with [modifier]. */
@Composable
fun SkeletonBox(
    modifier: Modifier = Modifier,
    shimmer: State<Float> = rememberSkeletonShimmer(),
    shape: Shape = SkeletonShape,
) {
    Box(modifier.skeleton(shimmer, shape))
}

/**
 * Placeholder for [lines] lines of [style] text. Each line is as tall as the real one, with a
 * glyph-high bar in it, so swapping the skeleton for the text doesn't shift the layout. Width
 * comes from [modifier]; the last of several lines is [lastLineWidthFraction] of it, like the end
 * of a paragraph.
 */
@Composable
fun SkeletonText(
    style: TextStyle,
    modifier: Modifier = Modifier,
    lines: Int = 1,
    lastLineWidthFraction: Float = 0.6f,
    shimmer: State<Float> = rememberSkeletonShimmer(),
) {
    val density = LocalDensity.current
    val (lineHeight, barHeight) = remember(style, density) {
        with(density) {
            val fontSize = if (style.fontSize.isSpecified) style.fontSize.toDp() else DefaultFontSize
            val lineHeight = when {
                style.lineHeight.isSp -> style.lineHeight.toDp()
                style.lineHeight.isEm -> fontSize * style.lineHeight.value
                else -> fontSize * DEFAULT_LINE_HEIGHT_TO_FONT_SIZE
            }
            lineHeight to fontSize * TEXT_BAR_TO_FONT_SIZE
        }
    }
    Column(modifier) {
        repeat(lines) { index ->
            val isLastOfSeveral = lines > 1 && index == lines - 1
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(lineHeight),
                contentAlignment = Alignment.CenterStart,
            ) {
                SkeletonBox(
                    modifier = Modifier
                        .fillMaxWidth(if (isLastOfSeveral) lastLineWidthFraction else 1f)
                        .height(barHeight),
                    shimmer = shimmer,
                )
            }
        }
    }
}

@Preview
@Composable
private fun Preview() = BasePreview {
    val shimmer = rememberSkeletonShimmer()
    SkeletonText(AppTheme.typography.headlineMedium, Modifier.fillMaxWidth(0.5f), shimmer = shimmer)
    SkeletonText(AppTheme.typography.bodyMedium, Modifier.fillMaxWidth(), lines = 3, shimmer = shimmer)
    SkeletonBox(Modifier.fillMaxWidth().height(80.dp), shimmer)
}
