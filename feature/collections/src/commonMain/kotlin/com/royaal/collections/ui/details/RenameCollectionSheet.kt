package com.royaal.collections.ui.details

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import com.royaal.collections.ui.common.collectionNameError
import com.royaal.designsystem.components.bottomSheet.GGBottomSheetDialog
import com.royaal.designsystem.components.button.GGButton
import com.royaal.designsystem.components.button.GGButtonType
import com.royaal.designsystem.components.input.GGInput
import ggdata.feature.collections.generated.resources.Res
import ggdata.feature.collections.generated.resources.cancel
import ggdata.feature.collections.generated.resources.createHint
import ggdata.feature.collections.generated.resources.renameTitle
import ggdata.feature.collections.generated.resources.save
import org.jetbrains.compose.resources.stringResource

/**
 * A new name for a user's collection.
 *
 * @param takenNames lowercased names of the user's other collections: the new one can't repeat them.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun RenameCollectionSheet(
    currentName: String,
    takenNames: Set<String>,
    onRename: (name: String) -> Unit,
    onDismissRequest: () -> Unit,
) {
    var name by rememberSaveable { mutableStateOf(currentName) }
    val trimmedName = name.trim()
    val error = collectionNameError(name, takenNames)
    val canSave = trimmedName.isNotEmpty() && error == null && trimmedName != currentName

    GGBottomSheetDialog(
        onDismissRequest = onDismissRequest,
        title = stringResource(Res.string.renameTitle),
        actions = { dismiss ->
            GGButton(
                text = stringResource(Res.string.cancel),
                onClick = dismiss,
                buttonType = GGButtonType.OUTLINED,
            )
            GGButton(
                text = stringResource(Res.string.save),
                enabled = canSave,
                onClick = {
                    onRename(trimmedName)
                    dismiss()
                },
            )
        },
    ) { dismiss ->
        // Opened to type: the keyboard comes up at once.
        val focusRequester = remember { FocusRequester() }
        LaunchedEffect(focusRequester) { focusRequester.requestFocus() }
        GGInput(
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(focusRequester),
            initialValue = name,
            hint = stringResource(Res.string.createHint),
            error = error,
            onImeAction = {
                if (canSave) {
                    onRename(trimmedName)
                    dismiss()
                }
            },
            onValueChanged = { name = it },
        )
    }
}
