package com.royaal.main.data

import com.royaal.arch.result.onSuccess
import com.royaal.arch.result.wrapAsResult
import com.royaal.data.db.GameCollectionDao
import com.royaal.data.db.GameDao
import com.royaal.data.db.GameWithCompanies
import com.royaal.data.db.dao.UserGameDao
import com.royaal.data.db.entity.PlayStatus
import com.royaal.data.network.igdb.IgdbClient
import com.royaal.data.network.igdb.model.GameExpandedResponse
import com.royaal.data.network.igdb.model.GenreExpandedResponse
import com.royaal.data.network.igdb.typed.GameFields
import com.royaal.data.network.igdb.typed.GameTimeToBeatFields
import com.royaal.data.network.igdb.typed.TypedFilterBuilder
import com.royaal.data.network.igdb.typed.typed
import com.royaal.data.prefs.LastOpenedGameStorage
import com.royaal.entities.Mapper
import com.royaal.entities.data.GameData
import com.royaal.entities.data.GamePlayStatus
import com.royaal.entities.data.GenreData
import com.royaal.entities.data.mappers.CachedGame
import com.royaal.entities.data.mappers.GameCacheMapper
import com.royaal.entities.data.mappers.GameEntityMapper
import com.royaal.entities.data.mappers.GameMapper
import com.royaal.entities.data.mappers.GenreMapper
import com.royaal.entities.data.mappers.PlayStatusMapper
import com.royaal.main.domain.DiscoverCollection
import com.royaal.main.domain.MainScreenRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlin.time.Clock
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.minutes

internal class MainScreenRepositoryImpl(
    private val userGameDao: UserGameDao,
    private val collectionDao: GameCollectionDao,
    private val gameDao: GameDao,
    private val client: IgdbClient,
    private val lastOpenedGameStorage: LastOpenedGameStorage,
    private val gameCacheMapper: Mapper<GameExpandedResponse, CachedGame> = GameCacheMapper(),
    private val gameMapper: Mapper<GameExpandedResponse, GameData> = GameMapper(),
    private val gameEntityMapper: Mapper<GameWithCompanies, GameData> = GameEntityMapper(),
    private val playStatusMapper: Mapper<PlayStatus, GamePlayStatus> = PlayStatusMapper(),
    private val genreMapper: Mapper<GenreExpandedResponse, GenreData> = GenreMapper(),
) : MainScreenRepository {

    private val recommendationsCache = ExpiringCache<Long?, List<GameData>>(ttl = CACHE_TTL)
    private val genresCache = ExpiringCache<Unit, List<GenreData>>(ttl = CACHE_TTL)

    override fun getLatestGame(): Flow<GameData> = flow {
        val gameId = lastOpenedGameStorage.load() ?: return@flow
        val timeToBeat = client.typed.gameTimeToBeats.first {
            fields(GameTimeToBeatFields.normally)
            where { GameTimeToBeatFields.gameId eq gameId }
        }

        client.typed.expandedGames
            .byId(gameId) {
                fields(
                    GameFields.id,
                    GameFields.name,
                    GameFields.cover.imageId,
                    GameFields.summary,
                    GameFields.releaseDates.year,
                    GameFields.platforms.name,
                    GameFields.involvedCompanies.developer,
                    GameFields.involvedCompanies.companyName,
                )
            }
            .wrapAsResult()
            .onSuccess { response ->
                cache(listOf(response))
                val game = gameMapper.map(response).copy(
                    timeToBeat = timeToBeat?.normally?.let { it / SECONDS_IN_HOUR },
                )
                emitAll(userGameDao.observe(gameId).map { userGame ->
                    game.copy(
                        favourite = userGame?.favourite ?: false,
                        status = userGame?.status?.let(playStatusMapper::map),
                    )
                })
            }
    }

    override fun getCollections(): Flow<List<DiscoverCollection>> = combine(
        collectionDao.observeAllWithGames(),
        userGameDao.observeGamesWithAnyStatus(),
    ) { collections, gamesWithStatus ->
        val gamesByStatus = gamesWithStatus.groupBy { it.userGame?.status }
        collections.mapNotNull { (collection, collectionGames) ->
            val status = collection.status
            val games = if (status != null) {
                gamesByStatus[status].orEmpty()
            } else {
                // The relation brings plain games; companies and user data aren't needed for a cover.
                collectionGames.map { GameWithCompanies(it, developer = null, publisher = null, userGame = null) }
            }
            if (games.isEmpty()) return@mapNotNull null
            DiscoverCollection(
                id = collection.id,
                name = collection.name,
                status = status?.let(playStatusMapper::map),
                gamesCount = games.size,
                preview = games.take(COLLECTION_PREVIEW_SIZE).map(gameEntityMapper::map),
            )
        }
    }

    override fun getBacklogGames(): Flow<List<GameData>> =
        userGameDao.observeGamesByStatus(PlayStatus.BACKLOG).map { games ->
            games.map(gameEntityMapper::map)
        }

    override suspend fun getRecommendations(genreId: Long?): List<GameData> =
        recommendationsCache.getOrLoad(genreId) { loadRecommendations(genreId) }

    override fun getCachedRecommendations(genreId: Long?): List<GameData>? = recommendationsCache[genreId]

    private suspend fun loadRecommendations(genreId: Long?): List<GameData> =
        client.typed.expandedGames
            .list {
                fields(
                    GameFields.id,
                    GameFields.name,
                    GameFields.cover.imageId,
                    GameFields.summary,
                    GameFields.firstReleaseDate,
                    GameFields.rating,
                    GameFields.platforms.name,
                    GameFields.genres.name,
                    GameFields.involvedCompanies.developer,
                    GameFields.involvedCompanies.companyName,
                )
                where {
                    popularLastYear()
                    if (genreId != null) GameFields.genres containsAny listOf(genreId)
                }
                desc(GameFields.totalRatingCount)
                limit(RECOMMENDATIONS_COUNT)
            }
            // Cached, so they can be put into collections, which reference cached games only.
            .also { cache(it) }
            .map(gameMapper::map)

    /**
     * IGDB has no genre popularity, so it's derived from a sample of the most popular recent
     * games: the more of them have a genre, the higher it goes. Every genre returned is thus
     * guaranteed to have games for [getRecommendations].
     */
    override suspend fun getGenres(): List<GenreData> = genresCache.getOrLoad(Unit) { loadGenres() }

    private suspend fun loadGenres(): List<GenreData> {
        val sample = client.typed.expandedGames.list {
            fields(GameFields.genres.name)
            where { popularLastYear() }
            desc(GameFields.totalRatingCount)
            limit(GENRES_SAMPLE_SIZE)
        }
        val genres = sample.flatMap { it.genres }.filter { !it.name.isNullOrBlank() }
        val gamesPerGenre = genres.groupingBy { it.id }.eachCount()
        return genres
            .distinctBy { it.id }
            .sortedByDescending { gamesPerGenre[it.id] }
            .take(GENRES_COUNT)
            .map(genreMapper::map)
    }

    override suspend fun toggleGameFavourite(gameId: Long, isFavourite: Boolean) {
        userGameDao.setFavourite(
            gameId,
            isFavourite,
            now = Clock.System.now().toEpochMilliseconds()
        )
    }

    private suspend fun cache(games: List<GameExpandedResponse>) {
        val cached = games.map(gameCacheMapper::map)
        gameDao.cacheGames(
            games = cached.map { it.game },
            companies = cached.flatMap { it.companies }.distinctBy { it.id },
        )
    }

    /**
     * Released within the last year (not announced for later), rated at least once, so there's
     * something to rank by. Only standalone games: no DLCs, bundles, ports or editions
     * (`version_parent` is set on "GOTY", "Deluxe" and alike).
     */
    private fun TypedFilterBuilder.popularLastYear() {
        val now = Clock.System.now()
        GameFields.firstReleaseDate gte (now - 365.days).epochSeconds
        GameFields.firstReleaseDate lte now.epochSeconds
        GameFields.totalRatingCount.isNotNull()
        GameFields.versionParent.isNull()
        raw("game_type = (${STANDALONE_GAME_TYPES.joinToString(",")})")
    }

    private companion object {
        const val SECONDS_IN_HOUR = 3600
        const val RECOMMENDATIONS_COUNT = 10

        /** As many as [com.royaal.designsystem.uikit.CollectionCard] shows. */
        const val COLLECTION_PREVIEW_SIZE = 4
        const val GENRES_COUNT = 10
        const val GENRES_SAMPLE_SIZE = 500

        /** Popularity barely moves within it, and switching genres back and forth stays instant. */
        val CACHE_TTL = 30.minutes

        /** IGDB `game_types` ids: main game, standalone expansion, remake, remaster, expanded game. */
        val STANDALONE_GAME_TYPES = listOf(0, 4, 8, 9, 10)
    }
}
