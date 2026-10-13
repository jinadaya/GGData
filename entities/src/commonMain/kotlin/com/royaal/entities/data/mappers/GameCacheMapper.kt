package com.royaal.entities.data.mappers

import com.royaal.data.db.CompanyEntity
import com.royaal.data.db.GameEntity
import com.royaal.data.network.igdb.model.GameExpandedResponse
import com.royaal.entities.Mapper
import kotlinx.datetime.TimeZone
import kotlinx.datetime.format
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock
import kotlin.time.Instant

/** An IGDB game ready for [com.royaal.data.db.GameDao.cacheGames]: the row and the companies it references. */
data class CachedGame(
    val game: GameEntity,
    val companies: List<CompanyEntity>,
)

/**
 * Maps an IGDB game to its cache rows. Fields the response doesn't carry stay `null`, so caching
 * keeps whatever was stored for them before.
 */
class GameCacheMapper(
    private val now: () -> Long = { Clock.System.now().toEpochMilliseconds() },
) : Mapper<GameExpandedResponse, CachedGame> {

    override fun map(input: GameExpandedResponse): CachedGame {
        val developer = input.involvedCompanies.firstOrNull { it.developer == true }?.company
        val publisher = input.involvedCompanies.firstOrNull { it.publisher == true }?.company
        return CachedGame(
            game = GameEntity(
                id = input.id,
                name = input.name.orEmpty(),
                coverImageId = input.cover?.imageId,
                firstReleaseDate = input.firstReleaseDate,
                rating = input.rating,
                platforms = input.platforms.mapNotNull { it.name }.takeIf { it.isNotEmpty() },
                genres = input.genres.mapNotNull { it.name }.takeIf { it.isNotEmpty() },
                summary = input.summary,
                developerId = developer?.id,
                publisherId = publisher?.id,
                cachedAt = now(),
                timeToBeat = null,
                maxPlayers = null,
                gameReleaseDate = input.firstReleaseDate?.let { epochSeconds ->
                    Instant.fromEpochSeconds(epochSeconds).toLocalDateTime(TimeZone.UTC).format(releaseDateFormat)
                },
                alternativeNames = input.alternativeNames.mapNotNull { it.name }.takeIf { it.isNotEmpty() },
            ),
            companies = listOfNotNull(developer, publisher)
                .distinctBy { it.id }
                .map { company -> CompanyEntity(id = company.id, name = company.name.orEmpty()) },
        )
    }
}
