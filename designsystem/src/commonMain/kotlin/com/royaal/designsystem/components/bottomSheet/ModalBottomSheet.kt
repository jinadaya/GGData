package com.royaal.designsystem.components.bottomSheet

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberBottomSheetState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.royaal.designsystem.components.tag.GGTag
import com.royaal.designsystem.theme.AppTheme
import com.royaal.designsystem.theme.bottomSheetColors
import com.royaal.designsystem.util.BasePreview
import kotlinx.coroutines.launch

private val SheetMaxWidth = 640.dp
private const val SheetMaxHeightFraction = 0.9f
private val SheetCornerRadius = 16.dp
private val HandleWidth = 32.dp
private val HandleHeight = 4.dp
private val HandleVerticalPadding = 12.dp
private val HorizontalPadding = 24.dp
private val TitleBottomPadding = 16.dp
private val ActionsSpacing = 12.dp
private val ActionsVerticalPadding = 16.dp
private val BottomPadding = 24.dp

private val sheetShape = RoundedCornerShape(
    topStart = SheetCornerRadius,
    topEnd = SheetCornerRadius,
)

/**
 * Нижняя шторка GGData для короткой задачи текущего экрана:
 * "Add to collection", сортировка и фильтры, быстрые действия.
 *
 * Показывается, пока находится в композиции:
 * ```
 * if (showSheet) {
 *     GGBottomSheetDialog(
 *         onDismissRequest = { showSheet = false },
 *         title = "Add to collection",
 *         actions = { dismiss ->
 *             GGButton(text = "Cancel", type = OUTLINED, onClick = dismiss)
 *             GGButton(text = "Add", onClick = { add(); dismiss() })
 *         },
 *     ) { dismiss ->
 *         ...
 *     }
 * }
 * ```
 *
 * @param onDismissRequest вызывается после того, как шторка закрылась: тап по scrim,
 * «Назад», свайп вниз или `dismiss()` из контента. Здесь уберите её из композиции.
 * @param actions кнопки под контентом, прижаты вправо: сначала OUTLINED «отмена», последней
 * NORMAL «подтвердить». `dismiss` закрывает шторку с анимацией, затем вызывает [onDismissRequest].
 * @param header не прокручивается: остаётся под заголовком, пока [content] прокручивается
 * (например, обложка). С теми же отступами 24dp по бокам.
 * @param content прокручиваемое содержимое с отступами 24dp по бокам.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GGBottomSheetDialog(
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    title: String? = null,
    sheetState: SheetState = rememberBottomSheetState(initialValue = SheetValue.Hidden),
    actions: (@Composable RowScope.(dismiss: () -> Unit) -> Unit)? = null,
    header: (@Composable ColumnScope.() -> Unit)? = null,
    content: @Composable ColumnScope.(dismiss: () -> Unit) -> Unit,
) {
    val colors = AppTheme.bottomSheetColors
    val scope = rememberCoroutineScope()
    val currentOnDismissRequest by rememberUpdatedState(onDismissRequest)

    val dismiss: () -> Unit = remember(sheetState, scope) {
        {
            scope.launch { sheetState.hide() }.invokeOnCompletion {
                if (!sheetState.isVisible) currentOnDismissRequest()
            }
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        modifier = modifier,
        sheetState = sheetState,
        sheetMaxWidth = SheetMaxWidth,
        shape = sheetShape,
        containerColor = colors.container,
        contentColor = colors.content,
        tonalElevation = 0.dp,
        scrimColor = colors.scrim,
        dragHandle = { DragHandle() },
    ) {
        BoxWithConstraints {
            val maxSheetHeight: Dp = if (constraints.hasBoundedHeight) {
                maxHeight * SheetMaxHeightFraction
            } else {
                Dp.Unspecified
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = maxSheetHeight),
            ) {
                title?.let { title ->
                    Text(
                        text = title,
                        style = AppTheme.typography.titleLarge,
                        color = colors.content,
                        modifier = Modifier
                            .padding(horizontal = HorizontalPadding)
                            .padding(bottom = TitleBottomPadding),
                    )
                }

                header?.let { header ->
                    Column(
                        modifier = Modifier
                            .padding(horizontal = HorizontalPadding)
                            .padding(bottom = TitleBottomPadding),
                        content = header,
                    )
                }

                // weight(fill = false): контент занимает сколько нужно,
                // а если не влезает — прокручивается, и действия остаются видны.
                Column(
                    modifier = Modifier
                        .weight(1f, fill = false)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = HorizontalPadding),
                ) {
                    content(dismiss)
                }

                if (actions != null) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = HorizontalPadding, vertical = ActionsVerticalPadding),
                        horizontalArrangement = Arrangement.spacedBy(ActionsSpacing, Alignment.End),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        actions(dismiss)
                    }
                } else {
                    Spacer(Modifier.height(BottomPadding))
                }
            }
        }
    }
}

@Composable
private fun DragHandle() {
    val colors = AppTheme.bottomSheetColors
    Box(
        modifier = Modifier
            .padding(vertical = HandleVerticalPadding)
            .size(width = HandleWidth, height = HandleHeight)
            .background(color = colors.handle, shape = CircleShape),
    )
}