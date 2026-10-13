package com.royaal.designsystem.components.button

import androidx.compose.animation.Animatable
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.royaal.designsystem.icons.AppIcons
import com.royaal.designsystem.theme.AppTheme
import com.royaal.designsystem.theme.ButtonVariantColors
import com.royaal.designsystem.theme.buttonColors
import com.royaal.designsystem.util.AppConst
import com.royaal.designsystem.util.conditional
import com.royaal.designsystem.util.pressScale
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource

enum class GGIconButtonType {
    FLATTENED,
    OUTLINED,
    NORMAL,
    ;
    @Composable
    internal fun getColorScheme(selected: Boolean): ButtonVariantColors =
        if (selected) AppTheme.buttonColors.filled else when (this) {
            FLATTENED -> AppTheme.buttonColors.flattened
            NORMAL -> AppTheme.buttonColors.filled
            OUTLINED -> AppTheme.buttonColors.outlined
        }
}

private val GGIconButtonType.restElevation: Dp
    get() = when (this) {
        GGIconButtonType.FLATTENED -> 0.dp
        GGIconButtonType.OUTLINED -> 4.dp
        GGIconButtonType.NORMAL -> 6.dp
    }

private val iconButtonShape = AppConst.buttonShape

@Composable
fun GGIconButton(
    imageVector: ImageVector,
    type: GGIconButtonType = GGIconButtonType.NORMAL,
    enabled: Boolean = true,
    selected: Boolean = false,
    onClick: () -> Unit,
) {
    val colorScheme = type.getColorScheme(selected)
    val contentColor = if (enabled) colorScheme.content else colorScheme.disabledContent
    GGIconButton(
        type, enabled, selected, onClick
    ) {
        Icon(
            modifier = Modifier.size(24.dp),
            imageVector = imageVector,
            tint = contentColor,
            contentDescription = null,
        )
    }
}

@Composable
fun GGIconButton(
    iconRes: DrawableResource,
    type: GGIconButtonType = GGIconButtonType.NORMAL,
    enabled: Boolean = true,
    selected: Boolean = false,
    onClick: () -> Unit,
) {
    val colorScheme = type.getColorScheme(selected)
    val contentColor = if (enabled) colorScheme.content else colorScheme.disabledContent
    GGIconButton(
        type, enabled, selected, onClick
    ) {
        Icon(
            modifier = Modifier.size(24.dp),
            painter = painterResource(iconRes),
            tint = contentColor,
            contentDescription = null,
        )
    }
}

@Composable
private fun GGIconButton(
    type: GGIconButtonType = GGIconButtonType.NORMAL,
    enabled: Boolean = true,
    selected: Boolean = false,
    onClick: () -> Unit,
    iconContent: @Composable BoxScope.() -> Unit,
) {
    val colorScheme = type.getColorScheme(selected)
    val containerColor = if (enabled) colorScheme.container else colorScheme.disabledContainer
    val borderColor = if (enabled) colorScheme.border else colorScheme.disabledContent
    val animatedContainerColor = remember(type, selected) {
        Animatable(colorScheme.pressedContainer.copy(0f))
    }

    val density = LocalDensity.current
    val cornerRadiusPx = with(density) { AppConst.buttonCornerRadius.toPx() }
    val restElevation = if (enabled) type.restElevation else 0.dp
    val elevation = remember(type, enabled) {
        Animatable(with(density) { restElevation.toPx() })
    }
    val interactionSource = remember { MutableInteractionSource() }
    LaunchedEffect(interactionSource, type, enabled, selected) {
        val restPx = with(density) { restElevation.toPx() }
        val pressedPx = 0f
        interactionSource.interactions.collectLatest { interaction ->
            when (interaction) {
                is PressInteraction.Press -> {
                    launch { elevation.animateTo(pressedPx) }
                    launch { animatedContainerColor.animateTo(colorScheme.pressedContainer, tween(300)) }
                }

                is PressInteraction.Release,
                is PressInteraction.Cancel -> {
                    launch { elevation.animateTo(restPx) }
                    launch { animatedContainerColor.animateTo(colorScheme.pressedContainer.copy(0f), tween(300)) }
                }
            }
        }
    }

    Box(
        modifier = Modifier
            .pressScale(interactionSource, enabled)
            .sizeIn(minWidth = AppConst.buttonHeight, minHeight = AppConst.buttonHeight)
            .graphicsLayer {
                shadowElevation = elevation.value
                shape = iconButtonShape
                clip = false
            }
            .background(color = containerColor, shape = iconButtonShape)
            .clip(iconButtonShape)
            .conditional(
                condition = type != GGIconButtonType.FLATTENED,
                `else` = {
                    drawWithContent {
                        drawRoundRect(
                            color = animatedContainerColor.value,
                            cornerRadius = CornerRadius(cornerRadiusPx)
                        )
                        drawContent()
                    }
                }
            ) {
                border(color = borderColor, shape = iconButtonShape, width = 1.dp)
            }
            .clickable(
                onClick = onClick,
                interactionSource = interactionSource,
                enabled = enabled,
                indication = null,
                role = Role.Button,
            ),
        contentAlignment = Alignment.Center,
        content = iconContent,
    )
}

@Preview(showBackground = true)
@Composable
private fun Preview() = AppTheme {
    Row(Modifier.padding(12.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        for (type in GGIconButtonType.entries) {
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                GGIconButton(
                    imageVector = AppIcons.favorite,
                    type = type,
                    onClick = {}
                )
                GGIconButton(
                    imageVector = AppIcons.favorite,
                    type = type,
                    onClick = {},
                    selected = true,
                )
                GGIconButton(
                    imageVector = AppIcons.favorite,
                    type = type,
                    onClick = {},
                    enabled = false,
                )
            }
        }
    }
}
