package com.royaal.main.domain

import com.royaal.entities.data.GameData
import com.royaal.entities.data.GenreData
import kotlinx.coroutines.flow.Flow

internal interface MainScreenRepository {

    fun getLatestGame(): Flow<GameData>

    /**
     * The user's collections that have games, pinned first, then oldest first. A status collection
     * has the games with its status.
     */
    fun getCollections(): Flow<List<DiscoverCollection>>

    fun getBacklogGames(): Flow<List<GameData>>

    /**
     * The most popular games released within the last year, of the given genre if [genreId] is
     * set. Popularity is the number of ratings on IGDB. Kept in memory for a while, see
     * [getCachedRecommendations].
     */
    suspend fun getRecommendations(genreId: Long?): List<GameData>

    /** What [getRecommendations] returned for [genreId] recently, `null` if it has to load again. */
    fun getCachedRecommendations(genreId: Long?): List<GameData>?

    /** Genres that the most popular games of the last year have most often, most frequent first. */
    suspend fun getGenres(): List<GenreData>

    suspend fun toggleGameFavourite(gameId: Long, isFavourite: Boolean)
}
