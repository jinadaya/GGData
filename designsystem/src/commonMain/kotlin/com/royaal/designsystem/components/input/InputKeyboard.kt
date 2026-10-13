package com.royaal.designsystem.components.input

import androidx.compose.foundation.text.input.KeyboardActionHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction

/**
 * The keyboard's action key: calls [onImeAction], then does what the key promises - moves focus
 * on `Next`/`Previous`, otherwise clears it, which hides the keyboard. On iOS that's the main
 * way to close it: the keyboard has no key of its own for that.
 */
@Composable
internal fun rememberImeActionHandler(
    imeAction: ImeAction,
    onImeAction: (() -> Unit)?,
): KeyboardActionHandler {
    val focusManager = LocalFocusManager.current
    val currentOnImeAction = rememberUpdatedState(onImeAction)
    return remember(focusManager, imeAction) {
        KeyboardActionHandler {
            currentOnImeAction.value?.invoke()
            when (imeAction) {
                ImeAction.Next -> focusManager.moveFocus(FocusDirection.Next)
                ImeAction.Previous -> focusManager.moveFocus(FocusDirection.Previous)
                else -> focusManager.clearFocus()
            }
        }
    }
}
