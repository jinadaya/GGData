package com.royaal.designsystem.util

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager

/*
 * The iOS keyboard has no key to close it, so screens with inputs close it themselves: clearing
 * focus hides it. Put these on the screen's root, together with the inputs' own action key.
 */

/**
 * Clears focus on a tap that nothing else handles: empty space, plain text. Taps on buttons,
 * cards and the input itself are consumed by them and don't get here.
 */
@Composable
fun Modifier.clearFocusOnTap(): Modifier {
    val focusManager = LocalFocusManager.current
    return pointerInput(focusManager) {
        detectTapGestures(onTap = { focusManager.clearFocus() })
    }
}

/** Clears focus as soon as the user scrolls any scrollable inside, like iOS lists do. */
@Composable
fun Modifier.clearFocusOnScroll(): Modifier {
    val focusManager = LocalFocusManager.current
    val connection = remember(focusManager) {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                if (source == NestedScrollSource.UserInput && available != Offset.Zero) {
                    focusManager.clearFocus()
                }
                return Offset.Zero
            }
        }
    }
    return nestedScroll(connection)
}
