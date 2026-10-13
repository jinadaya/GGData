package com.royaal.collections.ui.details

import androidx.lifecycle.SavedStateHandle
import com.royaal.arch.mvi.ScreenSnapshot
import com.royaal.arch.mvi.SnapshotHandler
import com.royaal.arch.mvi.model.intent.ext.retryForever
import com.royaal.arch.mvi.viewmodel.MviViewModel
import com.royaal.collections.domain.CollectionKey
import com.royaal.collections.domain.CollectionsRepository
import com.royaal.collections.domain.RemovedGame
import com.royaal.collections.ui.common.MAX_COLLECTION_NAME_LENGTH
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
        if (key is CollectionKey.Custom) {
            subscription(NAMES_KEY) {
                bind(flow = repo.observeCollections(), policy = { retryForever() }) { collections ->
                    CollectionInnerEvent.UpdateOtherNames(
                        collections
                            .filter { it.key is CollectionKey.Custom && it.key != key }
                            .mapTo(mutableSetOf()) { it.name.lowercase() }
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

            is CollectionUserEvent.Rename -> intent(event) {
                val collectionId = state.value.collectionId ?: return@intent
                // The sheet validates too; this only guards against a bad name slipping through.
                val name = event.name.trim()
                val isValid = name.isNotEmpty() &&
                    name.length <= MAX_COLLECTION_NAME_LENGTH &&
                    name.lowercase() !in state.value.otherNames
                if (isValid) repo.rename(collectionId, name)
            }

            is CollectionUserEvent.Reorder -> intent(event) {
                val collectionId = state.value.collectionId ?: return@intent
                repo.reorder(collectionId, event.gameIds)
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
        const val NAMES_KEY = "Names"
    }
}
