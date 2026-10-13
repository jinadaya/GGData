package com.royaal.collections.ui.details

import androidx.lifecycle.SavedStateHandle
import com.royaal.arch.mvi.ScreenSnapshot
import com.royaal.arch.mvi.SnapshotHandler
import com.royaal.arch.mvi.viewmodel.MviViewModel
import com.royaal.collections.domain.CollectionKey
import com.royaal.collections.domain.CollectionsRepository
import com.royaal.collections.domain.RemovedGame
import com.royaal.entities.Mapper
import com.royaal.entities.data.GameData
import com.royaal.entities.ui.UiGame
import com.royaal.entities.ui.mappers.GameUiMapper

internal class CollectionViewModel(
    savedStateHandle: SavedStateHandle,
    key: CollectionKey,
    private val repo: CollectionsRepository,
    private val gameUiMapper: Mapper<GameData, UiGame> = GameUiMapper(),
) : MviViewModel<
        CollectionScreenState,
        CollectionUserEvent,
        CollectionInnerEvent,
        CollectionSideEffect,
        ScreenSnapshot.NoSnapshot,
        >(
            savedStateHandle = savedStateHandle,
            initialState = CollectionScreenState(key),
            reducer = CollectionReducer(),
            snapshotHandler = SnapshotHandler.noSnapshot(CollectionScreenState(key)),
        ) {

    /** The last game taken out, until undone or another one is removed. */
    private var lastRemoved: RemovedGame? = null

    init {
        subscription(COLLECTION_KEY) {
            launch {
                repo.observeCollection(key).collect { details ->
                    // Gone: deleted here, and Delete closes the screen itself.
                    if (details == null) return@collect
                    emit(
                        CollectionInnerEvent.Update(
                            key = details.key,
                            title = details.name,
                            collectionId = details.collectionId,
                            pinned = details.pinned,
                            games = details.games.map(gameUiMapper::map),
                        )
                    )
                }
            }
        }
    }

    override fun onEvent(event: CollectionUserEvent) {
        when (event) {
            CollectionUserEvent.TogglePin -> intent(event) {
                val collectionId = state.value.collectionId ?: return@intent
                repo.setPinned(collectionId, !state.value.pinned)
            }

            CollectionUserEvent.Delete -> intent(event) {
                val collectionId = state.value.collectionId ?: return@intent
                repo.deleteCollection(collectionId)
                postEffect(CollectionSideEffect.Close)
            }

            is CollectionUserEvent.RemoveGame -> intent(event) {
                lastRemoved = repo.removeGame(state.value.key, event.gameId) ?: return@intent
                postEffect(CollectionSideEffect.ShowRemoved)
            }

            CollectionUserEvent.UndoRemove -> intent(event) {
                val removed = lastRemoved ?: return@intent
                lastRemoved = null
                repo.restoreGame(removed)
            }
        }
    }

    private companion object {
        const val COLLECTION_KEY = "Collection"
    }
}
