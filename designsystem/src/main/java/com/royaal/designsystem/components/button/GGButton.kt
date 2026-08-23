package com.royaal.designsystem.components.button

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.isSpecified
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.royaal.designsystem.components.Text
import com.royaal.designsystem.theme.AppTheme
import com.royaal.designsystem.theme.containerColors
import com.royaal.designsystem.theme.iconColors
import com.royaal.designsystem.theme.outlineColors
import com.royaal.designsystem.util.clickable
import com.royaal.designsystem.util.textsource.TextSource
import com.royaal.designsystem.util.textsource.asTextSource

enum class GGButtonType {
    NORMAL,
    OUTLINED,
    ;
}

@Composable
fun GGButton(
    modifier: Modifier = Modifier,
    text: TextSource,
    @DrawableRes
    iconStart: Int? = null,
    iconStartTint: Color = Color.Unspecified,
    @DrawableRes
    iconEnd: Int? = null,
    iconEndTint: Color = Color.Unspecified,
    buttonType: GGButtonType = GGButtonType.NORMAL,
    onClick: () -> Unit,
) {
    val buttonContent = remember(text, iconStart, iconStartTint, iconEnd, iconEndTint) {
        @Composable {
            Row(
                modifier = Modifier.padding(
                    horizontal = 16.dp,
                    vertical = 8.dp,
                )
            ) {
                iconStart?.let { iconRes ->
                    Icon(
                        painter = painterResource(iconRes),
                        tint = iconStartTint.takeIf(Color::isSpecified)
                            ?: AppTheme.iconColors.iconPrimary,
                        contentDescription = null,
                    )
                }
                Text(
                    text = text,
                    style = AppTheme.typography.labelLarge,
                )
                iconEnd?.let { iconRes ->
                    Icon(
                        painter = painterResource(iconRes),
                        tint = iconEndTint.takeIf(Color::isSpecified)
                            ?: AppTheme.iconColors.iconPrimary,
                        contentDescription = null,
                    )
                }
            }
        }
    }
    when (buttonType) {
        GGButtonType.NORMAL -> NormalButton(modifier, buttonContent, onClick)
        GGButtonType.OUTLINED -> OutlinedButton(modifier, buttonContent, onClick)
    }
}

@Composable
private fun OutlinedButton(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
    onClick: () -> Unit,
) {
    Box(
        modifier = modifier
            .background(color = AppTheme.outlineColors.outlineBorder)
            .border(
                width = 1.dp,
                color = AppTheme.outlineColors.outline,
                shape = buttonShape,
            )
            .clip(buttonShape)
            .clickable(onClick),
    ) {
        content()
    }
}

@Composable
private fun NormalButton(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
    onClick: () -> Unit,
) {
    Box(
        modifier = modifier
            .background(color = AppTheme.containerColors.tertiaryContainer)
            .clip(shape = buttonShape)
            .clickable(onClick),
    ) {
        content()
    }
}

private val buttonShape = RoundedCornerShape(12.dp)

@Preview
@Composable
private fun Preview() {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        GGButtonType.entries.forEach {
            GGButton(
                text = it.name.asTextSource(),
                onClick = {},
                buttonType = it,
            )
        }
    }
}