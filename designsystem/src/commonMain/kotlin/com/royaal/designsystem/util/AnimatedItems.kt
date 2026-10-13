package com.royaal.designsystem.util

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridItemScope
import androidx.compose.foundation.lazy.grid.LazyGridItemSpanScope
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * An item that fades in when it shows up, fades out when it goes, and slides to its new place
 * when others come and go around it: `item` with `Modifier.animateItem()` on its content. Give it
 * a [key]: without one, an item is told apart only by its index.
 */
fun LazyListScope.animatedItem(
    key: Any? = null,
    contentType: Any? = null,
    content: @Composable LazyItemScope.() -> Unit,
) = item(key = key, contentType = contentType) {
    val scope = this
    Box(Modifier.animateItem()) { scope.content() }
}

/** [animatedItem] of a grid. */
fun LazyGridScope.animatedItem(
    key: Any? = null,
    span: (LazyGridItemSpanScope.() -> GridItemSpan)? = null,
    contentType: Any? = null,
    content: @Composable LazyGridItemScope.() -> Unit,
) = item(key = key, span = span, contentType = contentType) {
    val scope = this
    Box(Modifier.animateItem()) { scope.content() }
}
