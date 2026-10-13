package com.royaal.collections.ui.list

import androidx.lifecycle.SavedStateHandle
import com.royaal.arch.mvi.ScreenSnapshot
import com.royaal.arch.mvi.SnapshotHandler
import com.royaal.arch.mvi.model.intent.ext.retryForever
import com.royaal.arch.mvi.viewmodel.MviViewModel
import com.royaal.collections.domain.CollectionSummary
import com.royaal.collections.domain.CollectionsRepository
import com.royaal.collections.ui.common.MAX_COLLECTION_NAME_LENGTH
import com.royaal.collections.ui.common.UiCollection
import com.royaal.entities.Mapper
import com.royaal.entities.data.GameData
import com.royaal.entities.ui.UiGame
import com.royaal.entities.ui.mappers.GameUiMapper

internal class CollectionsViewModel(
    savedStateHandle: SavedStateHandle,
    private val repo: CollectionsRepository,
    private val gameUiMapper: Mapper<GameData, UiGame> = GameUiMapper(),
) : MviViewModel<
        CollectionsScreenState,
        CollectionsUserEvent,
        CollectionsInnerEvent,
        CollectionsSideEffect,
        ScreenSnapshot.NoSnapshot>(
    savedStateHandle = savedStateHandle,
    initialState = CollectionsScreenState(),
    reducer = CollectionsReducer(),
    snapshotHandler = SnapshotHandler.noSnapshot(CollectionsScreenState()),
) {

    init {
        subscription(COLLECTIONS_KEY) {
            bind(flow = repo.observeCollections(), policy = { retryForever() }) { collections ->
                CollectionsInnerEvent.UpdateCollections(collections.map { it.toUi() })
            }
        }
    }

    override fun onEvent(event: CollectionsUserEvent) {
        when (event) {
            is CollectionsUserEvent.CollectionClicked -> intent(event) {
                postEffect(CollectionsSideEffect.OpenCollection(event.key))
            }

            is CollectionsUserEvent.CreateCollection -> intent(event) {
                // The sheet validates too; this only guards against a bad name slipping through.
                val name = event.name.trim()
                val isValid = name.isNotEmpty() &&
                    name.length <= MAX_COLLECTION_NAME_LENGTH &&
                    name.lowercase() !in state.value.customNames
                if (isValid) repo.createCollection(name)
            }

            is CollectionsUserEvent.AddStatusCollections -> intent(event) {
                repo.addStatusCollections(event.statuses)
            }
        }
    }

    private fun CollectionSummary.toUi() = UiCollection(
        key = key,
        name = name,
        pinned = pinned,
        gamesCount = gamesCount,
        preview = preview.map(gameUiMapper::map),
    )

    private companion object {
        const val COLLECTIONS_KEY = "Collections"
    }
}
