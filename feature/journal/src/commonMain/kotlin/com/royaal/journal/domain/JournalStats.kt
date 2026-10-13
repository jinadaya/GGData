package com.royaal.journal.domain

import com.royaal.entities.data.GamePlayStatus

/**
 * What the user did over a period.
 *
 * @param hours the hours of the games completed or dropped in it: hours aren't dated, so only
 *   the games that were finished in the period count.
 */
internal data class JournalStats(
    val started: Int,
    val completed: Int,
    val dropped: Int,
    val hours: Int,
)

/** Stats of the games since [since], epoch millis. Only a game's current status counts. */
internal fun List<JournalGame>.statsSince(since: Long): JournalStats {
    val completed = filter { it.status == GamePlayStatus.COMPLETED && it.completedAt.isSince(since) }
    val dropped = filter { it.status == GamePlayStatus.DROPPED && it.statusChangedAt.isSince(since) }
    return JournalStats(
        started = count { it.startedAt.isSince(since) },
        completed = completed.size,
        dropped = dropped.size,
        hours = (completed + dropped).sumOf { it.hoursPlayed ?: 0 },
    )
}

private fun Long?.isSince(since: Long): Boolean = this != null && this >= since
