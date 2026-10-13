package com.royaal.main.domain

import com.royaal.entities.data.GameData
import com.royaal.entities.data.GamePlayStatus

/**
 * A collection on Discover.
 *
 * @param name the user's title; empty for a status collection, which is titled by [status].
 * @param preview the first few games, for the cover.
 */
internal data class DiscoverCollection(
    val id: Long,
    val name: String,
    val status: GamePlayStatus?,
    val gamesCount: Int,
    val preview: List<GameData>,
)
