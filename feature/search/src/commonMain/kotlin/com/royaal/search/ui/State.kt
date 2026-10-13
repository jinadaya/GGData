package com.royaal.search.ui

import com.royaal.arch.mvi.InnerEvent
import com.royaal.arch.mvi.ScreenState
import com.royaal.arch.mvi.SideEffect
import com.royaal.arch.mvi.UserEvent
import com.royaal.entities.ui.UiGame

/**
 * @param isLoading the query is waiting for debounce or for IGDB; cached results may already be shown.
 * @param isRemoteFailed IGDB couldn't be reached, [games] are cached ones only.
 */
internal data class SearchScreenState(
    val query: String = "",
    val games: List<UiGame> = emptyList(),
    val isLoading: Boolean = false,
    val isRemoteFailed: Boolean = false,
) : ScreenState {
    val isNothingFound: Boolean
        get() = query.isNotBlank() && !isLoading && games.isEmpty()

    /** Nothing searched yet: no query and no results. */
    val isIdle: Boolean
        get() = query.isBlank() && games.isEmpty()
}

internal sealed interface SearchUserEvent : UserEvent {
    data class QueryChanged(val query: String) : SearchUserEvent
}

internal sealed interface SearchSideEffect : SideEffect

internal sealed interface SearchInnerEvent : InnerEvent {
    data class UpdateQuery(val query: String) : SearchInnerEvent
    data class UpdateResults(
        val games: List<UiGame>,
        val isRemoteLoading: Boolean,
        val isRemoteFailed: Boolean,
    ) : SearchInnerEvent
}
