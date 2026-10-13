package com.royaal.search.ui

import com.royaal.arch.mvi.MviReducer

internal class SearchReducer : MviReducer<SearchScreenState, SearchInnerEvent> {

    override fun reduce(
        oldState: SearchScreenState,
        event: SearchInnerEvent,
    ): SearchScreenState = when (event) {
        is SearchInnerEvent.UpdateQuery -> oldState.copy(
            query = event.query,
            games = if (event.query.isBlank()) emptyList() else oldState.games,
            isLoading = event.query.isNotBlank(),
            isRemoteFailed = false,
        )

        is SearchInnerEvent.UpdateResults -> oldState.copy(
            games = event.games,
            isLoading = event.isRemoteLoading,
            isRemoteFailed = event.isRemoteFailed,
        )
    }
}
