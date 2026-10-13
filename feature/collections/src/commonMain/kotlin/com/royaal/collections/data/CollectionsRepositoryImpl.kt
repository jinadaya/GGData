package com.royaal.collections.data

import com.royaal.collections.domain.CollectionDetails
import com.royaal.collections.domain.CollectionKey
import com.royaal.collections.domain.CollectionSummary
import com.royaal.collections.domain.CollectionsRepository
import com.royaal.collections.domain.RemovedGame
import com.royaal.data.db.GameCollectionDao
import com.royaal.data.db.GameCollectionEntity
import com.royaal.data.db.GameWithCompanies
import com.royaal.data.db.dao.GameCollectionCrossRefDao
import com.royaal.data.db.dao.UserGameDao
import com.royaal.data.db.entity.GameCollectionCrossRef
import com.royaal.data.db.entity.PlayStatus
import com.royaal.entities.Mapper
import com.royaal.entities.data.GameData
import com.royaal.entities.data.GamePlayStatus
import com.royaal.entities.data.mappers.GameEntityMapper
import com.royaal.entities.data.mappers.PlayStatusMapper
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlin.time.Clock

internal class CollectionsRepositoryImpl(
    private val collectionDao: GameCollectionDao,
    private val crossRefDao: GameCollectionCrossRefDao,
    private val userGameDao: UserGameDao,
    private val gameEntityMapper: Mapper<GameWithCompanies, GameData> = GameEntityMapper(),
    private val playStatusMapper: Mapper<PlayStatus, GamePlayStatus> = PlayStatusMapper(),
) : CollectionsRepository {

    override fun observeCollections(): Flow<List<CollectionSummary>> = combine(
        collectionDao.observeAllWithGames(),
        userGameDao.observeGamesWithAnyStatus(),
        userGameDao.observeFavouriteGames(),
    ) { collections, gamesWithStatus, favourites ->
        val gamesByStatus = gamesWithStatus.groupBy { it.userGame?.status }
        val favouritesSummary = summary(CollectionKey.Favourites, name = "", pinned = false, favourites)
        val userCollections = collections.map { (collection, games) ->
            val status = collection.status
            if (status != null) {
                summary(
                    key = CollectionKey.Status(playStatusMapper.map(status)),
                    name = "",
                    pinned = collection.pinned,
                    games = gamesByStatus[status].orEmpty(),
                )
            } else {
                summary(
                    key = CollectionKey.Custom(collection.id),
                    name = collection.name,
                    pinned = collection.pinned,
                    // The relation brings plain games; companies and user data aren't needed for a cover.
                    games = games.map { GameWithCompanies(it, developer = null, publisher = null, userGame = null) },
                )
            }
        }
        listOf(favouritesSummary) + userCollections
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun observeCollection(key: CollectionKey): Flow<CollectionDetails?> = when (key) {
        CollectionKey.Favourites -> userGameDao.observeFavouriteGames().map { games ->
            CollectionDetails(key, collectionId = null, name = "", pinned = false, games = games.map(gameEntityMapper::map))
        }

        is CollectionKey.Status -> observeStatusCollection(key.status)

        is CollectionKey.Custom -> collectionDao.observeById(key.id).flatMapLatest { collection ->
            val status = collection?.status
            when {
                collection == null -> flowOf(null)
                status != null -> observeStatusCollection(playStatusMapper.map(status))
                else -> collectionDao.observeGames(collection.id).map { games ->
                    CollectionDetails(
                        key = key,
                        collectionId = collection.id,
                        name = collection.name,
                        pinned = collection.pinned,
                        games = games.map(gameEntityMapper::map),
                    )
                }
            }
        }
    }

    private fun observeStatusCollection(status: GamePlayStatus): Flow<CollectionDetails> {
        val playStatus = status.toPlayStatus()
        return combine(
            collectionDao.observeByStatus(playStatus),
            userGameDao.observeGamesByStatus(playStatus),
        ) { collection, games ->
            CollectionDetails(
                key = CollectionKey.Status(status),
                collectionId = collection?.id,
                name = "",
                pinned = collection?.pinned ?: false,
                games = games.map(gameEntityMapper::map),
            )
        }
    }

    override suspend fun createCollection(name: String) {
        collectionDao.insert(GameCollectionEntity(name = name.trim(), createdAt = now()))
    }

    override suspend fun addStatusCollections(statuses: List<GamePlayStatus>) {
        val now = now()
        collectionDao.insertIgnoringExisting(
            // A millisecond apart, so they keep the given order among collections sorted by creation.
            statuses.mapIndexed { index, status ->
                GameCollectionEntity(name = "", createdAt = now + index, status = status.toPlayStatus())
            }
        )
    }

    override suspend fun setPinned(collectionId: Long, pinned: Boolean) =
        collectionDao.setPinned(collectionId, pinned)

    override suspend fun deleteCollection(collectionId: Long) =
        collectionDao.deleteById(collectionId)

    override suspend fun removeGame(key: CollectionKey, gameId: Long): RemovedGame? = when (key) {
        CollectionKey.Favourites -> {
            userGameDao.setFavourite(gameId, favourite = false, now = now())
            RemovedGame(key, gameId, addedAt = null)
        }

        is CollectionKey.Status -> {
            userGameDao.setStatus(gameId, status = null, now = now())
            RemovedGame(key, gameId, addedAt = null)
        }

        is CollectionKey.Custom -> crossRefDao.get(gameId, key.id)?.let { crossRef ->
            crossRefDao.removeGameFromCollection(gameId, key.id)
            RemovedGame(key, gameId, addedAt = crossRef.addedAt)
        }
    }

    override suspend fun restoreGame(removed: RemovedGame) = when (val key = removed.key) {
        CollectionKey.Favourites -> userGameDao.setFavourite(removed.gameId, favourite = true, now = now())
        is CollectionKey.Status -> userGameDao.setStatus(removed.gameId, key.status.toPlayStatus(), now = now())
        is CollectionKey.Custom -> crossRefDao.addGameToCollection(
            GameCollectionCrossRef(
                gameId = removed.gameId,
                collectionId = key.id,
                // Back to where it was in the list, not to the top.
                addedAt = removed.addedAt ?: now(),
            )
        )
    }

    private fun summary(
        key: CollectionKey,
        name: String,
        pinned: Boolean,
        games: List<GameWithCompanies>,
    ) = CollectionSummary(
        key = key,
        name = name,
        pinned = pinned,
        gamesCount = games.size,
        preview = games.take(PREVIEW_SIZE).map(gameEntityMapper::map),
    )

    private fun GamePlayStatus.toPlayStatus(): PlayStatus = when (this) {
        GamePlayStatus.BACKLOG -> PlayStatus.BACKLOG
        GamePlayStatus.PLAYING -> PlayStatus.PLAYING
        GamePlayStatus.COMPLETED -> PlayStatus.COMPLETED
        GamePlayStatus.DROPPED -> PlayStatus.DROPPED
    }

    private fun now(): Long = Clock.System.now().toEpochMilliseconds()

    private companion object {
        /** As many as [com.royaal.designsystem.uikit.CollectionCard] shows. */
        const val PREVIEW_SIZE = 4
    }
}
