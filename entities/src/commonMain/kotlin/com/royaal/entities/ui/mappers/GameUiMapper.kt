package com.royaal.entities.ui.mappers

import com.royaal.entities.Mapper
import com.royaal.entities.data.GameData
import com.royaal.entities.ui.UiGame

class GameUiMapper : Mapper<GameData, UiGame> {

    override fun map(input: GameData): UiGame = UiGame(
        id = input.id,
        title = input.title,
        coverUrl = input.coverUrl,
        coverPreviewUrl = input.coverPreviewUrl,
        rating = input.rating?.toFloat() ?: -1f,
        devTeamTitle = input.developer?.name,
        releaseYear = input.releaseYear,
        platforms = input.platforms.takeIf { it.isNotEmpty() },
        genres = input.genres.takeIf { it.isNotEmpty() },
        description = input.description.orEmpty(),
        favourite = input.favourite,
        timeToBeat = input.timeToBeat,
        maxPlayers = input.maxPlayers,
        releaseDate = input.releaseDate,
    )
}
