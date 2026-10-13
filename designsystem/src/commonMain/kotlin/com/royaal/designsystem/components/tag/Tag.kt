package com.royaal.designsystem.components.tag

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.royaal.designsystem.components.skeleton.SkeletonBox
import com.royaal.designsystem.components.skeleton.rememberSkeletonShimmer
import com.royaal.designsystem.theme.AppTheme
import com.royaal.designsystem.theme.TagColors
import com.royaal.designsystem.theme.TagColorsVariant
import com.royaal.designsystem.theme.tagColors
import com.royaal.designsystem.util.horizontalScrollHint
import com.royaal.designsystem.util.pressScale

enum class GGTagType {
    NEUTRAL,
    OUTLINE,
    SOLID,
    ERROR,
    ;

    internal fun variant(colors: TagColors): TagColorsVariant = when (this) {
        NEUTRAL -> colors.neutral
        OUTLINE -> colors.outline
        SOLID -> colors.solid
        ERROR -> colors.error
    }
}

private val TagHeight = 28.dp
private val HorizontalPadding = 12.dp
private val RemoveEndPadding = 4.dp
private val RemoveSpacing = 4.dp
private val RemoveButtonSize = 20.dp
private val RemoveIconSize = 16.dp
private val CornerRadiusDp = 8.dp
private val BorderWidth = 1.dp
private val TagRowSpacing = 8.dp
private val TagRowDistance = 4.dp
private val SkeletonTagWidth = 64.dp

/**
 * Метка в одно-два слова: статус или категория рядом с элементом. Сам тег не нажимается.
 *
 * @param onRemove если задан, справа появляется кнопка ×.
 * @param removeLabel имя кнопки для экранных чтецов, например "Remove csv".
 */
@Composable
fun GGTag(
    text: String,
    modifier: Modifier = Modifier,
    type: GGTagType = GGTagType.NEUTRAL,
    onRemove: (() -> Unit)? = null,
    removeLabel: String = "Remove",
) {
    val variant = type.variant(AppTheme.tagColors)

    Row(
        modifier = modifier
            .height(TagHeight)
            .drawBehind {
                val radius = CornerRadiusDp.toPx()
                if (variant.container.alpha > 0f) {
                    drawRoundRect(color = variant.container, cornerRadius = CornerRadius(radius))
                }
                if (variant.border.alpha > 0f) {
                    val borderPx = BorderWidth.toPx()
                    drawRoundRect(
                        color = variant.border,
                        topLeft = Offset(borderPx / 2f, borderPx / 2f),
                        size = Size(size.width - borderPx, size.height - borderPx),
                        cornerRadius = CornerRadius((radius - borderPx / 2f).coerceAtLeast(0f)),
                        style = Stroke(width = borderPx),
                    )
                }
            }
            .padding(
                start = HorizontalPadding,
                end = if (onRemove != null) RemoveEndPadding else HorizontalPadding,
            ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = text,
            style = AppTheme.typography.bodySmall,
            color = variant.content,
            maxLines = 1,
            softWrap = false,
            overflow = TextOverflow.Ellipsis,
        )
        if (onRemove != null) {
            Spacer(Modifier.width(RemoveSpacing))
            RemoveButton(
                color = variant.content,
                label = removeLabel,
                onClick = onRemove,
            )
        }
    }
}

/** [GGTag] while its text is loading. Real tags vary in width, so pass a few different [width]s. */
@Composable
fun GGSkeletonTag(
    modifier: Modifier = Modifier,
    width: Dp = SkeletonTagWidth,
    shimmer: State<Float> = rememberSkeletonShimmer(),
) {
    SkeletonBox(
        modifier = modifier.size(width = width, height = TagHeight),
        shimmer = shimmer,
        shape = RoundedCornerShape(CornerRadiusDp),
    )
}

@Composable
fun GGTagRow(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues.Zero,
    content: @Composable RowScope.() -> Unit,
) {
    val scrollState = rememberScrollState()
    Row(
        modifier = modifier
            .horizontalScrollHint(scrollState)
            .horizontalScroll(scrollState)
            // Inside the scroll: tags scroll edge to edge, but rest aligned with the screen content.
            .padding(contentPadding),
        horizontalArrangement = Arrangement.spacedBy(TagRowSpacing),
        verticalAlignment = Alignment.CenterVertically,
        content = content,
    )
}

@Composable
fun GGTagFlowRow(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues.Zero,
    content: @Composable RowScope.() -> Unit,
) {
    FlowRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(TagRowSpacing),
        verticalArrangement = Arrangement.spacedBy(TagRowDistance),
        content = content,
    )
}

@Composable
private fun RemoveButton(
    color: Color,
    label: String,
    onClick: () -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    Box(
        modifier = Modifier
            .pressScale(interactionSource)
            .size(RemoveButtonSize)
            .clip(CircleShape)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                role = Role.Button,
                onClick = onClick,
            )
            .semantics { contentDescription = label },
        contentAlignment = Alignment.Center,
    ) {
        Canvas(Modifier.size(RemoveIconSize)) {
            val unit = size.width / 24f
            val stroke = 1.5f * unit
            drawLine(
                color = color,
                start = Offset(7f * unit, 7f * unit),
                end = Offset(17f * unit, 17f * unit),
                strokeWidth = stroke,
                cap = StrokeCap.Round,
            )
            drawLine(
                color = color,
                start = Offset(17f * unit, 7f * unit),
                end = Offset(7f * unit, 17f * unit),
                strokeWidth = stroke,
                cap = StrokeCap.Round,
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun GGTagPreview() = AppTheme {
    Column(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        GGTagRow {
            GGTag(text = "Draft")
            GGTag(text = "csv", type = GGTagType.OUTLINE)
            GGTag(text = "Selected", type = GGTagType.SOLID)
            GGTag(text = "Sync failed", type = GGTagType.ERROR)
        }
        GGTagRow {
            GGTag(text = "csv", type = GGTagType.OUTLINE, onRemove = {}, removeLabel = "Remove csv")
            GGTag(text = "PC", type = GGTagType.SOLID, onRemove = {}, removeLabel = "Remove PC")
        }
    }
}