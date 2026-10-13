package com.royaal.collections.domain

import com.royaal.entities.data.GameData
import com.royaal.entities.data.GamePlayStatus
import kotlinx.coroutines.flow.Flow

/** Which collection is meant. */
internal sealed interface CollectionKey {
    data object Favourites : CollectionKey
    data class Status(val status: GamePlayStatus) : CollectionKey
    data class Custom(val id: Long) : CollectionKey
}

/**
 * A collection on the collections screen.
 *
 * @param name the user's title of a [CollectionKey.Custom] one; others are titled by their key.
 * @param preview the first few games, for the cover.
 */
internal data class CollectionSummary(
    val key: CollectionKey,
    val name: String,
    val pinned: Boolean,
    val gamesCount: Int,
    val preview: List<GameData>,
)

/**
 * A collection with all of its games.
 *
 * @param key [CollectionKey.Status] for a status collection even if it was opened by id.
 * @param collectionId the row of a collection the user has: pin and delete apply to it. `null` for
 *   favourites and for a status collection the user hasn't added.
 */
internal data class CollectionDetails(
    val key: CollectionKey,
    val collectionId: Long?,
    val name: String,
    val pinned: Boolean,
    val games: List<GameData>,
)

/** What [CollectionsRepository.removeGame] took out, to put it back on undo, where it was. */
internal data class RemovedGame(
    val key: CollectionKey,
    val gameId: Long,
    val addedAt: Long?,
    val position: Int = 0,
)

internal interface CollectionsRepository {

    /** Favourites first, then the user's collections: pinned first, then oldest first. */
    fun observeCollections(): Flow<List<CollectionSummary>>

    /** Emits `null` once a [CollectionKey.Custom] collection no longer exists. */
    fun observeCollection(key: CollectionKey): Flow<CollectionDetails?>

    suspend fun createCollection(name: String)

    /** Adds the status collections, in this order, skipping ones the user already has. */
    suspend fun addStatusCollections(statuses: List<GamePlayStatus>)

    suspend fun setPinned(collectionId: Long, pinned: Boolean)

    suspend fun rename(collectionId: Long, name: String)

    /** Puts a user's collection's games in the order of [gameIds], the first one on top. */
    suspend fun reorder(collectionId: Long, gameIds: List<Long>)

    /** A status collection is only hidden: its games keep their status. */
    suspend fun deleteCollection(collectionId: Long)

    /**
     * Takes the game out of [key]: out of a user's collection, or its status / favourite mark
     * cleared. `null` if it wasn't there.
     */
    suspend fun removeGame(key: CollectionKey, gameId: Long): RemovedGame?

    suspend fun restoreGame(removed: RemovedGame)
}
