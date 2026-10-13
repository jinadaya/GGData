package com.royaal.data.db

import androidx.room.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import androidx.sqlite.execSQL
import com.royaal.data.db.entity.GameCollectionCrossRef
import com.royaal.data.db.entity.PlayStatus
import com.royaal.data.db.migrations.MIGRATION_6_7
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

/** Runs on iOS only: it needs the bundled SQLite's native library, which Android host tests lack. */
class GameCollectionDaoTest {

    private val database = Room.inMemoryDatabaseBuilder<AppDatabase>()
        .setDriver(BundledSQLiteDriver())
        .setQueryCoroutineContext(Dispatchers.Default)
        .build()
    private val collectionDao = database.gameCollectionDao()
    private val crossRefDao = database.gameCollectionCrossRefDao()
    private val gameDao = database.gameDao()

    @AfterTest
    fun tearDown() = database.close()

    @Test
    fun pinnedCollectionsGoFirstThenOldest() = runTest {
        val old = collectionDao.insert(GameCollectionEntity(name = "Old", createdAt = 1))
        val new = collectionDao.insert(GameCollectionEntity(name = "New", createdAt = 2))
        val pinned = collectionDao.insert(GameCollectionEntity(name = "Pinned", createdAt = 3))

        collectionDao.setPinned(pinned, true)

        assertEquals(listOf(pinned, old, new), collectionDao.observeAll().first().map { it.id })
    }

    @Test
    fun statusCollectionIsAddedOnce() = runTest {
        val backlog = GameCollectionEntity(name = "", createdAt = 1, status = PlayStatus.BACKLOG)

        collectionDao.insertIgnoringExisting(listOf(backlog))
        collectionDao.insertIgnoringExisting(listOf(backlog, backlog.copy(status = PlayStatus.PLAYING)))

        val statuses = collectionDao.observeAll().first().map { it.status }
        assertEquals(listOf(PlayStatus.BACKLOG, PlayStatus.PLAYING), statuses)
    }

    @Test
    fun gamesOfCollectionAreMostRecentlyAddedFirst() = runTest {
        gameDao.upsertAll(listOf(game(1, "First"), game(2, "Second")))
        val collection = collectionDao.insert(GameCollectionEntity(name = "Mine", createdAt = 1))
        crossRefDao.addGameToCollection(GameCollectionCrossRef(gameId = 1, collectionId = collection, addedAt = 10))
        crossRefDao.addGameToCollection(GameCollectionCrossRef(gameId = 2, collectionId = collection, addedAt = 20))

        val names = collectionDao.observeGames(collection).first().map { it.game.name }

        assertEquals(listOf("Second", "First"), names)
    }

    @Test
    fun settingGameCollectionsKeepsWhenItWasAddedToTheRemainingOnes() = runTest {
        gameDao.upsertAll(listOf(game(1, "Game")))
        val kept = collectionDao.insert(GameCollectionEntity(name = "Kept", createdAt = 1))
        val removed = collectionDao.insert(GameCollectionEntity(name = "Removed", createdAt = 2))
        val added = collectionDao.insert(GameCollectionEntity(name = "Added", createdAt = 3))
        crossRefDao.setGameCollections(gameId = 1, collectionIds = setOf(kept, removed), now = 10)

        crossRefDao.setGameCollections(gameId = 1, collectionIds = setOf(kept, added), now = 20)

        assertEquals(setOf(kept, added), crossRefDao.getCollectionIds(1).toSet())
        assertEquals(10, crossRefDao.get(gameId = 1, collectionId = kept)?.addedAt)
        assertEquals(20, crossRefDao.get(gameId = 1, collectionId = added)?.addedAt)
    }

    @Test
    fun migrationFrom6KeepsCollectionsUnpinnedAndAllowsOneCollectionPerStatus() {
        val connection = BundledSQLiteDriver().open(":memory:")
        connection.execSQL(GAME_COLLECTION_TABLE_V6)
        connection.execSQL("INSERT INTO game_collection (name, createdAt) VALUES ('Mine', 0)")

        MIGRATION_6_7.migrate(connection)

        val row = connection.prepare("SELECT pinned, status IS NULL FROM game_collection").use {
            it.step()
            it.getLong(0) to it.getLong(1)
        }
        assertEquals(0L to 1L, row)
        connection.execSQL("INSERT INTO game_collection (name, createdAt, status) VALUES ('', 0, 'BACKLOG')")
        assertFailsWith<Exception> {
            connection.execSQL("INSERT INTO game_collection (name, createdAt, status) VALUES ('', 0, 'BACKLOG')")
        }
        connection.close()
    }

    private fun game(id: Long, name: String) = GameEntity(
        id = id,
        name = name,
        cachedAt = 0,
        timeToBeat = null,
        maxPlayers = null,
        gameReleaseDate = null,
    )

    private companion object {
        /** `game_collection` as of schema 6, see `schemas/.../6.json`. */
        const val GAME_COLLECTION_TABLE_V6 = "CREATE TABLE IF NOT EXISTS `game_collection` " +
            "(`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT NOT NULL, `createdAt` INTEGER NOT NULL)"
    }
}
