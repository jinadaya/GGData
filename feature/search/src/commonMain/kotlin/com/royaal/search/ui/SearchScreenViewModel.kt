package com.royaal.search.ui

import androidx.lifecycle.SavedStateHandle
import com.royaal.arch.mvi.ScreenSnapshot
import com.royaal.arch.mvi.SnapshotHandler
import com.royaal.arch.mvi.viewmodel.MviViewModel
import com.royaal.entities.Mapper
import com.royaal.entities.data.GameData
import com.royaal.entities.ui.UiGame
import com.royaal.entities.ui.mappers.GameUiMapper
import com.royaal.search.domain.RemoteSearchStatus
import com.royaal.search.domain.SearchRepository
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds

internal class SearchScreenViewModel(
    savedStateHandle: SavedStateHandle,
    private val repo: SearchRepository,
    private val gameUiMapper: Mapper<GameData, UiGame> = GameUiMapper(),
) : MviViewModel<
        SearchScreenState,
        SearchUserEvent,
        SearchInnerEvent,
        SearchSideEffect,
        ScreenSnapshot.NoSnapshot>(
    savedStateHandle = savedStateHandle,
    initialState = SearchScreenState(),
    reducer = SearchReducer(),
    snapshotHandler = SnapshotHandler.noSnapshot(SearchScreenState()),
) {

    override fun onEvent(event: SearchUserEvent) {
        when (event) {
            is SearchUserEvent.QueryChanged -> intent(SearchUserEvent.QueryChanged::class) {
                emit(SearchInnerEvent.UpdateQuery(event.query))
                if (event.query.isNotBlank()) delay(DEBOUNCE)
                repo.search(event.query).collect { result ->
                    emit(
                        SearchInnerEvent.UpdateResults(
                            games = result.games.map(gameUiMapper::map),
                            isRemoteLoading = result.remoteStatus == RemoteSearchStatus.LOADING,
                            isRemoteFailed = result.remoteStatus == RemoteSearchStatus.FAILED,
                        )
                    )
                }
            }
        }
    }

    private companion object {
        val DEBOUNCE = 300.milliseconds
    }
}
