package com.royaal.journal.domain

import com.royaal.entities.data.GameData
import com.royaal.entities.data.GamePlayStatus
import kotlinx.coroutines.flow.Flow

/**
 * A game in the journal: it has a status. Times are epoch millis.
 *
 * @param rating the user's own, 1 to 10.
 * @param startedAt when it was first set playing, `null` if never.
 * @param completedAt when it was last set completed, `null` if never.
 */
internal data class JournalGame(
    val game: GameData,
    val status: GamePlayStatus,
    val hoursPlayed: Int?,
    val rating: Int?,
    val note: String?,
    val statusChangedAt: Long?,
    val startedAt: Long?,
    val completedAt: Long?,
)

internal interface JournalRepository {

    /** Every game with a status, the most recently changed first. */
    fun observeGames(): Flow<List<JournalGame>>
}
