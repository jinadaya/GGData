package com.royaal.main.ui

import androidx.lifecycle.SavedStateHandle
import com.royaal.arch.mvi.ScreenSnapshot
import com.royaal.arch.mvi.SnapshotHandler
import com.royaal.arch.mvi.model.intent.ext.PolicyScope
import com.royaal.arch.mvi.model.intent.ext.policy
import com.royaal.arch.mvi.model.intent.ext.retryForever
import com.royaal.arch.mvi.viewmodel.MviViewModel
import com.royaal.entities.Mapper
import com.royaal.entities.data.GameData
import com.royaal.entities.data.GenreData
import com.royaal.entities.ui.UiGame
import com.royaal.entities.ui.UiGameCollection
import com.royaal.entities.ui.UiGenre
import com.royaal.entities.ui.mappers.GameUiMapper
import com.royaal.entities.ui.mappers.GenreUiMapper
import com.royaal.main.domain.MainScreenRepository
import kotlin.time.Duration.Companion.seconds

internal class MainScreenViewModel(
    savedStateHandle: SavedStateHandle,
    private val repo: MainScreenRepository,
    private val gameUiMapper: Mapper<GameData, UiGame> = GameUiMapper(),
    private val genreUiMapper: Mapper<GenreData, UiGenre> = GenreUiMapper(),
) : MviViewModel<
        MainScreenState,
        MainUserEvent,
        MainInnerEvent,
        MainSideEffect,
        ScreenSnapshot.NoSnapshot>(
    savedStateHandle = savedStateHandle,
    initialState = MainScreenState(),
    reducer = MainReducer(),
    snapshotHandler = SnapshotHandler.noSnapshot(MainScreenState())
) {

    init {
        intent(HAS_LATEST_GAME_KEY) {
            emit(MainInnerEvent.UpdateHasLatestGame(repo.hasLatestGame()))
        }
        loadLatestGame()
        subscription(BACKLOG_KEY) {
            bind(flow = repo.getBacklogGames(), policy = { retryForever() }) {
                // A glance at what's next; "See all" opens the whole list.
                val games = it.take(BACKLOG_PREVIEW_COUNT).map(gameUiMapper::map)
                MainInnerEvent.UpdateBacklogGames(games)
            }
        }
        subscription(COLLECTIONS_KEY) {
            bind(flow = repo.getCollections(), policy = { retryForever() }) {
                val collections = it.map { collection ->
                    val preview = collection.preview.map(gameUiMapper::map)
                    UiGameCollectionWithUiGames(
                        collection = UiGameCollection(
                            id = collection.id,
                            title = collection.name,
                            gamesCount = collection.gamesCount,
                            gameIds = preview.map { game -> game.id.toString() },
                        ),
                        games = preview,
                        status = collection.status,
                    )
                }
                MainInnerEvent.UpdateCollections(collections)
            }
        }
        loadGenres()
        loadRecommendations(genreId = null)
    }

    override fun onEvent(event: MainUserEvent) {
        when (event) {
            is MainUserEvent.SetLastAsFavourite -> intentLatest {
                state.value.latestGame?.let { latestGame ->
                    repo.toggleGameFavourite(latestGame.id, event.favourite)
                }
            }

            is MainUserEvent.GenreClicked -> {
                val genreId = event.genreId.takeIf { it != state.value.selectedGenreId }
                loadRecommendations(genreId)
            }

            MainUserEvent.RetryLatestGame -> {
                if (state.value.isLatestGameFailed) loadLatestGame()
            }

            MainUserEvent.RetryRecommendations -> {
                if (state.value.isGenresFailed) loadGenres()
                if (state.value.isRecommendationsFailed) loadRecommendations(state.value.selectedGenreId)
            }

            is MainUserEvent.AddGameToFavClicked -> intent(MainUserEvent.AddGameToFavClicked::class) {
                repo.toggleGameFavourite(event.game.id, !event.game.favourite)
            }

            is MainUserEvent.AddGameClicked -> intent(DIALOG_KEY) {
                emit(MainInnerEvent.ShowAddDialog(event.game))
            }

            is MainUserEvent.GameLongClicked -> intent(DIALOG_KEY) {
                emit(MainInnerEvent.ShowDetailsDialog(event.game))
            }

            MainUserEvent.DialogDismissed -> intent(DIALOG_KEY) {
                emit(MainInnerEvent.CloseDialogs)
            }

            is MainUserEvent.GameClicked -> intent(MainUserEvent.GameClicked::class) {
                postEffect(MainSideEffect.NavigateTo(NavDestination.Game(event.game.id)))
            }
            is MainUserEvent.CollectionClicked -> intent(MainUserEvent.CollectionClicked::class) {
                postEffect(MainSideEffect.NavigateTo(NavDestination.Collection(event.collection.id)))
            }
            MainUserEvent.SeeBacklog -> intent(MainUserEvent.SeeBacklog::class) {
                postEffect(MainSideEffect.NavigateTo(NavDestination.Backlog))
            }
            MainUserEvent.SeeCollections -> intent(MainUserEvent.SeeCollections::class) {
                postEffect(MainSideEffect.NavigateTo(NavDestination.AllCollections))
            }
        }
    }

    /** Restarts with the same key: Retry replaces a failed load. */
    private fun loadLatestGame() = subscription(FOR_YOU_KEY) {
        launch {
            emit(MainInnerEvent.LoadLatestGame)
            var isFailed = false
            var isFound = false
            policy({ networkPolicy(this); catch { isFailed = true } }) {
                repo.getLatestGame().collect {
                    isFound = true
                    emit(MainInnerEvent.UpdateLatestGame(gameUiMapper.map(it)))
                }
            }
            when {
                isFailed -> emit(MainInnerEvent.LatestGameFailed)
                // Not on IGDB anymore: as if no game was opened.
                !isFound -> emit(MainInnerEvent.UpdateHasLatestGame(false))
            }
        }
    }

    private fun loadGenres() = intent(GENRES_KEY) {
        emit(MainInnerEvent.LoadGenres)
        var genres: List<UiGenre>? = null
        policy(networkPolicy) {
            genres = repo.getGenres().map(genreUiMapper::map)
        }
        emit(genres?.let(MainInnerEvent::UpdateGenres) ?: MainInnerEvent.GenresFailed)
    }

    /** Shares the key with every other load, so the latest selection always wins. */
    private fun loadRecommendations(genreId: Long?) = intent(RECOMMENDATIONS_KEY) {
        repo.getCachedRecommendations(genreId)?.let { cached ->
            emit(MainInnerEvent.UpdateRecommendations(genreId, cached.map(gameUiMapper::map)))
            return@intent
        }
        emit(MainInnerEvent.SelectGenre(genreId))
        var games: List<UiGame>? = null
        policy(networkPolicy) {
            games = repo.getRecommendations(genreId).map(gameUiMapper::map)
        }
        emit(
            games?.let { MainInnerEvent.UpdateRecommendations(genreId, it) }
                ?: MainInnerEvent.RecommendationsFailed(genreId),
        )
    }

    private companion object {
        const val GENRES_KEY = "Genres"
        const val RECOMMENDATIONS_KEY = "Recommendations"
        const val DIALOG_KEY = "Dialog"
        private const val HAS_LATEST_GAME_KEY = "HasLatestGame"
        private const val FOR_YOU_KEY = "ForYou"
        private const val BACKLOG_KEY = "Backlog"
        private const val COLLECTIONS_KEY = "Collections"
        private const val BACKLOG_PREVIEW_COUNT = 3

        /** In the companion: `init` uses it before instance properties declared below it are set. */
        val networkPolicy: PolicyScope<MainInnerEvent>.() -> Unit = {
            retry(3)
            backoff(1.seconds, factor = 2.0, max = 8.seconds)
        }
    }
}