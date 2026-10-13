package com.royaal.designsystem.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.royaal.designsystem.icons.AppIcons
import com.royaal.designsystem.theme.AppTheme
import com.royaal.designsystem.theme.bottomNavBarColors
import com.royaal.designsystem.theme.brandColors
import com.royaal.designsystem.theme.textColors
import com.royaal.designsystem.util.BasePreview
import com.royaal.designsystem.util.click
import com.royaal.designsystem.util.colored
import com.royaal.designsystem.util.conditional
import com.royaal.designsystem.util.pressScale

private val cornerRadius = 8.dp
private val bottomNavBarShape = RoundedCornerShape(cornerRadius.times(1.5f))

interface BottomBarEntry {
    val icon: ImageVector
    val title: String
    val key: Any
        get() = title
}

enum class BottomBarType {
    Tiles, Solid,
    ;
}

@Composable
fun GGBottomBar(
    selected: BottomBarEntry? = null,
    destinations: List<BottomBarEntry>,
    type: BottomBarType = BottomBarType.Solid,
    onDestinationClicked: (BottomBarEntry) -> Unit,
) {
    val colors = AppTheme.bottomNavBarColors
    require(destinations.isNotEmpty()) { "At least one destination entry should be provided." }
    val actualSelected = remember(selected) {
        selected ?: destinations.first()
    }
    val borderColor = AppTheme.brandColors.secondaryContainer
    val selectedIndex = destinations.indexOfFirst { it.key == actualSelected.key }
    // Fractional while sliding: 1.5f is halfway between the second and the third entry.
    val indicatorPosition = animateFloatAsState(
        targetValue = selectedIndex.toFloat(),
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "BottomBarIndicator",
    )
    Row(
        modifier = Modifier
            .conditional(type == BottomBarType.Solid) {
                dropShadow(
                    shape = bottomNavBarShape,
                ) {
                    color = colors.content
                    radius = cornerRadius.toPx()
                    alpha = 0.2f
                }.background(
                    color = colors.background,
                    shape = bottomNavBarShape,
                ).border(
                    width = 1.dp,
                    shape = bottomNavBarShape,
                    color = borderColor,
                )
            }
            .padding(4.dp)
            .fillMaxWidth()
            .conditional(type == BottomBarType.Solid && selectedIndex >= 0) {
                // One indicator for the whole bar, so it can slide between entries. Solid entries
                // share the width equally (weight 1f, no spacing), so an entry's index is its position.
                drawBehind {
                    val entryWidth = size.width / destinations.size
                    val start = entryWidth * indicatorPosition.value
                    val left = if (layoutDirection == LayoutDirection.Rtl) size.width - start - entryWidth else start
                    drawRoundRect(
                        color = colors.selectedBackground,
                        topLeft = Offset(left, 0f),
                        size = Size(entryWidth, size.height),
                        cornerRadius = CornerRadius(cornerRadius.toPx()),
                    )
                }
            },
        horizontalArrangement = when (type) {
            BottomBarType.Tiles -> Arrangement.spacedBy(16.dp)
            BottomBarType.Solid -> Arrangement.SpaceBetween
        }
    ) {
        for (dest in destinations) {
            val interactionSource = remember(dest.key) { MutableInteractionSource() }
            BottomBarEntry(
                dest = dest,
                selected = actualSelected.key == dest.key,
                drawSelectedBackground = type == BottomBarType.Tiles,
                modifier = Modifier
                    .pressScale(interactionSource)
                    .conditional(type == BottomBarType.Tiles) {
                        dropShadow(
                            shape = bottomNavBarShape,
                        ) {
                            color = colors.content
                            radius = cornerRadius.toPx()
                            alpha = 0.2f
                        }.background(
                            color = colors.background,
                            shape = bottomNavBarShape,
                        )
                    }
                    .weight(1f)
                    .click(interactionSource = interactionSource, pressScale = false) {
                        onDestinationClicked(dest)
                    },
            )
        }
    }
}

@Composable
private fun BottomBarEntry(
    dest: BottomBarEntry,
    selected: Boolean,
    modifier: Modifier = Modifier,
    drawSelectedBackground: Boolean = true,
) {
    val density = LocalDensity.current
    val colors = AppTheme.bottomNavBarColors
    val contentColor = animateColorAsState(
        if (selected) colors.selectedContent else colors.content
    )
    val containerColor = animateColorAsState(
        if (selected) colors.selectedBackground else colors.background
    )
    Column(
        modifier = modifier
            .drawBehind {
                if (selected && drawSelectedBackground) {
                    val radiusPx = with(density) { cornerRadius.toPx() }
                    drawRoundRect(
                        color = containerColor.value,
                        cornerRadius = CornerRadius(radiusPx)
                    )
                }
            }
            .padding(horizontal = 16.dp, vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            modifier = Modifier.colored {
                contentColor.value
            },
            imageVector = dest.icon,
            contentDescription = dest.title,
        )
        Text(
            modifier = Modifier.colored {
                contentColor.value
            },
            text = dest.title,
            style = AppTheme.typography.bodySmall,
            color = AppTheme.textColors.primary,
        )
    }
}

@Preview
@Composable
private fun Preview() = BasePreview {
    BottomBarEntry(
        object : BottomBarEntry {
            override val icon: ImageVector = AppIcons.favorite
            override val title: String = "Favourite"
        },
        selected = false,
    )
    BottomBarEntry(
        object : BottomBarEntry {
            override val icon: ImageVector = AppIcons.search
            override val title: String = "Search"
        },
        selected = true,
    )
    val selectedDest = remember {
        mutableStateOf<BottomBarEntry?>(null)
    }
    GGBottomBar(
        destinations = listOf(
            object : BottomBarEntry {
                override val icon: ImageVector = AppIcons.favorite
                override val title: String = "Favourite"
            },
            object : BottomBarEntry {
                override val icon: ImageVector = AppIcons.search
                override val title: String = "Search"
            },
            object : BottomBarEntry {
                override val icon: ImageVector = AppIcons.check
                override val title: String = "Check"
            }
        ),
        selected = selectedDest.value,
        type = BottomBarType.Solid,
        onDestinationClicked = {
            selectedDest.value = it
        }
    )
}