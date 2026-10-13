package com.royaal.main.ui

import com.royaal.arch.mvi.InnerEvent
import com.royaal.arch.mvi.ScreenState
import com.royaal.arch.mvi.SideEffect
import com.royaal.arch.mvi.UserEvent
import com.royaal.entities.data.GamePlayStatus
import com.royaal.entities.ui.UiGame
import com.royaal.entities.ui.UiGameCollection
import com.royaal.entities.ui.UiGenre

internal data class MainScreenState(
    val title: String = "",
    /** Whether the user has opened a game, `null` until known; [latestGame] loads from the network after. */
    val hasLatestGame: Boolean? = null,
    val latestGame: UiGame? = null,
    val isLatestGameFailed: Boolean = false,
    val backlogGames: List<UiGame> = emptyList(),
    val isBacklogLoaded: Boolean = false,
    val collections: List<UiGameCollectionWithUiGames> = emptyList(),
    val isCollectionsLoaded: Boolean = false,
    val genres: List<UiGenre> = emptyList(),
    /** Genre [recommendations] are filtered by; `null` for the most popular games of any genre. */
    val selectedGenreId: Long? = null,
    val recommendations: List<UiGame> = emptyList(),
    val isGenresLoading: Boolean = true,
    val isRecommendationsLoading: Boolean = true,
    val isGenresFailed: Boolean = false,
    val isRecommendationsFailed: Boolean = false,
    /** Shown in the add-to-library sheet. */
    val addingGame: UiGame? = null,
    /** Shown in the details dialog. */
    val detailsGame: UiGame? = null,
) : ScreenState {
    /** Until the local library is read, it's unknown whether to show it or the onboarding. */
    val isLibraryLoading: Boolean
        get() = hasLatestGame == null || !isBacklogLoaded || !isCollectionsLoaded

    val isEmptyState: Boolean
        get() = !isLibraryLoading && hasLatestGame == false && backlogGames.isEmpty() && collections.isEmpty()

    val isLatestGameLoading: Boolean
        get() = hasLatestGame == true && latestGame == null && !isLatestGameFailed
}

/**
 * @param status set for a status collection: it's titled by the status, [UiGameCollection.title]
 * is empty.
 */
data class UiGameCollectionWithUiGames(
    val collection: UiGameCollection,
    val games: List<UiGame>,
    val status: GamePlayStatus? = null,
)

internal sealed interface MainUserEvent : UserEvent {
    data class SetLastAsFavourite(val favourite: Boolean) : MainUserEvent

    /** Selects the genre, or deselects it if it's already selected. */
    data class GenreClicked(val genreId: Long) : MainUserEvent

    /** Loads For You again after it failed. */
    data object RetryLatestGame : MainUserEvent

    /** Loads again what failed in the onboarding: genres, recommendations or both. */
    data object RetryRecommendations : MainUserEvent
    data class AddGameToFavClicked(val game: UiGame) : MainUserEvent

    /** "+" on a card: opens the add dialog. */
    data class AddGameClicked(val game: UiGame) : MainUserEvent

    /** Long tap on a card: opens the details dialog. */
    data class GameLongClicked(val game: UiGame) : MainUserEvent

    /** The add or the details dialog has closed. */
    data object DialogDismissed : MainUserEvent

    /** Tap on a card: opens the game's page. */
    data class GameClicked(val game: UiGame) : MainUserEvent
    data class CollectionClicked(val collection: UiGameCollection) : MainUserEvent
    data object SeeBacklog : MainUserEvent
    data object SeeCollections : MainUserEvent
}

internal sealed interface MainSideEffect : SideEffect {
    data class NavigateTo(val destination: NavDestination) : MainSideEffect
}

internal sealed interface NavDestination {
    data class Collection(val id: Long) : NavDestination
    data object Backlog : NavDestination
    data object AllCollections : NavDestination
    data class Game(val id: Long) : NavDestination
}

internal sealed interface MainInnerEvent : InnerEvent {
    data class UpdateHasLatestGame(val hasLatestGame: Boolean) : MainInnerEvent
    data class UpdateLatestGame(val game: UiGame) : MainInnerEvent
    data object LoadLatestGame : MainInnerEvent
    data object LatestGameFailed : MainInnerEvent
    data class UpdateBacklogGames(val games: List<UiGame>) : MainInnerEvent
    data class UpdateCollections(val collections: List<UiGameCollectionWithUiGames>) : MainInnerEvent
    data class UpdateGenres(val genres: List<UiGenre>) : MainInnerEvent
    data object LoadGenres : MainInnerEvent
    data object GenresFailed : MainInnerEvent
    data class RecommendationsFailed(val genreId: Long?) : MainInnerEvent
    data class SelectGenre(val genreId: Long?) : MainInnerEvent

    /** Also selects [genreId]: games cached for it are shown at once, with no loading in between. */
    data class UpdateRecommendations(val genreId: Long?, val games: List<UiGame>) : MainInnerEvent
    data class ShowAddDialog(val game: UiGame) : MainInnerEvent
    data class ShowDetailsDialog(val game: UiGame) : MainInnerEvent
    data object CloseDialogs : MainInnerEvent
}