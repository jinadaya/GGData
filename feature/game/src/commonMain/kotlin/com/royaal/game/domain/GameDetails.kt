package com.royaal.game.domain

import com.royaal.entities.data.CompanyData
import com.royaal.entities.data.GameData
import com.royaal.entities.data.GamePlayStatus

/**
 * A game with what its page needs beyond [GameData].
 *
 * @param heroImageUrl a wide artwork or screenshot for the top of the page; `null` to use the cover.
 * @param seriesIds IGDB series (`collections`) the game belongs to; empty while only cached data is known.
 * @param isComplete `false` for what's cached locally, before IGDB answers.
 */
internal data class GameDetails(
    val game: GameData,
    val developer: CompanyData?,
    val publisher: CompanyData?,
    val heroImageUrl: String?,
    val seriesIds: List<Long>,
    val seriesName: String?,
    val franchiseIds: List<Long>,
    val genreIds: List<Long>,
    val isComplete: Boolean,
)

/**
 * What the user has put on a game. All empty when they haven't touched it.
 *
 * @param statusChangedAt epoch millis the status was set.
 * @param collections names of the user's own collections the game is in.
 */
internal data class PlayerStats(
    val status: GamePlayStatus?,
    val statusChangedAt: Long?,
    val favourite: Boolean,
    val collections: List<String>,
) {
    val isEmpty: Boolean
        get() = status == null && !favourite && collections.isEmpty()
}

/**
 * A company's page.
 *
 * @param foundedYear the year it was founded, if IGDB knows it.
 */
internal data class CompanyDetails(
    val id: Long,
    val name: String,
    val description: String?,
    val logoUrl: String?,
    val foundedYear: Int?,
)
