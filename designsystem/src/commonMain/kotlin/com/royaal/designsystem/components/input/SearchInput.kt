package com.royaal.designsystem.components.input

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.clearText
import androidx.compose.foundation.text.input.TextFieldDecorator
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.error
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.royaal.designsystem.icons.AppIcons
import com.royaal.designsystem.theme.AppTheme
import com.royaal.designsystem.theme.inputColors
import com.royaal.designsystem.theme.textColors
import com.royaal.designsystem.util.BasePreview
import com.royaal.designsystem.util.click
import com.royaal.designsystem.util.colored
import kotlinx.coroutines.flow.collectLatest

private val baseInputCornerRadius = 12.dp
private val baseInputBorderWidth = 1.dp
private val baseInputContentPadding = 16.dp
private val baseInputSpacing = 4.dp

private val colorAnimationSpec: AnimationSpec<Color> =
    tween(durationMillis = 150, easing = FastOutSlowInEasing)

/**
 * @param onSearchClicked the keyboard's Search key; after it the keyboard hides.
 * @param onClearClicked the × button, after it has cleared the text.
 */
@Composable
fun GGSearchInput(
    initialValue: String,
    onSearchClicked: () -> Unit,
    loading: Boolean = false,
    title: String? = null,
    subtitle: String? = null,
    hint: String? = null,
    error: String? = null,
    enabled: Boolean = true,
    modifier: Modifier = Modifier,
    onClearClicked: (() -> Unit)? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
    onValueChanged: (String) -> Unit,
) {
    val colors = AppTheme.inputColors
    val isError = error != null

    val containerColor = animateColorAsState(
        targetValue = if (enabled) {
            colors.container.transparentToward(colors.disabledContainer)
        } else {
            colors.disabledContainer.transparentToward(colors.container)
        },
        animationSpec = colorAnimationSpec,
        label = "InputContainer",
    )
    val borderColor = animateColorAsState(
        targetValue = when {
            !enabled -> colors.disabledBorder
            isError -> colors.errorBorder
            else -> colors.border
        }.transparentToward(colors.border),
        animationSpec = colorAnimationSpec,
        label = "InputBorder",
    )
    val textColor = animateColorAsState(
        targetValue = if (enabled) colors.content else colors.disabledContent,
        animationSpec = colorAnimationSpec,
        label = "InputText",
    )
    val underlyingTextColor = animateColorAsState(
        targetValue = when {
            !enabled -> colors.disabledContent
            isError -> colors.errorContent
            else -> colors.subContent
        },
        animationSpec = colorAnimationSpec,
        label = "InputUnderlyingText",
    )

    val state = rememberTextFieldState(initialValue)
    val currentOnValueChanged = rememberUpdatedState(onValueChanged)
    LaunchedEffect(state) {
        snapshotFlow { state.text.toString() }.collectLatest { currentOnValueChanged.value(it) }
    }
    val underlyingText = error ?: subtitle
    val textStyle = AppTheme.typography.bodyMedium

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(baseInputSpacing),
    ) {
        title?.let { title ->
            Text(
                modifier = Modifier.colored { textColor.value },
                text = title,
                style = AppTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.Bold,
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        BasicTextField(
            state = state,
            enabled = enabled,
            textStyle = textStyle.copy(color = textColor.value),
            cursorBrush = SolidColor(textColor.value),
            // Single line: in a multi-line field Enter inserts a line break instead of the action.
            lineLimits = TextFieldLineLimits.SingleLine,
            keyboardOptions = keyboardOptions,
            onKeyboardAction = rememberImeActionHandler(keyboardOptions.imeAction, onSearchClicked),
            modifier = Modifier
                .fillMaxWidth()
                .semantics { if (error != null) error(error) },
            decorator = TextFieldDecorator { innerTextField ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .drawBehind {
                            val radius = baseInputCornerRadius.toPx()
                            drawRoundRect(
                                color = containerColor.value,
                                cornerRadius = CornerRadius(radius),
                            )
                            val borderPx = baseInputBorderWidth.toPx()
                            val innerRadius = (radius - borderPx / 2f).coerceAtLeast(0f)
                            drawRoundRect(
                                color = borderColor.value,
                                topLeft = Offset(borderPx / 2f, borderPx / 2f),
                                size = Size(size.width - borderPx, size.height - borderPx),
                                cornerRadius = CornerRadius(innerRadius),
                                style = Stroke(width = borderPx),
                            )
                        }
                        .padding(baseInputContentPadding),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        modifier = Modifier.size(24.dp),
                        imageVector = AppIcons.search,
                        tint = colors.content,
                        contentDescription = null,
                    )
                    // The hint lies under the field, not before it, so the cursor starts where the text will.
                    Box(
                        modifier = Modifier.weight(1f),
                        contentAlignment = Alignment.CenterStart,
                    ) {
                        if (derivedStateOf { state.text.isEmpty() }.value && hint != null) {
                            Text(
                                text = hint,
                                style = textStyle,
                                color = AppTheme.textColors.secondary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                        innerTextField()
                    }
                    if (loading) {
                        CircularProgressIndicator(
                            modifier = Modifier
                                .align(Alignment.CenterVertically)
                                .size(24.dp),
                            color = colors.content,
                            strokeWidth = 2.dp,
                            strokeCap = StrokeCap.Round,
                        )
                    } else if (state.text.isNotEmpty()) {
                        Icon(
                            imageVector = AppIcons.close,
                            modifier = Modifier
                                .size(24.dp)
                                .click {
                                    state.clearText()
                                    onClearClicked?.invoke()
                                },
                            contentDescription = null,
                        )
                    }
                }
            },
        )
        underlyingText?.let { underlyingText ->
            Text(
                modifier = Modifier.colored { underlyingTextColor.value },
                text = underlyingText,
                style = AppTheme.typography.labelMedium,
            )
        }
    }
}

private fun Color.transparentToward(other: Color): Color =
    if (alpha == 0f) other.copy(alpha = 0f) else this

@Preview
@Composable
private fun Preview() = BasePreview {
    GGSearchInput("", hint = "enter text", onSearchClicked = {}) {}
    GGSearchInput("initial", title = "Input", onSearchClicked = {}) {}
    GGSearchInput("initial", error = "Some error", onSearchClicked = {}) {}
    GGSearchInput(
        initialValue = "initial",
        title = "disabled",
        subtitle = "Some subtitle",
        enabled = false,
        loading = true,
        onSearchClicked = {}
    ) {}
}