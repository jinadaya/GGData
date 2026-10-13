package com.royaal.journal.ui

import androidx.lifecycle.SavedStateHandle
import com.royaal.arch.mvi.MviReducer
import com.royaal.arch.mvi.ScreenSnapshot
import com.royaal.arch.mvi.SnapshotHandler
import com.royaal.arch.mvi.model.intent.ext.retryForever
import com.royaal.arch.mvi.viewmodel.MviViewModel
import com.royaal.entities.Mapper
import com.royaal.entities.data.GameData
import com.royaal.entities.ui.UiGame
import com.royaal.entities.ui.mappers.GameUiMapper
import com.royaal.journal.domain.JournalGame
import com.royaal.journal.domain.JournalRepository
import com.royaal.journal.domain.statsSince
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock

internal class JournalViewModel(
    savedStateHandle: SavedStateHandle,
    private val repo: JournalRepository,
    private val gameUiMapper: Mapper<GameData, UiGame> = GameUiMapper(),
) : MviViewModel<
        JournalScreenState,
        JournalUserEvent,
        JournalInnerEvent,
        JournalSideEffect,
        ScreenSnapshot.NoSnapshot,
        >(
            savedStateHandle = savedStateHandle,
            initialState = JournalScreenState(),
            reducer = JournalReducer(),
            snapshotHandler = SnapshotHandler.noSnapshot(JournalScreenState()),
        ) {

    init {
        subscription(GAMES_KEY) {
            bind(flow = repo.observeGames(), policy = { retryForever() }) { games ->
                // Taken anew with every change: the month may have turned since the last one.
                val timeZone = TimeZone.currentSystemDefault()
                val today = Clock.System.now().toLocalDateTime(timeZone).date
                val monthStart = LocalDate(today.year, today.month, 1).atStartOfDayIn(timeZone)
                val yearStart = LocalDate(today.year, 1, 1).atStartOfDayIn(timeZone)
                JournalInnerEvent.Update(
                    games = games.map { it.toUi() },
                    monthStats = games.statsSince(monthStart.toEpochMilliseconds()),
                    yearStats = games.statsSince(yearStart.toEpochMilliseconds()),
                )
            }
        }
    }

    override fun onEvent(event: JournalUserEvent) {
        when (event) {
            is JournalUserEvent.StatusClicked -> intent(event) { emit(JournalInnerEvent.SelectStatus(event.status)) }
            is JournalUserEvent.PeriodClicked -> intent(event) { emit(JournalInnerEvent.SelectPeriod(event.period)) }
        }
    }

    private fun JournalGame.toUi() = UiJournalGame(
        game = gameUiMapper.map(game),
        status = status,
        hoursPlayed = hoursPlayed,
        rating = rating,
        note = note,
    )

    private companion object {
        const val GAMES_KEY = "Games"
    }
}

private class JournalReducer : MviReducer<JournalScreenState, JournalInnerEvent> {
    override fun reduce(oldState: JournalScreenState, event: JournalInnerEvent): JournalScreenState = when (event) {
        is JournalInnerEvent.Update -> oldState.copy(
            games = event.games,
            monthStats = event.monthStats,
            yearStats = event.yearStats,
        )

        is JournalInnerEvent.SelectStatus -> oldState.copy(selectedStatus = event.status)
        is JournalInnerEvent.SelectPeriod -> oldState.copy(period = event.period)
    }
}
