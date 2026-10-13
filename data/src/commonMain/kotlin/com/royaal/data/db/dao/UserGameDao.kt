package com.royaal.data.db.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import com.royaal.data.db.GameWithCompanies
import com.royaal.data.db.entity.PlayStatus
import com.royaal.data.db.entity.UserGameEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserGameDao {

    @Upsert
    suspend fun upsert(userGame: UserGameEntity)

    @Query("SELECT * FROM user_game WHERE gameId = :gameId")
    suspend fun get(gameId: Long): UserGameEntity?

    @Query("SELECT * FROM user_game WHERE gameId = :gameId")
    fun observe(gameId: Long): Flow<UserGameEntity?>

    /** Cached games with the given [status], most recently changed first. */
    @Transaction
    @Query(
        """
        SELECT game.* FROM game
        INNER JOIN user_game ON user_game.gameId = game.id
        WHERE user_game.status = :status
        ORDER BY user_game.statusChangedAt DESC
        """
    )
    fun observeGamesByStatus(status: PlayStatus): Flow<List<GameWithCompanies>>

    /** Cached games with any status set, most recently changed first. Group by `userGame.status`. */
    @Transaction
    @Query(
        """
        SELECT game.* FROM game
        INNER JOIN user_game ON user_game.gameId = game.id
        WHERE user_game.status IS NOT NULL
        ORDER BY user_game.statusChangedAt DESC
        """
    )
    fun observeGamesWithAnyStatus(): Flow<List<GameWithCompanies>>

    @Transaction
    @Query(
        """
        SELECT game.* FROM game
        INNER JOIN user_game ON user_game.gameId = game.id
        WHERE user_game.favourite = 1
        ORDER BY user_game.updatedAt DESC
        """
    )
    fun observeFavouriteGames(): Flow<List<GameWithCompanies>>

    @Transaction
    suspend fun setFavourite(gameId: Long, favourite: Boolean, now: Long) {
        val current = get(gameId) ?: UserGameEntity(gameId = gameId, updatedAt = now)
        upsert(current.copy(favourite = favourite, updatedAt = now))
    }

    @Transaction
    suspend fun setStatus(gameId: Long, status: PlayStatus?, now: Long) {
        val current = get(gameId) ?: UserGameEntity(gameId = gameId, updatedAt = now)
        upsert(current.copy(status = status, statusChangedAt = now, updatedAt = now))
    }
}
