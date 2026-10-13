package com.royaal.journal.ui

import com.royaal.arch.mvi.InnerEvent
import com.royaal.arch.mvi.ScreenState
import com.royaal.arch.mvi.SideEffect
import com.royaal.arch.mvi.UserEvent
import com.royaal.entities.data.GamePlayStatus
import com.royaal.entities.ui.UiGame
import com.royaal.game.ui.common.JournalStatuses
import com.royaal.journal.domain.JournalStats

internal enum class StatsPeriod { MONTH, YEAR }

/** @param rating the user's own, 1 to 10. */
internal data class UiJournalGame(
    val game: UiGame,
    val status: GamePlayStatus,
    val hoursPlayed: Int?,
    val rating: Int?,
    val note: String?,
)

/**
 * @param games `null` until read from the database.
 * @param selectedStatus picked by the user; until then, the first status that has games.
 */
internal data class JournalScreenState(
    val games: List<UiJournalGame>? = null,
    val selectedStatus: GamePlayStatus? = null,
    val period: StatsPeriod = StatsPeriod.MONTH,
    val monthStats: JournalStats = JournalStats(0, 0, 0, 0),
    val yearStats: JournalStats = JournalStats(0, 0, 0, 0),
) : ScreenState {
    val isLoading: Boolean
        get() = games == null

    val isEmpty: Boolean
        get() = games?.isEmpty() == true

    val shownStatus: GamePlayStatus
        get() = selectedStatus
            ?: JournalStatuses.firstOrNull { status -> games.orEmpty().any { it.status == status } }
            ?: JournalStatuses.first()

    val shownGames: List<UiJournalGame>
        get() = games.orEmpty().filter { it.status == shownStatus }

    val stats: JournalStats
        get() = when (period) {
            StatsPeriod.MONTH -> monthStats
            StatsPeriod.YEAR -> yearStats
        }

    fun count(status: GamePlayStatus): Int = games.orEmpty().count { it.status == status }
}

internal sealed interface JournalUserEvent : UserEvent {
    data class StatusClicked(val status: GamePlayStatus) : JournalUserEvent
    data class PeriodClicked(val period: StatsPeriod) : JournalUserEvent
}

internal sealed interface JournalSideEffect : SideEffect

internal sealed interface JournalInnerEvent : InnerEvent {
    data class Update(
        val games: List<UiJournalGame>,
        val monthStats: JournalStats,
        val yearStats: JournalStats,
    ) : JournalInnerEvent

    data class SelectStatus(val status: GamePlayStatus) : JournalInnerEvent
    data class SelectPeriod(val period: StatsPeriod) : JournalInnerEvent
}
