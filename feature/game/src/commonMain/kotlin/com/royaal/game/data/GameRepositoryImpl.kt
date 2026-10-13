package com.royaal.game.data

import com.royaal.data.db.GameCollectionDao
import com.royaal.data.db.GameDao
import com.royaal.data.db.GameWithCompanies
import com.royaal.data.db.dao.GameCollectionCrossRefDao
import com.royaal.data.db.dao.UserGameDao
import com.royaal.data.db.entity.PlayStatus
import com.royaal.data.network.igdb.IgdbClient
import com.royaal.data.network.igdb.IgdbImageFormat
import com.royaal.data.network.igdb.IgdbImageSize
import com.royaal.data.network.igdb.IgdbMedia
import com.royaal.data.network.igdb.model.GameExpandedResponse
import com.royaal.data.network.igdb.typed.CompanyFields
import com.royaal.data.network.igdb.typed.CompanyLogoFields
import com.royaal.data.network.igdb.typed.GameFields
import com.royaal.data.network.igdb.typed.GameTimeToBeatFields
import com.royaal.data.network.igdb.typed.IgdbField
import com.royaal.data.network.igdb.typed.TypedFilterBuilder
import com.royaal.data.network.igdb.typed.TypedQueryBuilder
import com.royaal.data.network.igdb.typed.typed
import com.royaal.data.prefs.LastOpenedGameStorage
import com.royaal.entities.Mapper
import com.royaal.entities.data.CompanyData
import com.royaal.entities.data.GameData
import com.royaal.entities.data.GamePlayStatus
import com.royaal.entities.data.mappers.CachedGame
import com.royaal.entities.data.mappers.GameCacheMapper
import com.royaal.entities.data.mappers.GameEntityMapper
import com.royaal.entities.data.mappers.GameMapper
import com.royaal.entities.data.mappers.PlayStatusMapper
import com.royaal.game.domain.CompanyDetails
import com.royaal.game.domain.GameDetails
import com.royaal.game.domain.GameLibraryEntry
import com.royaal.game.domain.GameRepository
import com.royaal.game.domain.GameScreenshot
import com.royaal.game.domain.JournalEntry
import com.royaal.game.domain.LibraryCollection
import com.royaal.game.domain.PlayerStats
import kotlin.time.Clock
import kotlin.time.Instant
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

internal class GameRepositoryImpl(
    private val gameDao: GameDao,
    private val userGameDao: UserGameDao,
    private val collectionDao: GameCollectionDao,
    private val crossRefDao: GameCollectionCrossRefDao,
    private val client: IgdbClient,
    private val lastOpenedGameStorage: LastOpenedGameStorage,
    private val gameMapper: Mapper<GameExpandedResponse, GameData> = GameMapper(),
    private val gameEntityMapper: Mapper<GameWithCompanies, GameData> = GameEntityMapper(),
    private val gameCacheMapper: Mapper<GameExpandedResponse, CachedGame> = GameCacheMapper(),
    private val playStatusMapper: Mapper<PlayStatus, GamePlayStatus> = PlayStatusMapper(),
) : GameRepository {

    override fun observeGame(gameId: Long): Flow<GameDetails> = flow {
        val cached = gameDao.getWithCompanies(gameId)
        if (cached != null) emit(cached.toDetails())

        val response = client.typed.expandedGames.byId(gameId) {
            fields(*LIST_FIELDS, *DETAILS_FIELDS)
        } ?: throw NoSuchElementException("IGDB has no game $gameId")
        val timeToBeat = loadTimeToBeat(gameId)
        cache(listOf(response), timeToBeat = timeToBeat)
        emit(response.toDetails(timeToBeat))
    }

    override suspend fun getSeriesGames(details: GameDetails): List<GameData> {
        val seriesIds = details.seriesIds
        val franchiseIds = details.franchiseIds
        if (seriesIds.isEmpty() && franchiseIds.isEmpty()) return emptyList()
        return loadGames {
            where {
                // A series is the closer kin; a franchise is the fallback for games outside any.
                if (seriesIds.isNotEmpty()) {
                    GameFields.collections containsAny seriesIds
                } else {
                    GameFields.franchises containsAny franchiseIds
                }
                GameFields.id neq details.game.id
                standaloneGames()
            }
            asc(GameFields.firstReleaseDate)
            limit(SERIES_LIMIT)
        }
    }

    override suspend fun getSimilarGames(details: GameDetails, excludeGameIds: Set<Long>): List<GameData> {
        val genreIds = details.genreIds.take(SIMILAR_GENRES)
        if (genreIds.isEmpty()) return emptyList()
        val games = loadGames {
            where {
                GameFields.genres containsAll genreIds
                GameFields.id neq details.game.id
                GameFields.totalRatingCount.isNotNull()
                standaloneGames()
            }
            desc(GameFields.totalRatingCount)
            // More than shown: the excluded ones are dropped below.
            limit(SIMILAR_LIMIT + excludeGameIds.size)
        }
        return games.filter { it.id !in excludeGameIds }.take(SIMILAR_LIMIT)
    }

    override suspend fun getDeveloperGames(companyId: Long, excludeGameId: Long?, limit: Int): List<GameData> =
        loadGames {
            where {
                GameFields.involvedCompanies.company eq companyId
                GameFields.involvedCompanies.developer eq true
                if (excludeGameId != null) GameFields.id neq excludeGameId
                standaloneGames()
            }
            desc(GameFields.totalRatingCount)
            limit(limit)
        }

    override suspend fun getCompany(companyId: Long): CompanyDetails? {
        val company = client.typed.expandedCompanies.byId(companyId) {
            fields(CompanyFields.name, CompanyFields.description, CompanyFields.logo, CompanyFields.startDate)
        } ?: return null
        val logoImageId = company.logo?.let { logoId ->
            client.typed.companyLogos.byId(logoId) { fields(CompanyLogoFields.imageId) }?.imageId
        }
        return CompanyDetails(
            id = company.id,
            name = company.name.orEmpty(),
            description = company.description?.takeIf { it.isNotBlank() },
            // 720p fits the logo in whole; logo_med_2x crops wide ones. PNG: logos are often transparent.
            logoUrl = logoImageId?.let {
                IgdbMedia.imageUrl(it, IgdbImageSize.Hd720, format = IgdbImageFormat.Png)
            },
            foundedYear = company.startDate?.let { epochSeconds ->
                Instant.fromEpochSeconds(epochSeconds).toLocalDateTime(TimeZone.UTC).year
            },
        )
    }

    override fun observePlayerStats(gameId: Long): Flow<PlayerStats> = combine(
        userGameDao.observe(gameId),
        collectionDao.observeAll(),
        crossRefDao.observeCollectionIds(gameId),
    ) { userGame, collections, gameCollectionIds ->
        PlayerStats(
            status = userGame?.status?.let(playStatusMapper::map),
            statusChangedAt = userGame?.statusChangedAt.takeIf { userGame?.status != null },
            favourite = userGame?.favourite ?: false,
            collections = collections
                .filter { it.status == null && it.id in gameCollectionIds }
                .map { it.name },
            hoursPlayed = userGame?.hoursPlayed,
            rating = userGame?.rating,
            note = userGame?.note,
        )
    }

    override fun observeJournalEntry(gameId: Long): Flow<JournalEntry> =
        userGameDao.observe(gameId).map { userGame ->
            JournalEntry(
                status = userGame?.status?.let(playStatusMapper::map),
                hoursPlayed = userGame?.hoursPlayed,
                rating = userGame?.rating,
                note = userGame?.note,
            )
        }

    override suspend fun saveJournalEntry(gameId: Long, entry: JournalEntry) {
        val now = Clock.System.now().toEpochMilliseconds()
        val current = userGameDao.get(gameId)
        val playStatus = entry.status?.toPlayStatus()
        // Only if it changed: re-setting the status would move the game to the top of its list.
        if (current?.status != playStatus) userGameDao.setStatus(gameId, playStatus, now)
        userGameDao.setJournalStats(gameId, entry.hoursPlayed, entry.rating, entry.note, now)
    }

    override suspend fun setFavourite(gameId: Long, favourite: Boolean) =
        userGameDao.setFavourite(gameId, favourite, now = Clock.System.now().toEpochMilliseconds())

    override suspend fun setLastOpened(gameId: Long) = lastOpenedGameStorage.save(gameId)

    override fun observeLibraryEntry(gameId: Long): Flow<GameLibraryEntry> = combine(
        userGameDao.observe(gameId),
        collectionDao.observeAll(),
        crossRefDao.observeCollectionIds(gameId),
    ) { userGame, collections, gameCollectionIds ->
        GameLibraryEntry(
            status = userGame?.status?.let(playStatusMapper::map),
            favourite = userGame?.favourite ?: false,
            collections = collections
                .filter { it.status == null }
                .sortedBy { it.createdAt }
                .map { LibraryCollection(it.id, it.name, containsGame = it.id in gameCollectionIds) },
        )
    }

    override suspend fun saveLibraryEntry(
        gameId: Long,
        status: GamePlayStatus?,
        favourite: Boolean,
        collectionIds: Set<Long>,
    ) {
        val now = Clock.System.now().toEpochMilliseconds()
        val current = userGameDao.get(gameId)
        val playStatus = status?.toPlayStatus()
        // Only what changed: re-setting the status would move the game to the top of its list.
        if (current?.status != playStatus) userGameDao.setStatus(gameId, playStatus, now)
        if ((current?.favourite ?: false) != favourite) userGameDao.setFavourite(gameId, favourite, now)
        crossRefDao.setGameCollections(gameId, collectionIds, now)
    }

    /** Fetches games for a row or a list, caching them: they can be opened or added from there. */
    private suspend fun loadGames(block: TypedQueryBuilder.() -> Unit): List<GameData> {
        val games = client.typed.expandedGames.list {
            fields(*LIST_FIELDS)
            block()
        }
        cache(games)
        return games.map(gameMapper::map)
    }

    /** In hours, `null` if IGDB has no estimate or can't be reached: the page is fine without it. */
    private suspend fun loadTimeToBeat(gameId: Long): Int? = try {
        client.typed.gameTimeToBeats.first {
            fields(GameTimeToBeatFields.normally)
            where { GameTimeToBeatFields.gameId eq gameId }
        }?.normally?.let { it / SECONDS_IN_HOUR }
    } catch (e: CancellationException) {
        throw e
    } catch (_: Exception) {
        null
    }

    private suspend fun cache(games: List<GameExpandedResponse>, timeToBeat: Int? = null) {
        if (games.isEmpty()) return
        val cached = games.map(gameCacheMapper::map)
        gameDao.cacheGames(
            games = cached.map { it.game.copy(timeToBeat = timeToBeat ?: it.game.timeToBeat) },
            companies = cached.flatMap { it.companies }.distinctBy { it.id },
        )
    }

    private fun GameWithCompanies.toDetails() = GameDetails(
        game = gameEntityMapper.map(this),
        developer = developer?.let { CompanyData(it.id, it.name) },
        publisher = publisher?.let { CompanyData(it.id, it.name) },
        heroImageUrl = null,
        screenshots = emptyList(),
        seriesIds = emptyList(),
        seriesName = null,
        franchiseIds = emptyList(),
        genreIds = emptyList(),
        isComplete = false,
    )

    private fun GameExpandedResponse.toDetails(timeToBeat: Int?): GameDetails {
        val game = gameMapper.map(this).copy(timeToBeat = timeToBeat)
        val heroImageId = (artworks.map { it.imageId } + screenshots.map { it.imageId }).firstNotNullOfOrNull { it }
        return GameDetails(
            game = game,
            developer = game.developer,
            publisher = game.publisher,
            heroImageUrl = heroImageId?.let { IgdbMedia.imageUrl(it, IgdbImageSize.ScreenshotHuge) },
            screenshots = screenshots.mapNotNull { it.imageId }.map { imageId ->
                GameScreenshot(
                    previewUrl = IgdbMedia.imageUrl(imageId, IgdbImageSize.ScreenshotBig),
                    url = IgdbMedia.imageUrl(imageId, IgdbImageSize.Hd1080),
                )
            },
            seriesIds = collections.map { it.id },
            seriesName = collections.firstNotNullOfOrNull { it.name },
            franchiseIds = franchises.map { it.id },
            genreIds = genres.map { it.id },
            isComplete = true,
        )
    }

    /**
     * No DLCs, bundles, ports or editions (`version_parent` is set on "GOTY", "Deluxe" and alike),
     * so rows aren't filled with copies of the same game.
     */
    private fun TypedFilterBuilder.standaloneGames() {
        GameFields.versionParent.isNull()
        raw("game_type = (${STANDALONE_GAME_TYPES.joinToString(",")})")
    }

    private fun GamePlayStatus.toPlayStatus(): PlayStatus = when (this) {
        GamePlayStatus.BACKLOG -> PlayStatus.BACKLOG
        GamePlayStatus.PLAYING -> PlayStatus.PLAYING
        GamePlayStatus.COMPLETED -> PlayStatus.COMPLETED
        GamePlayStatus.DROPPED -> PlayStatus.DROPPED
    }

    private companion object {
        const val SECONDS_IN_HOUR = 3600
        const val SERIES_LIMIT = 30
        const val SIMILAR_LIMIT = 15

        /** Matching all of a game's genres finds little; its first two say enough. */
        const val SIMILAR_GENRES = 2

        /** IGDB `game_types` ids: main game, standalone expansion, remake, remaster, expanded game. */
        val STANDALONE_GAME_TYPES = listOf(0, 4, 8, 9, 10)

        /** What a game card needs. */
        val LIST_FIELDS: Array<IgdbField<*>> = arrayOf(
            GameFields.id,
            GameFields.name,
            GameFields.cover.imageId,
            GameFields.summary,
            GameFields.firstReleaseDate,
            GameFields.rating,
            GameFields.platforms.name,
            GameFields.genres.name,
            GameFields.involvedCompanies.developer,
            GameFields.involvedCompanies.publisher,
            GameFields.involvedCompanies.companyName,
        )

        /** What only the game's page needs. */
        val DETAILS_FIELDS: Array<IgdbField<*>> = arrayOf(
            GameFields.alternativeNames.name,
            GameFields.artworks.imageId,
            GameFields.screenshots.imageId,
            GameFields.collections.name,
            GameFields.franchises.name,
        )
    }
}
