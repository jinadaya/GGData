package com.royaal.data.db

import androidx.room.Dao
import androidx.room.Query
import androidx.room.RawQuery
import androidx.room.RoomRawQuery
import androidx.room.Transaction
import androidx.room.Upsert
import com.royaal.data.db.entity.UserGameEntity
import com.royaal.data.db.search.GameSearchText
import kotlinx.coroutines.flow.Flow

@Dao
interface GameDao {

    @Upsert
    suspend fun upsert(game: GameEntity)

    @Upsert
    suspend fun upsertAll(games: List<GameEntity>)

    @Upsert
    suspend fun upsertCompanies(companies: List<CompanyEntity>)

    /**
     * Caches games fetched from IGDB along with their [companies]. Fields a fetched game lacks
     * keep their cached values, see [mergedWith].
     */
    @Transaction
    suspend fun cacheGames(games: List<GameEntity>, companies: List<CompanyEntity>) {
        upsertCompanies(companies)
        val cached = getByIds(games.map { it.id }).associateBy { it.id }
        upsertAll(games.map { game -> cached[game.id]?.let(game::mergedWith) ?: game })
    }

    @Query("SELECT * FROM game WHERE id = :id")
    suspend fun getById(id: Long): GameEntity?

    @Query("SELECT * FROM game WHERE id IN (:ids)")
    suspend fun getByIds(ids: List<Long>): List<GameEntity>

    @Query("SELECT * FROM game")
    fun observeAll(): Flow<List<GameEntity>>

    @Transaction
    @Query("SELECT * FROM game WHERE id IN (:ids)")
    fun observeWithCompanies(ids: List<Long>): Flow<List<GameWithCompanies>>

    /** Games that don't belong to any collection yet. */
    @Query("SELECT * FROM game WHERE id NOT IN (SELECT gameId FROM game_collection_cross_ref)")
    fun observeUncategorized(): Flow<List<GameEntity>>

    /**
     * Games where every query word starts some word of the title or an alternative name.
     * [match] comes from [GameSearchText.prefixMatch]; titles starting with [titlePrefix] (the
     * normalized query followed by `%`) go first, then the best rated.
     */
    @Transaction
    @Query(
        """
        SELECT game.* FROM game
        INNER JOIN game_fts ON game_fts.rowid = game.id
        WHERE game_fts MATCH :match
        ORDER BY game.searchText LIKE :titlePrefix DESC, game.rating IS NULL, game.rating DESC, game.name
        LIMIT :limit
        """
    )
    fun observeMatchingWordPrefixes(match: String, titlePrefix: String, limit: Int): Flow<List<GameWithCompanies>>

    /**
     * Games whose normalized names contain every one of [queryWords] anywhere, not only at a word
     * start (`craft` → Minecraft). A full scan, unlike [observeMatchingWordPrefixes].
     */
    fun observeContainingAll(queryWords: List<String>, limit: Int): Flow<List<GameWithCompanies>> {
        val sql = buildString {
            append("SELECT * FROM game WHERE ")
            queryWords.joinTo(this, separator = " AND ") { "instr(searchText, ?) > 0" }
            append(" ORDER BY rating IS NULL, rating DESC, name LIMIT ?")
        }
        return observeRaw(
            RoomRawQuery(sql) { statement ->
                queryWords.forEachIndexed { index, word -> statement.bindText(index + 1, word) }
                statement.bindLong(queryWords.size + 1, limit.toLong())
            }
        )
    }

    @Transaction
    @RawQuery(observedEntities = [GameEntity::class, CompanyEntity::class, UserGameEntity::class])
    fun observeRaw(query: RoomRawQuery): Flow<List<GameWithCompanies>>

    @Transaction
    @Query("SELECT * FROM game WHERE id = :gameId")
    suspend fun getWithCollections(gameId: Long): GameWithCollections?

    @Transaction
    @Query("SELECT * FROM game WHERE id = :id")
    suspend fun getWithCompanies(id: Long): GameWithCompanies?

    @Query("DELETE FROM game WHERE id = :id")
    suspend fun deleteById(id: Long)
}
