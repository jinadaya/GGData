package com.royaal.data.db.migrations

import androidx.room.migration.Migration
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.execSQL
import com.royaal.data.db.search.GameSearchText

/**
 * Moves user-set data out of the `game` cache into `user_game`:
 * - `game.favourite` becomes `user_game.favourite`, then the column is dropped;
 * - games of the old "backlog" collection get status `BACKLOG`, then that collection is deleted.
 */
internal val MIGRATION_4_5 = object : Migration(4, 5) {
    override fun migrate(connection: SQLiteConnection) {
        connection.execSQL(
            "CREATE TABLE IF NOT EXISTS `user_game` (`gameId` INTEGER NOT NULL, `status` TEXT, " +
                "`favourite` INTEGER NOT NULL, `statusChangedAt` INTEGER, `updatedAt` INTEGER NOT NULL, " +
                "PRIMARY KEY(`gameId`))"
        )
        connection.execSQL("CREATE INDEX IF NOT EXISTS `index_user_game_status` ON `user_game` (`status`)")
        connection.execSQL("CREATE INDEX IF NOT EXISTS `index_user_game_favourite` ON `user_game` (`favourite`)")

        connection.execSQL(
            """
            INSERT INTO user_game (gameId, status, favourite, statusChangedAt, updatedAt)
            SELECT id, NULL, 1, NULL, cachedAt FROM game WHERE favourite = 1
            """
        )
        connection.execSQL(
            """
            INSERT INTO user_game (gameId, status, favourite, statusChangedAt, updatedAt)
            SELECT ref.gameId, 'BACKLOG', 0, ref.addedAt, ref.addedAt
            FROM game_collection_cross_ref AS ref
            INNER JOIN game_collection AS collection ON collection.id = ref.collectionId
            WHERE lower(collection.name) = 'backlog'
            ON CONFLICT(gameId) DO UPDATE SET
                status = excluded.status,
                statusChangedAt = excluded.statusChangedAt
            """
        )
        connection.execSQL("DELETE FROM game_collection WHERE lower(name) = 'backlog'")

        connection.execSQL("ALTER TABLE game DROP COLUMN favourite")
    }
}

/**
 * Adds search over cached games:
 * - `game.alternativeNames` (stays `null` until a game is re-fetched with them);
 * - `game.searchText`, filled from the names right away (normalization is Kotlin code, not SQL);
 * - the FTS4 index `game_fts` over it, its sync triggers, and the initial index build.
 */
internal val MIGRATION_5_6 = object : Migration(5, 6) {
    override fun migrate(connection: SQLiteConnection) {
        connection.execSQL("ALTER TABLE game ADD COLUMN `alternativeNames` TEXT")
        connection.execSQL("ALTER TABLE game ADD COLUMN `searchText` TEXT NOT NULL DEFAULT ''")

        val names = buildMap {
            connection.prepare("SELECT id, name FROM game").use { statement ->
                while (statement.step()) put(statement.getLong(0), statement.getText(1))
            }
        }
        connection.prepare("UPDATE game SET searchText = ? WHERE id = ?").use { statement ->
            names.forEach { (id, name) ->
                statement.bindText(1, GameSearchText.index(name))
                statement.bindLong(2, id)
                statement.step()
                statement.reset()
            }
        }

        connection.execSQL(
            "CREATE VIRTUAL TABLE IF NOT EXISTS `game_fts` USING FTS4(" +
                "`searchText` TEXT NOT NULL, tokenize=unicode61, content=`game`)"
        )
        GAME_FTS_TRIGGERS.forEach(connection::execSQL)
        connection.execSQL("INSERT INTO game_fts(game_fts) VALUES('rebuild')")
    }
}

/** Same as Room generates for [com.royaal.data.db.entity.GameFtsEntity], see `schemas/.../6.json`. */
private val GAME_FTS_TRIGGERS = listOf(
    "CREATE TRIGGER IF NOT EXISTS room_fts_content_sync_game_fts_BEFORE_UPDATE BEFORE UPDATE ON `game` " +
        "BEGIN DELETE FROM `game_fts` WHERE `docid`=OLD.`rowid`; END",
    "CREATE TRIGGER IF NOT EXISTS room_fts_content_sync_game_fts_BEFORE_DELETE BEFORE DELETE ON `game` " +
        "BEGIN DELETE FROM `game_fts` WHERE `docid`=OLD.`rowid`; END",
    "CREATE TRIGGER IF NOT EXISTS room_fts_content_sync_game_fts_AFTER_UPDATE AFTER UPDATE ON `game` " +
        "BEGIN INSERT INTO `game_fts`(`docid`, `searchText`) VALUES (NEW.`rowid`, NEW.`searchText`); END",
    "CREATE TRIGGER IF NOT EXISTS room_fts_content_sync_game_fts_AFTER_INSERT AFTER INSERT ON `game` " +
        "BEGIN INSERT INTO `game_fts`(`docid`, `searchText`) VALUES (NEW.`rowid`, NEW.`searchText`); END",
)

/**
 * Collections get pinning and status collections ("all my Backlog games"): `pinned` and a
 * nullable `status`, at most one collection per status.
 */
internal val MIGRATION_6_7 = object : Migration(6, 7) {
    override fun migrate(connection: SQLiteConnection) {
        connection.execSQL("ALTER TABLE game_collection ADD COLUMN `pinned` INTEGER NOT NULL DEFAULT 0")
        connection.execSQL("ALTER TABLE game_collection ADD COLUMN `status` TEXT")
        connection.execSQL(
            "CREATE UNIQUE INDEX IF NOT EXISTS `index_game_collection_status` ON `game_collection` (`status`)"
        )
    }
}
