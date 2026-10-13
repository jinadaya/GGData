package com.royaal.designsystem.components.progressBar

import androidx.compose.animation.core.EaseInOut
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.progressSemantics
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.royaal.designsystem.theme.AppTheme
import com.royaal.designsystem.theme.ProgressBarColors
import com.royaal.designsystem.theme.progressBarColors
import kotlin.math.roundToInt

private val TrackHeight = 4.dp
private val HeaderSpacing = 8.dp
private val HeaderGap = 8.dp

private const val IndeterminateDurationMs = 1600
private const val IndeterminateFillFraction = 1f / 3f

/**
 * Определённый прогресс: сколько задачи уже сделано.
 *
 * @param progress от 0f до 1f. Лямбда читается только при отрисовке, поэтому частые
 * обновления прогресса не вызывают рекомпозицию; процент перекомпонуется
 * только когда меняется целое значение.
 * @param label что происходит, причастием: "Importing rows". При ошибке — что сломалось.
 * @param showValue показывать процент справа над полосой.
 * @param isError задача упала: заливка и процент становятся error.
 */
@Composable
fun GGProgressBar(
    progress: () -> Float,
    modifier: Modifier = Modifier,
    label: String? = null,
    showValue: Boolean = true,
    isError: Boolean = false,
) {
    val colors = AppTheme.progressBarColors
    val percent by remember(progress) {
        derivedStateOf { (progress().coerceIn(0f, 1f) * 100).roundToInt() }
    }

    ProgressBarLayout(
        modifier = modifier,
        label = label,
        value = if (showValue) "$percent%" else null,
        isError = isError,
        colors = colors,
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(TrackHeight)
                .semantics {
                    progressBarRangeInfo = ProgressBarRangeInfo(
                        current = progress().coerceIn(0f, 1f),
                        range = 0f..1f,
                    )
                }
                .clip(CircleShape)
                .drawBehind {
                    drawTrack(colors)
                    val width = size.width * progress().coerceIn(0f, 1f)
                    if (width > 0f) {
                        drawFill(
                            colors = colors,
                            isError = isError,
                            startX = 0f,
                            width = width,
                        )
                    }
                },
        )
    }
}

/**
 * Неопределённый прогресс: длительность неизвестна.
 */
@Composable
fun GGProgressBar(
    modifier: Modifier = Modifier,
    label: String? = null,
    isError: Boolean = false,
) {
    val colors = AppTheme.progressBarColors
    val transition = rememberInfiniteTransition(label = "ProgressBarIndeterminate")
    val position by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(IndeterminateDurationMs, easing = EaseInOut),
            repeatMode = RepeatMode.Restart,
        ),
        label = "ProgressBarIndeterminatePosition",
    )

    ProgressBarLayout(
        modifier = modifier,
        label = label,
        value = null,
        isError = isError,
        colors = colors,
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(TrackHeight)
                .progressSemantics()
                .clip(CircleShape)
                .drawBehind {
                    drawTrack(colors)
                    // Заливка въезжает слева из-за края и уезжает за правый край.
                    val fillWidth = size.width * IndeterminateFillFraction
                    val startX = -fillWidth + (size.width + fillWidth) * position
                    drawFill(
                        colors = colors,
                        isError = isError,
                        startX = startX,
                        width = fillWidth,
                    )
                },
        )
    }
}

@Composable
private fun ProgressBarLayout(
    modifier: Modifier,
    label: String?,
    value: String?,
    isError: Boolean,
    colors: ProgressBarColors,
    bar: @Composable () -> Unit,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(HeaderSpacing),
    ) {
        if (label != null || value != null) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(HeaderGap),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = label.orEmpty(),
                    style = AppTheme.typography.bodySmall,
                    color = colors.label,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )
                if (value != null) {
                    Text(
                        text = value,
                        style = AppTheme.typography.bodySmall,
                        color = if (isError) colors.errorValue else colors.value,
                        maxLines = 1,
                    )
                }
            }
        }
        bar()
    }
}

private fun DrawScope.drawTrack(colors: ProgressBarColors) {
    drawRect(color = colors.track)
}

private fun DrawScope.drawFill(
    colors: ProgressBarColors,
    isError: Boolean,
    startX: Float,
    width: Float,
) {
    val x = if (layoutDirection == LayoutDirection.Rtl) size.width - startX - width else startX
    drawRoundRect(
        color = if (isError) colors.errorFill else colors.fill,
        topLeft = Offset(x, 0f),
        size = Size(width, size.height),
        cornerRadius = CornerRadius(size.height / 2f),
    )
}

@Preview(showBackground = true)
@Composable
private fun GGProgressBarPreview() = AppTheme {
    Column(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        var progress by remember { mutableFloatStateOf(0.42f) }
        GGProgressBar(progress = { progress }, label = "Importing rows")
        GGProgressBar(progress = { 0.7f }, label = "Couldn't import rows", isError = true)
        GGProgressBar(label = "Syncing library")
        GGProgressBar(progress = { 0.25f })
    }
}