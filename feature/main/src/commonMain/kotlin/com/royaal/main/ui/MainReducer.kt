package com.royaal.main.ui

import com.royaal.arch.mvi.MviReducer

internal class MainReducer : MviReducer<MainScreenState, MainInnerEvent> {

    override fun reduce(
        oldState: MainScreenState,
        event: MainInnerEvent
    ): MainScreenState = when(event) {
        is MainInnerEvent.UpdateBacklogGames -> oldState.copy(
            backlogGames = event.games,
            isBacklogLoaded = true,
        )
        is MainInnerEvent.UpdateCollections -> oldState.copy(
            collections = event.collections,
            isCollectionsLoaded = true,
        )
        is MainInnerEvent.UpdateHasLatestGame -> oldState.copy(hasLatestGame = event.hasLatestGame)
        is MainInnerEvent.UpdateLatestGame -> oldState.copy(
            latestGame = event.game,
            isLatestGameFailed = false,
        )
        MainInnerEvent.LoadLatestGame -> oldState.copy(isLatestGameFailed = false)
        MainInnerEvent.LatestGameFailed -> oldState.copy(isLatestGameFailed = true)
        is MainInnerEvent.UpdateGenres -> oldState.copy(
            genres = event.genres,
            isGenresLoading = false,
            isGenresFailed = false,
        )
        MainInnerEvent.LoadGenres -> oldState.copy(
            isGenresLoading = true,
            isGenresFailed = false,
        )
        MainInnerEvent.GenresFailed -> oldState.copy(
            isGenresLoading = false,
            isGenresFailed = true,
        )
        is MainInnerEvent.SelectGenre -> oldState.copy(
            selectedGenreId = event.genreId,
            isRecommendationsLoading = true,
            isRecommendationsFailed = false,
        )
        is MainInnerEvent.UpdateRecommendations -> oldState.copy(
            selectedGenreId = event.genreId,
            recommendations = event.games,
            isRecommendationsLoading = false,
            isRecommendationsFailed = false,
        )
        is MainInnerEvent.RecommendationsFailed -> oldState.copy(
            selectedGenreId = event.genreId,
            recommendations = emptyList(),
            isRecommendationsLoading = false,
            isRecommendationsFailed = true,
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