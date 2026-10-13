package com.royaal.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import com.royaal.data.db.entity.GameCollectionCrossRef
import kotlinx.coroutines.flow.Flow

@Dao
interface GameCollectionCrossRefDao {

    @Insert
    suspend fun addGameToCollection(crossRef: GameCollectionCrossRef)

    @Query("SELECT * FROM game_collection_cross_ref WHERE gameId = :gameId AND collectionId = :collectionId")
    suspend fun get(gameId: Long, collectionId: Long): GameCollectionCrossRef?

    @Query("DELETE FROM game_collection_cross_ref WHERE gameId = :gameId AND collectionId = :collectionId")
    suspend fun removeGameFromCollection(gameId: Long, collectionId: Long)

    @Query("SELECT collectionId FROM game_collection_cross_ref WHERE gameId = :gameId")
    suspend fun getCollectionIds(gameId: Long): List<Long>

    @Query("SELECT collectionId FROM game_collection_cross_ref WHERE gameId = :gameId")
    fun observeCollectionIds(gameId: Long): Flow<List<Long>>

    /**
     * Leaves the game in exactly [collectionIds]. Collections it stays in keep their `addedAt`,
     * so it doesn't jump to the top of them.
     */
    @Transaction
    suspend fun setGameCollections(gameId: Long, collectionIds: Set<Long>, now: Long) {
        val current = getCollectionIds(gameId).toSet()
        (current - collectionIds).forEach { removeGameFromCollection(gameId, it) }
        (collectionIds - current).forEach {
            addGameToCollection(GameCollectionCrossRef(gameId = gameId, collectionId = it, addedAt = now))
        }
    }
}
