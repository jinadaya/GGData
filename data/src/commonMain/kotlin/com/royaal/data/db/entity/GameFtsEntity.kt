package com.royaal.data.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Fts4
import androidx.room.FtsOptions
import androidx.room.PrimaryKey
import com.royaal.data.db.GameEntity

/**
 * Full-text index over `game.searchText` (see [com.royaal.data.db.search.GameSearchText]).
 * External content: the text itself lives in `game` only, and the triggers Room generates keep
 * the index in sync on every insert/update/delete there - nothing writes to this table directly.
 *
 * @param gameId `rowid`, the same as [GameEntity.id].
 */
@Fts4(contentEntity = GameEntity::class, tokenizer = FtsOptions.TOKENIZER_UNICODE61)
@Entity(tableName = "game_fts")
data class GameFtsEntity(
    @PrimaryKey @ColumnInfo(name = "rowid") val gameId: Long,
    val searchText: String,
)
