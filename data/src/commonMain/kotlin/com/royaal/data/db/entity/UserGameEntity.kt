package com.royaal.data.db.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/** Where the user is with a game. `null` in [UserGameEntity.status] means "no status set". */
enum class PlayStatus { BACKLOG, PLAYING, COMPLETED, DROPPED }

/**
 * Everything the user has set on a game (status, favourite, ...), kept apart from the IGDB cache in
 * [com.royaal.data.db.GameEntity] so that re-fetching or evicting cached games never touches user data.
 *
 * Deliberately has no foreign key to `game`: a game can be marked before it is cached, and joins
 * simply skip it until the cache row appears. Cache cleanup should keep games referenced here.
 *
 * @param gameId IGDB game id, same as [com.royaal.data.db.GameEntity.id].
 * @param statusChangedAt epoch millis [status] was last changed, for ordering status lists.
 * @param updatedAt epoch millis this row was last written.
 * @param hoursPlayed hours the user says they've put in.
 * @param rating the user's own rating, 1 to 10.
 * @param note the user's own words on the game.
 * @param startedAt epoch millis the game was first set [PlayStatus.PLAYING], for the journal's stats.
 * @param completedAt epoch millis the game was last set [PlayStatus.COMPLETED], for the journal's stats.
 */
@Entity(
    tableName = "user_game",
    indices = [Index("status"), Index("favourite")],
)
data class UserGameEntity(
    @PrimaryKey val gameId: Long,
    val status: PlayStatus? = null,
    val favourite: Boolean = false,
    val statusChangedAt: Long? = null,
    val updatedAt: Long,
    val hoursPlayed: Int? = null,
    val rating: Int? = null,
    val note: String? = null,
    val startedAt: Long? = null,
    val completedAt: Long? = null,
)
