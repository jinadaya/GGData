package com.royaal.collections.ui.details

import com.royaal.arch.mvi.MviReducer

internal class CollectionReducer : MviReducer<CollectionScreenState, CollectionInnerEvent> {
    override fun reduce(
        oldState: CollectionScreenState,
        event: CollectionInnerEvent
    ): CollectionScreenState = when (event) {
        is CollectionInnerEvent.Update -> oldState.copy(
            key = event.key,
            title = event.title,
            collectionId = event.collectionId,
            pinned = event.pinned,
            games = event.games,
            isLoading = false,
        )
    }
}
