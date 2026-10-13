package com.royaal.collections.ui.list

import com.royaal.arch.mvi.MviReducer

internal class CollectionsReducer : MviReducer<CollectionsScreenState, CollectionsInnerEvent> {

    override fun reduce(
        oldState: CollectionsScreenState,
        event: CollectionsInnerEvent,
    ): CollectionsScreenState = when (event) {
        is CollectionsInnerEvent.UpdateCollections -> oldState.copy(
            collections = event.collections,
            isLoading = false,
        )
    }
}
