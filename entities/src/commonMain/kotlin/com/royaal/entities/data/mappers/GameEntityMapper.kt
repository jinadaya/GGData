package com.royaal.entities.data.mappers

import com.royaal.data.db.CompanyEntity
import com.royaal.data.db.GameWithCompanies
import com.royaal.data.db.entity.PlayStatus
import com.royaal.data.network.igdb.model.IgdbImageSizeResponse
import com.royaal.data.network.igdb.typed.InvolvedCompanyFields.developer
import com.royaal.entities.Mapper
import com.royaal.entities.data.CompanyData
import com.royaal.entities.data.GameData
import com.royaal.entities.data.GamePlayStatus
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Instant

class GameEntityMapper(
    private val companyMapper: Mapper<CompanyEntity, CompanyData> = CompanyEntityMapper(),
    private val playStatusMapper: Mapper<PlayStatus, GamePlayStatus> = PlayStatusMapper(),
) : Mapper<GameWithCompanies, GameData> {

    override fun map(input: GameWithCompanies): GameData {
        val game = input.game
        return GameData(
            id = game.id,
            title = game.name,
            coverUrl = game.coverImageId?.let { imageId ->
                coverUrl(imageId, IgdbImageSizeResponse.CoverBig2x)
            },
            coverPreviewUrl = game.coverImageId?.let { imageId ->
                coverUrl(imageId, IgdbImageSizeResponse.CoverSmall)
            },
            description = game.summary,
            releaseYear = game.firstReleaseDate?.let { epochSeconds ->
                Instant.fromEpochSeconds(epochSeconds).toLocalDateTime(TimeZone.UTC).year
            },
            rating = game.rating,
            platforms = input.game.platforms.orEmpty(),
            genres = input.game.genres.orEmpty(),
            developer = input.developer?.let(companyMapper::map),
            publisher = input.publisher?.let(companyMapper::map),
            favourite = input.userGame?.favourite ?: false,
            status = input.userGame?.status?.let(playStatusMapper::map),
            releaseDate = game.gameReleaseDate,
            maxPlayers = game.maxPlayers,
            timeToBeat = game.timeToBeat,
        )
    }

    private fun coverUrl(imageId: String, size: IgdbImageSizeResponse): String =
        "https://images.igdb.com/igdb/image/upload/t_${size.value}/$imageId.jpg"
}
