package com.royaal.data.network.igdb.typed

import com.royaal.data.network.igdb.IgdbClient
import com.royaal.data.network.igdb.model.GameResponse

/**
 * Compile-time examples kept as documentation for the typed API.
 * They are not executed.
 */
object TypedUsageExamples {
    suspend fun topRated(client: IgdbClient): List<GameResponse> = client.typed.games.list {
        fields(
            GameFields.id,
            GameFields.name,
            GameFields.rating,
            GameFields.ratingCount,
        )
        where {
            GameFields.rating gte 80.0
            GameFields.ratingCount gt 100
        }
        orderBy(GameFields.rating, com.royaal.data.network.igdb.query.SortOrder.Desc)
        limit(20)
    }

    suspend fun rpgOnPlaystation(client: IgdbClient): List<GameResponse> = client.typed.games.list {
        fields(GameFields.id, GameFields.name, GameFields.rating)
        where {
            GameFields.platforms containsAny listOf(48L)
            anyOf {
                GameFields.genres containsAny listOf(12L)
                GameFields.genres containsAny listOf(16L)
            }
        }
        orderBy(GameFields.rating.desc())
        limit(50)
    }
}
