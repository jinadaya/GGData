package com.royaal.entities.ui.mappers

import com.royaal.entities.Mapper
import com.royaal.entities.data.GameCollectionData
import com.royaal.entities.ui.UiGameCollection

class GameCollectionUiMapper : Mapper<GameCollectionData, UiGameCollection> {

    override fun map(input: GameCollectionData): UiGameCollection = UiGameCollection(
        id = input.id,
        title = input.title,
        gamesCount = input.gamesCount,
        gameIds = input.gameIds.map { it.toString() },
    )
}
