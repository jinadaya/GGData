package com.royaal.data.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.royaal.data.db.entity.PlayStatus

/**
 * A collection on the user's collections screen. Two kinds:
 * - user-created ([status] is `null`): games are added by hand, membership is tracked by
 *   [com.royaal.data.db.entity.GameCollectionCrossRef];
 * - status collection ([status] is set): the user chose to show "all my Backlog games" etc. as a
 *   collection. Games come from [com.royaal.data.db.entity.UserGameEntity.status], so deleting
 *   the collection only hides it, the games keep their status. At most one per status.
 *
 * Favourites are always shown and have no row here.
 *
 * @param name the user's title; for status collections the title comes from [status] instead.
 * @param pinned pinned collections go first.
 */
@Entity(
    tableName = "game_collection",
    indices = [Index(value = ["status"], unique = true)],
)
data class GameCollectionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val createdAt: Long,
    @ColumnInfo(defaultValue = "0")
    val pinned: Boolean = false,
    val status: PlayStatus? = null,
)
