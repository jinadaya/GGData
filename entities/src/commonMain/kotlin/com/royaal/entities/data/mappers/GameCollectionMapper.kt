package com.royaal.entities.data.mappers

import com.royaal.data.db.GameCollectionWithGames
import com.royaal.entities.Mapper
import com.royaal.entities.data.GameCollectionData

class GameCollectionMapper : Mapper<GameCollectionWithGames, GameCollectionData> {

    override fun map(input: GameCollectionWithGames): GameCollectionData = GameCollectionData(
        id = input.collection.id,
        title = input.collection.name,
        gamesCount = input.games.size,
        gameIds = input.games.map { it.id },
    )
}
