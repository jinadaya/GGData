package com.royaal.game.ui.journal

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.royaal.designsystem.components.bottomSheet.GGBottomSheetDialog
import com.royaal.designsystem.components.button.GGButton
import com.royaal.designsystem.components.button.GGButtonType
import com.royaal.designsystem.components.chips.GGFilterChip
import com.royaal.designsystem.components.input.GGInput
import com.royaal.designsystem.components.snackbar.LocalSnackbarHostState
import com.royaal.designsystem.components.snackbar.showGGSnackbar
import com.royaal.designsystem.theme.AppTheme
import com.royaal.designsystem.theme.textColors
import com.royaal.entities.data.GamePlayStatus
import com.royaal.entities.ui.UiGame
import com.royaal.game.domain.JournalEntry
import com.royaal.game.ui.common.JournalStatuses
import com.royaal.game.ui.common.title
import ggdata.feature.game.generated.resources.Res
import ggdata.feature.game.generated.resources.addStatus
import ggdata.feature.game.generated.resources.cancel
import ggdata.feature.game.generated.resources.journalHours
import ggdata.feature.game.generated.resources.journalHoursHint
import ggdata.feature.game.generated.resources.journalHoursInvalid
import ggdata.feature.game.generated.resources.journalNote
import ggdata.feature.game.generated.resources.journalNoteHint
import ggdata.feature.game.generated.resources.journalRating
import ggdata.feature.game.generated.resources.journalSaved
import ggdata.feature.game.generated.resources.journalTitle
import ggdata.feature.game.generated.resources.save
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

private const val MAX_HOURS = 99_999
private val RATINGS = 1..10

/** What the user types and picks in the sheet, applied only on Save. Hours stay text until then. */
private data class JournalDraft(
    val status: GamePlayStatus?,
    val hours: String,
    val rating: Int?,
    val note: String,
) {
    constructor(entry: JournalEntry) : this(
        status = entry.status,
        hours = entry.hoursPlayed?.toString().orEmpty(),
        rating = entry.rating,
        note = entry.note.orEmpty(),
    )

    /** `null` while the hours aren't a whole number of them. */
    fun toEntry(): JournalEntry? {
        val hoursText = hours.trim()
        val hoursPlayed = if (hoursText.isEmpty()) {
            null
        } else {
            hoursText.toIntOrNull()?.takeIf { it in 0..MAX_HOURS } ?: return null
        }
        return JournalEntry(
            status = status,
            hoursPlayed = hoursPlayed,
            rating = rating,
            note = note.trim().takeIf { it.isNotEmpty() },
        )
    }
}

/**
 * The game in the user's journal: its status, the hours they've put in, their own rating and
 * a note.
 *
 * @param onSaved called on Save, as the sheet closes. Show feedback from the screen, e.g. with
 * [rememberJournalSavedSnackbar]: whatever the sheet launches itself is cancelled once it's gone.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JournalEntrySheet(
    game: UiGame,
    onDismissRequest: () -> Unit,
    onSaved: (UiGame) -> Unit = {},
) {
    // Keyed by game: another game's sheet on the same screen gets its own.
    val viewModel = koinViewModel<JournalEntryViewModel>(key = "journal/${game.id}") { parametersOf(game.id) }
    val entry = viewModel.state.collectAsStateWithLifecycle().value.entry
    // Starts from the entry once it's loaded; later updates of it don't override what's typed.
    var draft by remember(entry == null) { mutableStateOf(entry?.let(::JournalDraft)) }

    GGBottomSheetDialog(
        onDismissRequest = onDismissRequest,
        title = stringResource(Res.string.journalTitle),
        // Taller than half the screen: half open, it would hide Save.
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        actions = { dismiss ->
            GGButton(
                text = stringResource(Res.string.cancel),
                onClick = dismiss,
                buttonType = GGButtonType.OUTLINED,
            )
            val edited = draft?.toEntry()
            GGButton(
                text = stringResource(Res.string.save),
                enabled = edited != null && entry != null && edited != entry,
                onClick = {
                    if (edited != null) {
                        viewModel.sendEvent(JournalEntryUserEvent.Save(edited))
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
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    JournalStatuses.forEach { status ->
                        val selected = current.status == status
                        GGFilterChip(
                            text = stringResource(status.title),
                            selected = selected,
                            // Tapping the selected status clears it.
                            onClick = { draft = current.copy(status = status.takeUnless { selected }) },
                        )
                    }
                }
            }
            Section(title = stringResource(Res.string.journalHours)) {
                GGInput(
                    modifier = Modifier.fillMaxWidth(),
                    initialValue = current.hours,
                    hint = stringResource(Res.string.journalHoursHint),
                    error = if (current.toEntry() == null) stringResource(Res.string.journalHoursInvalid) else null,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
                    onValueChanged = { hours -> draft = draft?.copy(hours = hours) },
                )
            }
            Section(title = stringResource(Res.string.journalRating)) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    RATINGS.forEach { rating ->
                        val selected = current.rating == rating
                        GGFilterChip(
                            text = rating.toString(),
                            selected = selected,
                            // Tapping the selected rating clears it.
                            onClick = { draft = current.copy(rating = rating.takeUnless { selected }) },
                        )
                    }
                }
            }
            Section(title = stringResource(Res.string.journalNote)) {
                GGInput(
                    modifier = Modifier.fillMaxWidth(),
                    initialValue = current.note,
                    hint = stringResource(Res.string.journalNoteHint),
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Sentences,
                        imeAction = ImeAction.Default,
                    ),
                    lineLimits = TextFieldLineLimits.MultiLine(minHeightInLines = 3, maxHeightInLines = 6),
                    onValueChanged = { note -> draft = draft?.copy(note = note) },
                )
            }
        }
    }
}

/**
 * "<game> saved to your journal", for [JournalEntrySheet]'s `onSaved`. Remember it at the screen
 * level: the snackbar outlives the sheet.
 */
@Composable
fun rememberJournalSavedSnackbar(): (UiGame) -> Unit {
    val snackbarHostState = LocalSnackbarHostState.current
    val scope = rememberCoroutineScope()
    return remember(snackbarHostState, scope) {
        { game ->
            scope.launch { snackbarHostState.showGGSnackbar(getString(Res.string.journalSaved, game.title)) }
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
        content()
    }
}
