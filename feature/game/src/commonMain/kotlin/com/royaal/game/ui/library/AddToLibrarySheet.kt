package com.royaal.game.ui.library

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.royaal.designsystem.components.bottomSheet.GGBottomSheetDialog
import com.royaal.designsystem.components.button.GGButton
import com.royaal.designsystem.components.button.GGButtonType
import com.royaal.designsystem.components.chips.GGFilterChip
import com.royaal.designsystem.components.snackbar.LocalSnackbarHostState
import com.royaal.designsystem.components.snackbar.showGGSnackbar
import com.royaal.designsystem.theme.AppTheme
import com.royaal.designsystem.theme.textColors
import com.royaal.entities.data.GamePlayStatus
import com.royaal.entities.ui.UiGame
import com.royaal.game.domain.GameLibraryEntry
import com.royaal.game.ui.common.JournalStatuses
import com.royaal.game.ui.common.title
import ggdata.feature.game.generated.resources.Res
import ggdata.feature.game.generated.resources.addCollections
import ggdata.feature.game.generated.resources.addNoCollections
import ggdata.feature.game.generated.resources.addStatus
import ggdata.feature.game.generated.resources.addTitle
import ggdata.feature.game.generated.resources.cancel
import ggdata.feature.game.generated.resources.favourite
import ggdata.feature.game.generated.resources.save
import ggdata.feature.game.generated.resources.saved
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

/** What the user picks in the sheet, applied only on Save. */
private data class LibraryDraft(
    val status: GamePlayStatus?,
    val favourite: Boolean,
    val collectionIds: Set<Long>,
) {
    constructor(entry: GameLibraryEntry) : this(entry.status, entry.favourite, entry.collectionIds)
}

/**
 * Puts [game] into the library: a status, favourite and the user's own collections.
 *
 * @param onSaved called on Save, as the sheet closes. Show feedback from the screen, e.g. with
 * [rememberLibrarySavedSnackbar]: whatever the sheet launches itself is cancelled once it's gone.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddToLibrarySheet(
    game: UiGame,
    onDismissRequest: () -> Unit,
    onSaved: (UiGame) -> Unit = {},
) {
    // Keyed by game: another game's sheet on the same screen gets its own.
    val viewModel = koinViewModel<LibraryViewModel>(key = "library/${game.id}") { parametersOf(game.id) }
    val entry = viewModel.state.collectAsStateWithLifecycle().value.entry
    // Starts from the entry once it's loaded; later updates of it don't override the user's picks.
    var draft by remember(entry == null) { mutableStateOf(entry?.let(::LibraryDraft)) }

    GGBottomSheetDialog(
        onDismissRequest = onDismissRequest,
        title = stringResource(Res.string.addTitle),
        actions = { dismiss ->
            GGButton(
                text = stringResource(Res.string.cancel),
                onClick = dismiss,
                buttonType = GGButtonType.OUTLINED,
            )
            val current = draft
            GGButton(
                text = stringResource(Res.string.save),
                enabled = current != null && entry != null && current != LibraryDraft(entry),
                onClick = {
                    if (current != null) {
                        viewModel.sendEvent(
                            LibraryUserEvent.Save(current.status, current.favourite, current.collectionIds)
                        )
                        onSaved(game)
                    }
                    dismiss()
                },
            )
        },
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text(
                text = game.title,
                style = AppTheme.typography.bodyMedium,
                color = AppTheme.textColors.secondary,
            )
            val current = draft ?: return@Column
            Section(title = stringResource(Res.string.addStatus)) {
                JournalStatuses.forEach { status ->
                    val selected = current.status == status
                    GGFilterChip(
                        text = stringResource(status.title),
                        selected = selected,
                        // Tapping the selected status clears it.
                        onClick = { draft = current.copy(status = status.takeUnless { selected }) },
                    )
                }
                GGFilterChip(
                    text = stringResource(Res.string.favourite),
                    selected = current.favourite,
                    onClick = { draft = current.copy(favourite = !current.favourite) },
                )
            }
            val collections = entry?.collections.orEmpty()
            Section(title = stringResource(Res.string.addCollections)) {
                if (collections.isEmpty()) {
                    Text(
                        text = stringResource(Res.string.addNoCollections),
                        style = AppTheme.typography.bodyMedium,
                        color = AppTheme.textColors.secondary,
                    )
                }
                collections.forEach { collection ->
                    val selected = collection.id in current.collectionIds
                    GGFilterChip(
                        text = collection.name,
                        selected = selected,
                        onClick = {
                            val ids = current.collectionIds
                            draft = current.copy(collectionIds = if (selected) ids - collection.id else ids + collection.id)
                        },
                    )
                }
            }
        }
    }
}

/**
 * "<game> saved to your library", for [AddToLibrarySheet]'s `onSaved`. Remember it at the screen
 * level: the snackbar outlives the sheet.
 */
@Composable
fun rememberLibrarySavedSnackbar(): (UiGame) -> Unit {
    val snackbarHostState = LocalSnackbarHostState.current
    val scope = rememberCoroutineScope()
    return remember(snackbarHostState, scope) {
        { game ->
            scope.launch { snackbarHostState.showGGSnackbar(getString(Res.string.saved, game.title)) }
        }
    }
}

@Composable
private fun Section(
    title: String,
    content: @Composable () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = title,
            style = AppTheme.typography.titleMedium,
        )
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            content()
        }
    }
}
