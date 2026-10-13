package com.royaal.game.ui.library

import androidx.lifecycle.SavedStateHandle
import com.royaal.arch.mvi.MviReducer
import com.royaal.arch.mvi.ScreenSnapshot
import com.royaal.arch.mvi.SnapshotHandler
import com.royaal.arch.mvi.viewmodel.MviViewModel
import com.royaal.game.domain.GameRepository

/**
 * Behind [AddToLibrarySheet]. Scoped to the screen showing the sheet, not to the sheet: a save
 * goes on after the sheet has closed.
 */
internal class LibraryViewModel(
    savedStateHandle: SavedStateHandle,
    private val gameId: Long,
    private val repo: GameRepository,
) : MviViewModel<
        LibraryState,
        LibraryUserEvent,
        LibraryInnerEvent,
        LibrarySideEffect,
        ScreenSnapshot.NoSnapshot,
        >(
            savedStateHandle = savedStateHandle,
            initialState = LibraryState(),
            reducer = LibraryReducer(),
            snapshotHandler = SnapshotHandler.noSnapshot(LibraryState()),
        ) {

    init {
        subscription(ENTRY_KEY) {
            launch {
                repo.observeLibraryEntry(gameId).collect { emit(LibraryInnerEvent.Update(it)) }
            }
        }
    }

    override fun onEvent(event: LibraryUserEvent) {
        when (event) {
            is LibraryUserEvent.Save -> intent(event) {
                repo.saveLibraryEntry(gameId, event.status, event.favourite, event.collectionIds)
            }
        }
    }

    private companion object {
        const val ENTRY_KEY = "Entry"
    }
}

private class LibraryReducer : MviReducer<LibraryState, LibraryInnerEvent> {
    override fun reduce(oldState: LibraryState, event: LibraryInnerEvent): LibraryState = when (event) {
        is LibraryInnerEvent.Update -> oldState.copy(entry = event.entry)
    }
}
