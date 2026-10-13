package com.royaal.search.domain

import com.royaal.entities.data.GameData
import kotlinx.coroutines.flow.Flow

internal interface SearchRepository {

    /**
     * Games matching [query]. Cached games come first, right away; meanwhile IGDB is searched,
     * and what it finds is cached and shows up in a later emission. Emits again on every cache
     * change, so collect it only while the query is current.
     */
    fun search(query: String): Flow<GameSearchResult>
}

internal data class GameSearchResult(
    val games: List<GameData>,
    val remoteStatus: RemoteSearchStatus,
)

internal enum class RemoteSearchStatus { LOADING, DONE, FAILED }
