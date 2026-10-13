package com.royaal.game.ui.game

import com.royaal.arch.mvi.InnerEvent
import com.royaal.arch.mvi.ScreenState
import com.royaal.arch.mvi.SideEffect
import com.royaal.arch.mvi.UserEvent
import com.royaal.entities.data.CompanyData
import com.royaal.entities.data.GamePlayStatus
import com.royaal.entities.ui.UiGame
import com.royaal.game.domain.GameScreenshot

/**
 * A row of games under the details. Hidden once loaded empty.
 *
 * @param isLoading `true` until the game itself has come from IGDB and the row has loaded.
 */
internal data class GameRow(
    val games: List<UiGame> = emptyList(),
    val isLoading: Boolean = true,
) {
    val isVisible: Boolean
        get() = isLoading || games.isNotEmpty()
}

/**
 * @param statusSince when the status was set, formatted; `null` without a status.
 * @param collections names of the user's own collections the game is in.
 * @param rating the user's own, 1 to 10.
 */
internal data class UiPlayerStats(
    val status: GamePlayStatus? = null,
    val statusSince: String? = null,
    val favourite: Boolean = false,
    val collections: List<String> = emptyList(),
    val hoursPlayed: Int? = null,
    val rating: Int? = null,
    val note: String? = null,
) {
    /** Has a status, is a favourite or is in a collection: the journal's numbers don't count. */
    val isInLibrary: Boolean
        get() = status != null || favourite || collections.isNotEmpty()

    val isEmpty: Boolean
        get() = !isInLibrary && hoursPlayed == null && rating == null && note == null
}

/**
 * @param game `null` until loaded, from the cache or IGDB.
 * @param screenshots known once the game has come from IGDB.
 * @param isComplete the game has come from IGDB: the hero image, series and genre ids are known.
 * @param isLoadFailed IGDB couldn't be reached; whatever was cached stays shown.
 */
internal data class GameScreenState(
    val gameId: Long,
    val game: UiGame? = null,
    val developer: CompanyData? = null,
    val publisher: CompanyData? = null,
    val heroImageUrl: String? = null,
    val screenshots: List<GameScreenshot> = emptyList(),
    val seriesName: String? = null,
    val isComplete: Boolean = false,
    val isLoadFailed: Boolean = false,
    val stats: UiPlayerStats = UiPlayerStats(),
    val series: GameRow = GameRow(),
    val similar: GameRow = GameRow(),
    val developerGames: GameRow = GameRow(),
) : ScreenState

internal sealed interface GameUserEvent : UserEvent {
    data object Retry : GameUserEvent
    data object ToggleFavourite : GameUserEvent
    data class GameClicked(val gameId: Long) : GameUserEvent
    data object DeveloperClicked : GameUserEvent
}

internal sealed interface GameSideEffect : SideEffect {
    data class OpenGame(val gameId: Long) : GameSideEffect
    data class OpenCompany(val companyId: Long) : GameSideEffect
}

internal sealed interface GameInnerEvent : InnerEvent {
    data class UpdateGame(
        val game: UiGame,
        val developer: CompanyData?,
        val publisher: CompanyData?,
        val heroImageUrl: String?,
        val screenshots: List<GameScreenshot>,
        val seriesName: String?,
        val isComplete: Boolean,
    ) : GameInnerEvent

    data object LoadFailed : GameInnerEvent
    data object Reload : GameInnerEvent
    data class UpdateStats(val stats: UiPlayerStats) : GameInnerEvent
    data class UpdateSeries(val games: List<UiGame>) : GameInnerEvent
    data class UpdateSimilar(val games: List<UiGame>) : GameInnerEvent
    data class UpdateDeveloperGames(val games: List<UiGame>) : GameInnerEvent
}
