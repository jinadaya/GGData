package com.royaal.game.ui.game

import androidx.lifecycle.SavedStateHandle
import com.royaal.arch.mvi.ScreenSnapshot
import com.royaal.arch.mvi.SnapshotHandler
import com.royaal.arch.mvi.viewmodel.MviViewModel
import com.royaal.entities.Mapper
import com.royaal.entities.data.GameData
import com.royaal.entities.ui.UiGame
import com.royaal.entities.ui.mappers.GameUiMapper
import com.royaal.game.domain.GameDetails
import com.royaal.game.domain.GameRepository
import com.royaal.game.domain.PlayerStats
import kotlinx.coroutines.CancellationException
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.format
import kotlinx.datetime.format.MonthNames
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Instant

internal class GameViewModel(
    savedStateHandle: SavedStateHandle,
    private val gameId: Long,
    private val repo: GameRepository,
    private val gameUiMapper: Mapper<GameData, UiGame> = GameUiMapper(),
) : MviViewModel<
        GameScreenState,
        GameUserEvent,
        GameInnerEvent,
        GameSideEffect,
        ScreenSnapshot.NoSnapshot,
        >(
            savedStateHandle = savedStateHandle,
            initialState = GameScreenState(gameId),
            reducer = GameReducer(),
            snapshotHandler = SnapshotHandler.noSnapshot(GameScreenState(gameId)),
        ) {

    init {
        loadGame()
        subscription(STATS_KEY) {
            launch {
                repo.observePlayerStats(gameId).collect { emit(GameInnerEvent.UpdateStats(it.toUi())) }
            }
        }
        intent(LAST_OPENED_KEY) { repo.setLastOpened(gameId) }
    }

    override fun onEvent(event: GameUserEvent) {
        when (event) {
            GameUserEvent.Retry -> intent(event) {
                emit(GameInnerEvent.Reload)
                loadGame()
            }

            GameUserEvent.ToggleFavourite -> intent(event) {
                repo.setFavourite(gameId, !state.value.stats.favourite)
            }

            is GameUserEvent.GameClicked -> intent(event) {
                postEffect(GameSideEffect.OpenGame(event.gameId))
            }

            GameUserEvent.DeveloperClicked -> intent(event) {
                val developer = state.value.developer ?: return@intent
                postEffect(GameSideEffect.OpenCompany(developer.id))
            }
        }
    }

    /** Restarts with the same key: Retry replaces a failed load. */
    private fun loadGame() = subscription(GAME_KEY) {
        launch {
            try {
                repo.observeGame(gameId).collect { details ->
                    emit(details.toUpdate())
                    if (details.isComplete) loadRows(details)
                }
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                emit(GameInnerEvent.LoadFailed)
            }
        }
    }

    /** Each row on its own: a failed one is just hidden, the others still show. */
    private fun loadRows(details: GameDetails) = intent(ROWS_KEY) {
        launch {
            val developerGames = details.developer?.let { developer ->
                loadOrEmpty { repo.getDeveloperGames(developer.id, excludeGameId = gameId, limit = ROW_LIMIT) }
            }
            if (developerGames != null) emit(GameInnerEvent.UpdateDeveloperGames(developerGames))
        }
        val series = loadOrEmpty { repo.getSeriesGames(details) }
        emit(GameInnerEvent.UpdateSeries(series))
        // After the series: its games aren't repeated among the similar ones.
        val similar = loadOrEmpty { repo.getSimilarGames(details, excludeGameIds = series.mapTo(mutableSetOf()) { it.id }) }
        emit(GameInnerEvent.UpdateSimilar(similar))
    }

    private suspend fun loadOrEmpty(load: suspend () -> List<GameData>): List<UiGame> = try {
        load().map(gameUiMapper::map)
    } catch (e: CancellationException) {
        throw e
    } catch (_: Exception) {
        emptyList()
    }

    private fun GameDetails.toUpdate() = GameInnerEvent.UpdateGame(
        game = gameUiMapper.map(game),
        developer = developer,
        publisher = publisher?.takeIf { it.id != developer?.id },
        heroImageUrl = heroImageUrl,
        seriesName = seriesName,
        isComplete = isComplete,
    )

    private fun PlayerStats.toUi() = UiPlayerStats(
        status = status,
        statusSince = statusChangedAt?.let { millis ->
            Instant.fromEpochMilliseconds(millis)
                .toLocalDateTime(TimeZone.currentSystemDefault())
                .date
                .format(sinceFormat)
        },
        favourite = favourite,
        collections = collections,
    )

    private companion object {
        const val GAME_KEY = "Game"
        const val STATS_KEY = "Stats"
        const val ROWS_KEY = "Rows"
        const val LAST_OPENED_KEY = "LastOpened"
        const val ROW_LIMIT = 15

        /** `12 Nov 2025`, like release dates. */
        val sinceFormat = LocalDate.Format {
            day()
            chars(" ")
            monthName(MonthNames.ENGLISH_ABBREVIATED)
            chars(" ")
            year()
        }
    }
}
