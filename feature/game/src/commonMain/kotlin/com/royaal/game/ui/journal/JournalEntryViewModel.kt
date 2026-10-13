package com.royaal.game.ui.journal

import androidx.lifecycle.SavedStateHandle
import com.royaal.arch.mvi.InnerEvent
import com.royaal.arch.mvi.MviReducer
import com.royaal.arch.mvi.ScreenSnapshot
import com.royaal.arch.mvi.ScreenState
import com.royaal.arch.mvi.SideEffect
import com.royaal.arch.mvi.SnapshotHandler
import com.royaal.arch.mvi.UserEvent
import com.royaal.arch.mvi.viewmodel.MviViewModel
import com.royaal.game.domain.GameRepository
import com.royaal.game.domain.JournalEntry

/** @param entry what's on the game now; `null` until loaded. */
internal data class JournalEntryState(
    val entry: JournalEntry? = null,
) : ScreenState

internal sealed interface JournalEntryUserEvent : UserEvent {
    data class Save(val entry: JournalEntry) : JournalEntryUserEvent
}

internal sealed interface JournalEntrySideEffect : SideEffect

internal sealed interface JournalEntryInnerEvent : InnerEvent {
    data class Update(val entry: JournalEntry) : JournalEntryInnerEvent
}

/**
 * Behind [JournalEntrySheet]. Scoped to the screen showing the sheet, not to the sheet: a save
 * goes on after the sheet has closed.
 */
internal class JournalEntryViewModel(
    savedStateHandle: SavedStateHandle,
    private val gameId: Long,
    private val repo: GameRepository,
) : MviViewModel<
        JournalEntryState,
        JournalEntryUserEvent,
        JournalEntryInnerEvent,
        JournalEntrySideEffect,
        ScreenSnapshot.NoSnapshot,
        >(
            savedStateHandle = savedStateHandle,
            initialState = JournalEntryState(),
            reducer = JournalEntryReducer(),
            snapshotHandler = SnapshotHandler.noSnapshot(JournalEntryState()),
        ) {

    init {
        subscription(ENTRY_KEY) {
            launch {
                repo.observeJournalEntry(gameId).collect { emit(JournalEntryInnerEvent.Update(it)) }
            }
        }
    }

    override fun onEvent(event: JournalEntryUserEvent) {
        when (event) {
            is JournalEntryUserEvent.Save -> intent(event) { repo.saveJournalEntry(gameId, event.entry) }
        }
    }

    private companion object {
        const val ENTRY_KEY = "Entry"
    }
}

private class JournalEntryReducer : MviReducer<JournalEntryState, JournalEntryInnerEvent> {
    override fun reduce(oldState: JournalEntryState, event: JournalEntryInnerEvent): JournalEntryState =
        when (event) {
            is JournalEntryInnerEvent.Update -> oldState.copy(entry = event.entry)
        }
}
