package com.royaal.collections.ui.details

import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/** How close to the list's edge the dragged item has to be for the list to scroll on. */
private val AutoScrollEdge = 48.dp

/**
 * Items of a LazyColumn dragged into a new order. Only items with the dragged one's content type
 * swap places with it, so headers stay put.
 *
 * @param onMove the dragged item has passed another one: move it there in the list.
 * @param onDrop the item is let go: save the order.
 */
@Stable
internal class ReorderState(
    private val listState: LazyListState,
    private val scope: CoroutineScope,
    private val autoScrollEdgePx: Float,
    private val onMove: (fromKey: Any, toKey: Any) -> Unit,
    private val onDrop: () -> Unit,
) {
    var draggedKey: Any? by mutableStateOf(null)
        private set

    /** How far the dragged item is from where the list has laid it out. */
    var draggedOffset: Float by mutableFloatStateOf(0f)
        private set

    fun onDragStart(key: Any) {
        draggedKey = key
        draggedOffset = 0f
    }

    fun onDrag(delta: Float) {
        val key = draggedKey ?: return
        draggedOffset += delta
        val layoutInfo = listState.layoutInfo
        val dragged = layoutInfo.visibleItemsInfo.firstOrNull { it.key == key } ?: return
        val top = dragged.offset + draggedOffset
        val center = (top + dragged.size / 2f).toInt()
        val target = layoutInfo.visibleItemsInfo.firstOrNull {
            it.key != key && it.contentType == dragged.contentType && center in it.offset until it.offset + it.size
        }
        if (target != null) {
            // The list keeps its first visible item in place: moving that one would scroll it away.
            val firstIndex = listState.firstVisibleItemIndex
            val firstOffset = listState.firstVisibleItemScrollOffset
            onMove(key, target.key)
            // Laid out at the target's place from now on, yet it stays under the finger.
            draggedOffset += dragged.offset - target.offset
            if (dragged.index == firstIndex || target.index == firstIndex) {
                scope.launch { listState.scrollToItem(firstIndex, firstOffset) }
            }
        }
        val scrollBy = when {
            top < layoutInfo.viewportStartOffset + autoScrollEdgePx ->
                top - (layoutInfo.viewportStartOffset + autoScrollEdgePx)
            top + dragged.size > layoutInfo.viewportEndOffset - autoScrollEdgePx ->
                top + dragged.size - (layoutInfo.viewportEndOffset - autoScrollEdgePx)
            else -> 0f
        }
        if (scrollBy != 0f) {
            scope.launch {
                // Slowly: the finger moves on in small steps, each one scrolls a little.
                val scrolled = listState.scrollBy(scrollBy / 4)
                draggedOffset += scrolled
            }
        }
    }

    fun onDragEnd() {
        if (draggedKey == null) return
        draggedKey = null
        draggedOffset = 0f
        onDrop()
    }
}

@Composable
internal fun rememberReorderState(
    listState: LazyListState,
    onMove: (fromKey: Any, toKey: Any) -> Unit,
    onDrop: () -> Unit,
): ReorderState {
    val scope = rememberCoroutineScope()
    val autoScrollEdgePx = with(LocalDensity.current) { AutoScrollEdge.toPx() }
    val currentOnMove by rememberUpdatedState(onMove)
    val currentOnDrop by rememberUpdatedState(onDrop)
    return remember(listState, scope, autoScrollEdgePx) {
        ReorderState(
            listState = listState,
            scope = scope,
            autoScrollEdgePx = autoScrollEdgePx,
            onMove = { from, to -> currentOnMove(from, to) },
            onDrop = { currentOnDrop() },
        )
    }
}

/** Drags the item with [key] by this handle. */
internal fun Modifier.reorderHandle(state: ReorderState, key: Any): Modifier = pointerInput(state, key) {
    detectDragGestures(
        onDragStart = { state.onDragStart(key) },
        onDragEnd = state::onDragEnd,
        onDragCancel = state::onDragEnd,
    ) { change, amount ->
        change.consume()
        state.onDrag(amount.y)
    }
}
