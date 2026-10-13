package com.royaal.collections.ui.list

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.royaal.collections.ui.common.MAX_COLLECTION_NAME_LENGTH
import com.royaal.collections.ui.common.title
import com.royaal.designsystem.components.bottomSheet.GGBottomSheetDialog
import com.royaal.designsystem.components.button.GGButton
import com.royaal.designsystem.components.button.GGButtonType
import com.royaal.designsystem.components.chips.GGFilterChip
import com.royaal.designsystem.components.dialog.GGDialog
import com.royaal.designsystem.components.input.GGInput
import com.royaal.designsystem.theme.AppTheme
import com.royaal.designsystem.theme.textColors
import com.royaal.entities.data.GamePlayStatus
import ggdata.feature.collections.generated.resources.Res
import ggdata.feature.collections.generated.resources.cancel
import ggdata.feature.collections.generated.resources.create
import ggdata.feature.collections.generated.resources.createHint
import ggdata.feature.collections.generated.resources.createNameTaken
import ggdata.feature.collections.generated.resources.createNameTooLong
import ggdata.feature.collections.generated.resources.createOrAddStatus
import ggdata.feature.collections.generated.resources.createTitle
import org.jetbrains.compose.resources.stringResource

/**
 * Names a new collection, or adds one of [statusesToAdd] with a single tap.
 *
 * @param takenNames lowercased names of the user's collections: a new one can't repeat them.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun CreateCollectionDialog(
    takenNames: Set<String>,
    statusesToAdd: List<GamePlayStatus>,
    onCreate: (name: String) -> Unit,
    onAddStatus: (GamePlayStatus) -> Unit,
    onDismissRequest: () -> Unit,
) {
    var name by rememberSaveable { mutableStateOf("") }
    val trimmedName = name.trim()
    val error = when {
        trimmedName.length > MAX_COLLECTION_NAME_LENGTH ->
            stringResource(Res.string.createNameTooLong, MAX_COLLECTION_NAME_LENGTH)
        trimmedName.lowercase() in takenNames -> stringResource(Res.string.createNameTaken)
        else -> null
    }
    val canCreate = trimmedName.isNotEmpty() && error == null

    GGBottomSheetDialog(
        onDismissRequest = onDismissRequest,
        title = stringResource(Res.string.createTitle),
        actions = { dismiss ->
            GGButton(
                text = stringResource(Res.string.cancel),
                onClick = dismiss,
                buttonType = GGButtonType.OUTLINED,
            )
            GGButton(
                text = stringResource(Res.string.create),
                enabled = canCreate,
                onClick = {
                    onCreate(trimmedName)
                    dismiss()
                },
            )
        },
    ) { dismiss ->
        GGInput(
            modifier = Modifier.fillMaxWidth(),
            initialValue = name,
            hint = stringResource(Res.string.createHint),
            error = error,
            onImeAction = {
                if (canCreate) {
                    onCreate(trimmedName)
                    dismiss()
                }
            },
            onValueChanged = { name = it },
        )
        if (statusesToAdd.isNotEmpty()) {
            Spacer(Modifier.height(16.dp))
            Text(
                text = stringResource(Res.string.createOrAddStatus),
                style = AppTheme.typography.labelLarge,
                color = AppTheme.textColors.secondary,
            )
            Spacer(Modifier.height(8.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                statusesToAdd.forEach { status ->
                    GGFilterChip(
                        text = stringResource(status.title),
                        selected = false,
                        onClick = {
                            onAddStatus(status)
                            dismiss()
                        },
                    )
                }
            }
        }
    }
}
