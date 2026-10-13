package com.royaal.game.ui.company

import androidx.lifecycle.SavedStateHandle
import com.royaal.arch.mvi.MviReducer
import com.royaal.arch.mvi.ScreenSnapshot
import com.royaal.arch.mvi.SnapshotHandler
import com.royaal.arch.mvi.viewmodel.MviViewModel
import com.royaal.entities.Mapper
import com.royaal.entities.data.GameData
import com.royaal.entities.ui.UiGame
import com.royaal.entities.ui.mappers.GameUiMapper
import com.royaal.game.domain.GameRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

internal class CompanyViewModel(
    savedStateHandle: SavedStateHandle,
    private val companyId: Long,
    private val repo: GameRepository,
    private val gameUiMapper: Mapper<GameData, UiGame> = GameUiMapper(),
) : MviViewModel<
        CompanyScreenState,
        CompanyUserEvent,
        CompanyInnerEvent,
        CompanySideEffect,
        ScreenSnapshot.NoSnapshot,
        >(
            savedStateHandle = savedStateHandle,
            initialState = CompanyScreenState(companyId),
            reducer = CompanyReducer(),
            snapshotHandler = SnapshotHandler.noSnapshot(CompanyScreenState(companyId)),
        ) {

    init {
        load()
    }

    override fun onEvent(event: CompanyUserEvent) {
        when (event) {
            CompanyUserEvent.Retry -> load()
            is CompanyUserEvent.GameClicked -> intent(event) {
                postEffect(CompanySideEffect.OpenGame(event.gameId))
            }
        }
    }

    private fun load() = intent(LOAD_KEY) {
        emit(CompanyInnerEvent.Loading)
        try {
            val (company, games) = coroutineScope {
                val company = async { repo.getCompany(companyId) }
                val games = async { repo.getDeveloperGames(companyId, limit = GAMES_LIMIT) }
                company.await() to games.await()
            }
            if (company == null) {
                emit(CompanyInnerEvent.LoadFailed)
            } else {
                emit(CompanyInnerEvent.Loaded(company, games.map(gameUiMapper::map)))
            }
        } catch (e: CancellationException) {
            throw e
        } catch (_: Exception) {
            emit(CompanyInnerEvent.LoadFailed)
        }
    }

    private companion object {
        const val LOAD_KEY = "Load"
        const val GAMES_LIMIT = 50
    }
}

private class CompanyReducer : MviReducer<CompanyScreenState, CompanyInnerEvent> {
    override fun reduce(oldState: CompanyScreenState, event: CompanyInnerEvent): CompanyScreenState = when (event) {
        CompanyInnerEvent.Loading -> oldState.copy(isLoading = true, isLoadFailed = false)
        is CompanyInnerEvent.Loaded -> oldState.copy(
            company = event.company,
            games = event.games,
            isLoading = false,
        )
        CompanyInnerEvent.LoadFailed -> oldState.copy(isLoading = false, isLoadFailed = true)
    }
}
