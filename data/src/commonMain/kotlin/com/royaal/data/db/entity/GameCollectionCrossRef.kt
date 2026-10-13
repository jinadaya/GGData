package com.royaal.data.db.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import com.royaal.data.db.GameCollectionEntity
import com.royaal.data.db.GameEntity

/**
 * Many-to-many membership: a [com.royaal.data.db.GameEntity] can be in several [com.royaal.data.db.GameCollectionEntity]s, or in none
 * (absence of a row here is what "outside any collection" means).
 */
@Entity(
    tableName = "game_collection_cross_ref",
    primaryKeys = ["gameId", "collectionId"],
    foreignKeys = [
        ForeignKey(
            entity = GameEntity::class,
            parentColumns = ["id"],
            childColumns = ["gameId"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = GameCollectionEntity::class,
            parentColumns = ["id"],
            childColumns = ["collectionId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("gameId"), Index("collectionId")],
)
data class GameCollectionCrossRef(
    val gameId: Long,
    val collectionId: Long,
    val addedAt: Long,
)