package com.royaal.designsystem.components.dialog

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.EaseOut
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.semantics.paneTitle
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.royaal.designsystem.theme.AppTheme
import com.royaal.designsystem.theme.dialogColors
import com.royaal.designsystem.util.BasePreview

private val DialogMaxWidth = 400.dp
private val DialogMargin = 24.dp
private val DialogPadding = 24.dp
private val DialogCornerRadius = 16.dp
private val DialogElevation = 8.dp
private val TitleToBodySpacing = 12.dp
private val BodyToActionsSpacing = 24.dp
private val ActionsSpacing = 12.dp

private const val EnterDurationMs = 200
private const val EnterInitialScale = 0.95f

private val dialogShape = RoundedCornerShape(DialogCornerRadius)

/**
 * Диалог GGData — только чтобы подтвердить то, что нельзя отменить ("Delete Backlog?"),
 * или чтобы намеренно прервать. Всё, что можно отменить, делайте сразу и предлагайте
 * Undo в Snackbar.
 *
 * Показывается, пока находится в композиции:
 * ```
 * if (showDialog) {
 *     GGDialog(
 *         onDismissRequest = { showDialog = false },
 *         title = "Delete Backlog?",
 *         text = "The collection and its 42 games will be removed. This can't be undone.",
 *     ) {
 *         GGButton(text = "Cancel", type = OUTLINED, onClick = { showDialog = false })
 *         GGButton(text = "Delete collection", onClick = { delete(); showDialog = false })
 *     }
 * }
 * ```
 *
 * @param title вопрос или утверждение.
 * @param text одно-два предложения о последствиях.
 * @param dismissible false — тап мимо и «Назад» ничего не делают, нужно выбрать действие.
 * @param actions не больше двух кнопок, прижаты вправо: сначала OUTLINED «Cancel»,
 * последней NORMAL с глаголом из заголовка ("Delete collection"), не "OK".
 */
@Composable
fun GGDialog(
    onDismissRequest: () -> Unit,
    title: String,
    modifier: Modifier = Modifier,
    text: String? = null,
    dismissible: Boolean = true,
    actions: @Composable RowScope.() -> Unit,
) {
    val colors = AppTheme.dialogColors

    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(
            dismissOnBackPress = dismissible,
            dismissOnClickOutside = dismissible,
            // Свою ширину задаём сами: до 400dp и 24dp от краёв.
            usePlatformDefaultWidth = false,
        ),
    ) {
        val visibleState = remember { MutableTransitionState(false).apply { targetState = true } }
        AnimatedVisibility(
            visibleState = visibleState,
            enter = fadeIn(tween(EnterDurationMs, easing = EaseOut)) +
                    scaleIn(tween(EnterDurationMs, easing = EaseOut), initialScale = EnterInitialScale),
        ) {
            Column(
                modifier = modifier
                    .padding(DialogMargin)
                    .widthIn(max = DialogMaxWidth)
                    .fillMaxWidth()
                    .shadow(DialogElevation, dialogShape)
                    .background(colors.container, dialogShape)
                    .padding(DialogPadding)
                    .semantics { paneTitle = title },
            ) {
                Text(
                    text = title,
                    style = AppTheme.typography.titleLarge,
                    color = colors.title,
                )
                if (text != null) {
                    Spacer(Modifier.height(TitleToBodySpacing))
                    Text(
                        text = text,
                        style = AppTheme.typography.bodyMedium,
                        color = colors.body,
                    )
                }
                Spacer(Modifier.height(BodyToActionsSpacing))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(ActionsSpacing, Alignment.End),
                    verticalAlignment = Alignment.CenterVertically,
                    content = actions,
                )
            }
        }
    }
}