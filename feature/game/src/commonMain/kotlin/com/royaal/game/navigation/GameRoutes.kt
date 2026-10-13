package com.royaal.game.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

/** A game's page. Shows what's cached at once, then the rest from IGDB. */
@Serializable
data class GameRoute(val gameId: Long) : NavKey

/** A developer's (or any company's) page with the games it made. */
@Serializable
data class CompanyRoute(val companyId: Long) : NavKey
