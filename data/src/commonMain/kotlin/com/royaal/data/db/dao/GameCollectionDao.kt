package com.royaal.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.royaal.data.db.entity.PlayStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface GameCollectionDao {

    @Insert
    suspend fun insert(collection: GameCollectionEntity): Long

    /** Skips collections whose status already has one: there is at most one per status. */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertIgnoringExisting(collections: List<GameCollectionEntity>)

    /** Pinned first, then oldest first. */
    @Query("SELECT * FROM game_collection ORDER BY pinned DESC, createdAt")
    fun observeAll(): Flow<List<GameCollectionEntity>>

    @Query("SELECT * FROM game_collection WHERE id = :id")
    fun observeById(id: Long): Flow<GameCollectionEntity?>

    @Query("SELECT * FROM game_collection WHERE status = :status")
    fun observeByStatus(status: PlayStatus): Flow<GameCollectionEntity?>

    @Transaction
    @Query("SELECT * FROM game_collection WHERE id = :id")
    suspend fun getWithGames(id: Long): GameCollectionWithGames?

    /** Pinned first, then oldest first. Status collections come with no games: theirs are by status. */
    @Transaction
    @Query("SELECT * FROM game_collection ORDER BY pinned DESC, createdAt")
    fun observeAllWithGames(): Flow<List<GameCollectionWithGames>>

    /** Games added to a user-created collection, most recently added first. */
    @Transaction
    @Query(
        """
        SELECT game.* FROM game
        INNER JOIN game_collection_cross_ref AS ref ON ref.gameId = game.id
        WHERE ref.collectionId = :collectionId
        ORDER BY ref.addedAt DESC
        """
    )
    fun observeGames(collectionId: Long): Flow<List<GameWithCompanies>>

    @Query("UPDATE game_collection SET pinned = :pinned WHERE id = :id")
    suspend fun setPinned(id: Long, pinned: Boolean)

    @Query("DELETE FROM game_collection WHERE id = :id")
    suspend fun deleteById(id: Long)
}
