package com.royaal.designsystem.components.button

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.isSpecified
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.royaal.designsystem.theme.AppTheme
import com.royaal.designsystem.theme.buttonColors
import com.royaal.designsystem.util.AppConst
import com.royaal.designsystem.util.click
import com.royaal.designsystem.util.pressScale
import kotlinx.coroutines.flow.collectLatest
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource

enum class GGButtonType {
    NORMAL,
    OUTLINED,
    ;
}

private val GGButtonType.restElevation: Dp
    get() = when (this) {
        GGButtonType.NORMAL -> AppConst.highShadowElevationDp
        GGButtonType.OUTLINED -> AppConst.midShadowElevationDp
    }

private val buttonShape = AppConst.buttonShape

@Composable
fun GGButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    buttonType: GGButtonType = GGButtonType.NORMAL,
    enabled: Boolean = true,
    textStyle: TextStyle = AppTheme.typography.labelLarge,
    textColor: Color = Color.Unspecified,
    iconStart: ImageVector? = null,
    iconStartTint: Color = Color.Unspecified,
    iconEnd: ImageVector? = null,
    iconEndTint: Color = Color.Unspecified,
) {
    val variant = when (buttonType) {
        GGButtonType.NORMAL -> AppTheme.buttonColors.filled
        GGButtonType.OUTLINED -> AppTheme.buttonColors.outlined
    }
    val containerColor = if (enabled) variant.container else variant.disabledContainer
    val contentColor = if (enabled) variant.content else variant.disabledContent
    val borderColor = if (enabled) variant.border else variant.disabledContent

    val density = LocalDensity.current
    // Анимация прижата к px и читается только внутри graphicsLayer{}-лямбды:
    // этот блок выполняется в draw-фазе и подписывается на стейт сам, минуя
    // рекомпозицию GGButton на каждом кадре анимации.
    val restElevation = if (enabled) buttonType.restElevation else AppConst.noShadowElevationDp
    val elevationPx = remember(buttonType, enabled) {
        Animatable(with(density) { restElevation.toPx() })
    }
    val interactionSource = remember { MutableInteractionSource() }

    LaunchedEffect(interactionSource, buttonType, enabled) {
        val restPx = with(density) { restElevation.toPx() }
        val pressedPx = 0f
        interactionSource.interactions.collectLatest { interaction ->
            when (interaction) {
                is PressInteraction.Press ->
                    elevationPx.animateTo(pressedPx, tween(durationMillis = 300))

                is PressInteraction.Release, is PressInteraction.Cancel ->
                    elevationPx.animateTo(restPx, tween(durationMillis = 350))

                else -> Unit
            }
        }
    }

    Box(
        modifier = modifier
            .pressScale(interactionSource, enabled)
            .heightIn(min = AppConst.buttonHeight)
            .graphicsLayer {
                shadowElevation = elevationPx.value
                shape = buttonShape
                clip = false
            }
            .background(color = containerColor, shape = buttonShape)
            .clip(buttonShape)
            .border(width = 1.dp, color = borderColor, shape = buttonShape)
            .click(onClick = onClick, enabled = enabled, interactionSource = interactionSource, pressScale = false),
        contentAlignment = Alignment.Center,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            iconStart?.let { imgVector ->
                Icon(
                    imageVector = imgVector,
                    tint = iconStartTint.takeIf(Color::isSpecified) ?: contentColor,
                    contentDescription = null,
                )
            }
            Text(
                text = text,
                color = textColor.takeIf(Color::isSpecified) ?: contentColor,
                style = textStyle,
            )
            iconEnd?.let { imgVector ->
                Icon(
                    imageVector = imgVector,
                    tint = iconEndTint.takeIf(Color::isSpecified) ?: contentColor,
                    contentDescription = null,
                )
            }
        }
    }
}

@Preview
@Composable
private fun GGButtonPreview() {
    AppTheme {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            GGButtonType.entries.forEach {
                GGButton(
                    text = it.name,
                    onClick = {},
                    buttonType = it,
                )
                GGButton(
                    text = it.name,
                    onClick = {},
                    buttonType = it,
                    enabled = false,
                )
            }
        }
    }
}
