package com.royaal.game.domain

import com.royaal.entities.data.GameData
import com.royaal.entities.data.GamePlayStatus
import kotlinx.coroutines.flow.Flow

internal interface GameRepository {

    /**
     * The cached game first, if there's one, then the complete one from IGDB, which is cached
     * too. Throws if IGDB can't be reached, after emitting the cached one.
     */
    fun observeGame(gameId: Long): Flow<GameDetails>

    /** Other games of the same series (or franchise), oldest first. */
    suspend fun getSeriesGames(details: GameDetails): List<GameData>

    /**
     * The most popular games sharing the game's genres, without [excludeGameIds] (e.g. the series',
     * which have a row of their own).
     */
    suspend fun getSimilarGames(details: GameDetails, excludeGameIds: Set<Long>): List<GameData>

    /** The most popular games [companyId] developed, without [excludeGameId]. */
    suspend fun getDeveloperGames(companyId: Long, excludeGameId: Long? = null, limit: Int): List<GameData>

    suspend fun getCompany(companyId: Long): CompanyDetails?

    fun observePlayerStats(gameId: Long): Flow<PlayerStats>

    suspend fun setFavourite(gameId: Long, favourite: Boolean)

    /** Remembers the game as the last one opened, for "For you" on Discover. */
    suspend fun setLastOpened(gameId: Long)

    fun observeLibraryEntry(gameId: Long): Flow<GameLibraryEntry>

    /** Sets the game's status and favourite, and leaves it in exactly [collectionIds]. */
    suspend fun saveLibraryEntry(
        gameId: Long,
        status: GamePlayStatus?,
        favourite: Boolean,
        collectionIds: Set<Long>,
    )
}
