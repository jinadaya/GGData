package com.royaal.entities.data.mappers

import com.royaal.data.network.igdb.model.CompanyExpandedResponse
import com.royaal.data.network.igdb.model.GameExpandedResponse
import com.royaal.data.network.igdb.model.IgdbImageSizeResponse
import com.royaal.entities.Mapper
import com.royaal.entities.data.CompanyData
import com.royaal.entities.data.GameData
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.format
import kotlinx.datetime.format.DateTimeFormat
import kotlinx.datetime.format.DateTimeFormatBuilder
import kotlinx.datetime.format.MonthNames
import kotlinx.datetime.format.Padding
import kotlinx.datetime.format.alternativeParsing
import kotlinx.datetime.format.char
import kotlinx.datetime.serializers.DayBasedDateTimeUnitSerializer
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Instant

class GameMapper(
    private val companyMapper: Mapper<CompanyExpandedResponse, CompanyData> = CompanyMapper(),
) : Mapper<GameExpandedResponse, GameData> {

    override fun map(input: GameExpandedResponse): GameData = GameData(
        id = input.id,
        title = input.name.orEmpty(),
        coverUrl = input.cover?.imageUrl(IgdbImageSizeResponse.CoverBig2x),
        coverPreviewUrl = input.cover?.imageUrl(IgdbImageSizeResponse.CoverSmall),
        description = input.summary,
        releaseYear = input.firstReleaseDate?.let { epochSeconds ->
            Instant.fromEpochSeconds(epochSeconds).toLocalDateTime(TimeZone.UTC).year
        },
        rating = input.rating,
        platforms = input.platforms.mapNotNull { it.name },
        genres = input.genres.mapNotNull { it.name },
        developer = input.involvedCompanies
            .firstOrNull { it.developer == true }
            ?.company
            ?.let(companyMapper::map),
        publisher = input.involvedCompanies
            .firstOrNull { it.publisher == true }
            ?.company
            ?.let(companyMapper::map),
        favourite = false,
        releaseDate = input.firstReleaseDate?.let { epochSeconds ->
            Instant.fromEpochSeconds(epochSeconds).toLocalDateTime(TimeZone.UTC).format(releaseDateFormat)
        },
        maxPlayers = input.multiplayerModes.maxOfOrNull {
            when {
                it.offlinecoop == true -> it.offlinemax
                it.onlinecoop == true -> it.onlinemax
                else -> 1
            } ?: 1
        },
        timeToBeat = null,
    )
}

/** `12 Nov 2011`: how a release date is shown, and stored in `GameEntity.gameReleaseDate`. */
internal val releaseDateFormat = LocalDateTime.Format {
    day()
    chars(" ")
    monthName(MonthNames.ENGLISH_ABBREVIATED)
    chars(" ")
    year()
}
