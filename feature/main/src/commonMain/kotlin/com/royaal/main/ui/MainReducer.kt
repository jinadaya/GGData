package com.royaal.main.ui

import com.royaal.arch.mvi.MviReducer

internal class MainReducer : MviReducer<MainScreenState, MainInnerEvent> {

    override fun reduce(
        oldState: MainScreenState,
        event: MainInnerEvent
    ): MainScreenState = when(event) {
        is MainInnerEvent.UpdateBacklogGames -> oldState.copy(backlogGames = event.games)
        is MainInnerEvent.UpdateCollections -> oldState.copy(collections = event.collections)
        is MainInnerEvent.UpdateLatestGame -> oldState.copy(latestGame = event.game)
        is MainInnerEvent.UpdateGenres -> oldState.copy(
            genres = event.genres,
            isGenresLoading = false,
        )
        is MainInnerEvent.SelectGenre -> oldState.copy(
            selectedGenreId = event.genreId,
            isRecommendationsLoading = true,
        )
        is MainInnerEvent.UpdateRecommendations -> oldState.copy(
            selectedGenreId = event.genreId,
            recommendations = event.games,
            isRecommendationsLoading = false,
        )
        is MainInnerEvent.ShowAddDialog -> oldState.copy(
            addingGame = event.game,
            detailsGame = null,
        )
        is MainInnerEvent.ShowDetailsDialog -> oldState.copy(
            detailsGame = event.game,
            addingGame = null,
        )
        MainInnerEvent.CloseDialogs -> oldState.copy(
            addingGame = null,
            detailsGame = null,
        )
    }
}