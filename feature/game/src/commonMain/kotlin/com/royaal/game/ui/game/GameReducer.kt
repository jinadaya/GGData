package com.royaal.game.ui.game

import com.royaal.arch.mvi.MviReducer

internal class GameReducer : MviReducer<GameScreenState, GameInnerEvent> {
    override fun reduce(oldState: GameScreenState, event: GameInnerEvent): GameScreenState = when (event) {
        is GameInnerEvent.UpdateGame -> oldState.copy(
            game = event.game,
            developer = event.developer,
            publisher = event.publisher,
            heroImageUrl = event.heroImageUrl,
            seriesName = event.seriesName,
            isComplete = event.isComplete,
            isLoadFailed = false,
            // A game without a developer has no row for it to wait for.
            developerGames = if (event.isComplete && event.developer == null) {
                GameRow(isLoading = false)
            } else {
                oldState.developerGames
            },
        )

        // The rows need the game from IGDB: without it, there's nothing to wait for.
        GameInnerEvent.LoadFailed -> oldState.copy(
            isLoadFailed = true,
            series = oldState.series.copy(isLoading = false),
            similar = oldState.similar.copy(isLoading = false),
            developerGames = oldState.developerGames.copy(isLoading = false),
        )

        GameInnerEvent.Reload -> oldState.copy(
            isLoadFailed = false,
            series = GameRow(),
            similar = GameRow(),
            developerGames = GameRow(),
        )

        is GameInnerEvent.UpdateStats -> oldState.copy(stats = event.stats)
        is GameInnerEvent.UpdateSeries -> oldState.copy(series = GameRow(event.games, isLoading = false))
        is GameInnerEvent.UpdateSimilar -> oldState.copy(similar = GameRow(event.games, isLoading = false))
        is GameInnerEvent.UpdateDeveloperGames -> oldState.copy(
            developerGames = GameRow(event.games, isLoading = false),
        )
    }
}
