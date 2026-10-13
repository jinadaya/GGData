package com.royaal.game.domain

import com.royaal.entities.data.CompanyData
import com.royaal.entities.data.GameData
import com.royaal.entities.data.GamePlayStatus

/**
 * A game with what its page needs beyond [GameData].
 *
 * @param heroImageUrl a wide artwork or screenshot for the top of the page; `null` to use the cover.
 * @param screenshots in IGDB's order; empty while only cached data is known.
 * @param seriesIds IGDB series (`collections`) the game belongs to; empty while only cached data is known.
 * @param isComplete `false` for what's cached locally, before IGDB answers.
 */
internal data class GameDetails(
    val game: GameData,
    val developer: CompanyData?,
    val publisher: CompanyData?,
    val heroImageUrl: String?,
    val screenshots: List<GameScreenshot>,
    val seriesIds: List<Long>,
    val seriesName: String?,
    val franchiseIds: List<Long>,
    val genreIds: List<Long>,
    val isComplete: Boolean,
)

/** A screenshot: [previewUrl] for a row of them, [url] to look at it full screen. */
internal data class GameScreenshot(
    val previewUrl: String,
    val url: String,
)

/**
 * What the user has put on a game. All empty when they haven't touched it.
 *
 * @param statusChangedAt epoch millis the status was set.
 * @param collections names of the user's own collections the game is in.
 * @param rating the user's own, 1 to 10.
 */
internal data class PlayerStats(
    val status: GamePlayStatus?,
    val statusChangedAt: Long?,
    val favourite: Boolean,
    val collections: List<String>,
    val hoursPlayed: Int?,
    val rating: Int?,
    val note: String?,
)

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
