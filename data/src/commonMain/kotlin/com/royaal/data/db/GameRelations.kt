package com.royaal.data.db

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation
import com.royaal.data.db.entity.GameCollectionCrossRef
import com.royaal.data.db.entity.UserGameEntity

data class GameCollectionWithGames(
    @Embedded val collection: GameCollectionEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(
            value = GameCollectionCrossRef::class,
            parentColumn = "collectionId",
            entityColumn = "gameId",
        ),
    )
    val games: List<GameEntity>,
)

data class GameWithCollections(
    @Embedded val game: GameEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(
            value = GameCollectionCrossRef::class,
            parentColumn = "gameId",
            entityColumn = "collectionId",
        ),
    )
    val collections: List<GameCollectionEntity>,
)

data class GameWithCompanies(
    @Embedded val game: GameEntity,
    @Relation(parentColumn = "developerId", entityColumn = "id")
    val developer: CompanyEntity?,
    @Relation(parentColumn = "publisherId", entityColumn = "id")
    val publisher: CompanyEntity?,
    @Relation(parentColumn = "id", entityColumn = "gameId")
    val userGame: UserGameEntity?,
)
