package com.royaal.designsystem.components.snackbar

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.SnackbarData
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.SnackbarVisuals
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.royaal.designsystem.theme.AppTheme
import com.royaal.designsystem.theme.snackbarColors
import com.royaal.designsystem.util.AppConst
import com.royaal.designsystem.util.BasePreview
import com.royaal.designsystem.util.pressScale

private val SnackbarMargin = 16.dp
private val SnackbarMaxWidth = 560.dp
private val SnackbarMinHeight = 48.dp
private val SnackbarCornerRadius = 12.dp
private val SnackbarElevation = 6.dp
private val SnackbarHorizontalPadding = 16.dp
private val SnackbarActionEndPadding = 8.dp
private val SnackbarVerticalPadding = 8.dp
private val MessageToActionSpacing = 12.dp
private val ActionHeight = 32.dp
private val ActionHorizontalPadding = 12.dp
private val ActionBorderWidth = 1.dp

private val snackbarShape = RoundedCornerShape(SnackbarCornerRadius)
// A button: the same corners as GGButton.
private val actionShape = AppConst.buttonShape

/** Состояние снекбара приложения; предоставляется [com.royaal.designsystem.components.screen.AppScreen]. */
val LocalSnackbarHostState: ProvidableCompositionLocal<SnackbarHostState> = staticCompositionLocalOf {
    error("No SnackbarHostState provided - is this composable called outside of AppScreen?")
}

@Composable
fun GGSnackbarHost(
    hostState: SnackbarHostState,
    modifier: Modifier = Modifier,
) {
    SnackbarHost(
        hostState = hostState,
        modifier = modifier,
        snackbar = { data -> GGSnackbar(data) },
    )
}

/**
 * Показать снекбар: что только что произошло, в прошедшем времени, без точки
 * ("Removed from Backlog"), и одно действие, чтобы вернуть ("Undo").
 *
 * По GGData новый снекбар заменяет текущий, а не встаёт в очередь.
 * Без действия держится 4 секунды ([SnackbarDuration.Short]); [SnackbarDuration.Indefinite] —
 * пока не закроют.
 *
 * ```
 * scope.launch {
 *     val result = snackbarHostState.showGGSnackbar("Removed from Backlog", actionLabel = "Undo")
 *     if (result == SnackbarResult.ActionPerformed) restore()
 * }
 * ```
 */
suspend fun SnackbarHostState.showGGSnackbar(
    message: String,
    actionLabel: String? = null,
    duration: SnackbarDuration = SnackbarDuration.Short,
): SnackbarResult {
    currentSnackbarData?.dismiss()
    return showSnackbar(
        message = message,
        actionLabel = actionLabel,
        withDismissAction = false,
        duration = duration,
    )
}

@Composable
private fun GGSnackbar(
    data: SnackbarData,
    modifier: Modifier = Modifier,
) {
    val colors = AppTheme.snackbarColors
    val actionLabel = data.visuals.actionLabel

    Row(
        modifier = modifier
            .padding(SnackbarMargin)
            .widthIn(max = SnackbarMaxWidth)
            .fillMaxWidth()
            .heightIn(min = SnackbarMinHeight)
            .shadow(SnackbarElevation, snackbarShape)
            .background(colors.container, snackbarShape)
            .padding(
                start = SnackbarHorizontalPadding,
                end = if (actionLabel != null) SnackbarActionEndPadding else SnackbarHorizontalPadding,
                top = SnackbarVerticalPadding,
                bottom = SnackbarVerticalPadding,
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(MessageToActionSpacing),
    ) {
        Text(
            text = data.visuals.message,
            style = AppTheme.typography.bodyMedium,
            color = colors.content,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
        )
        if (actionLabel != null) {
            val actionInteractionSource = remember { MutableInteractionSource() }
            Box(
                modifier = Modifier
                    .pressScale(actionInteractionSource)
                    .height(ActionHeight)
                    .clip(actionShape)
                    .border(ActionBorderWidth, colors.content, actionShape)
                    .clickable(
                        interactionSource = actionInteractionSource,
                        indication = null,
                        role = Role.Button,
                        onClick = data::performAction,
                    )
                    .padding(horizontal = ActionHorizontalPadding),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = actionLabel,
                    style = AppTheme.typography.labelMedium,
                    fontWeight = FontWeight.Medium,
                    color = colors.content,
                    maxLines = 1,
                )
            }
        }
    }
}

@Preview
@Composable
private fun Preview() = BasePreview {
    GGSnackbar(data = object : SnackbarData {
        override val visuals: SnackbarVisuals
            get() = object : SnackbarVisuals {
                override val message: String
                    get() = "Hello, World!"
                override val actionLabel: String? = "Close"
                override val withDismissAction: Boolean
                    get() = true
                override val duration: SnackbarDuration
                    get() = SnackbarDuration.Short
            }

        override fun performAction() = Unit

        override fun dismiss() = Unit
    })
}