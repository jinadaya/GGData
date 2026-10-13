package com.royaal.data.db

import androidx.room.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import androidx.sqlite.execSQL
import com.royaal.data.db.migrations.MIGRATION_5_6
import com.royaal.data.db.search.GameSearchText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals

/** Runs on iOS only: it needs the bundled SQLite's native library, which Android host tests lack. */
class GameSearchDaoTest {

    private val database = Room.inMemoryDatabaseBuilder<AppDatabase>()
        .setDriver(BundledSQLiteDriver())
        .setQueryCoroutineContext(Dispatchers.Default)
        .build()
    private val dao = database.gameDao()

    @AfterTest
    fun tearDown() = database.close()

    @Test
    fun matchesWordPrefixesInAnyCaseScriptAndAccent() = runTest {
        cacheAll()

        assertEquals(listOf(WITCHER_3), search("ведь охота"))
        assertEquals(listOf(WITCHER_3), search("ВЕДЬМАК"))
        assertEquals(listOf(POKEMON), search("pokemon"))
        assertEquals(listOf(POKEMON), search("POKÉ"))
        assertEquals(listOf(ASSASSINS_CREED), search("assassins"))
        assertEquals(listOf(ASSASSINS_CREED), search("assassin's cr"))
        assertEquals(listOf(HEDGEHOG), search("ежик"))
    }

    @Test
    fun matchesAlternativeNamesAndNumeralsInBothForms() = runTest {
        cacheAll()

        assertEquals(listOf(GTA_5), search("gta 5"))
        assertEquals(listOf(GTA_5), search("grand theft auto 5"))
        assertEquals(listOf(WITCHER_3), search("ведьмак 3"))
        assertEquals(listOf(WITCHER_3), search("witcher iii"))
        assertEquals(listOf(HALO), search("halo ce"))
        assertEquals(listOf(FINAL_FANTASY_7), search("final fantasy 7"))
    }

    @Test
    fun ranksTitlesStartingWithQueryFirstThenByRating() = runTest {
        cacheAll()

        assertEquals(listOf(DUNGEONS_3, MINECRAFT_DUNGEONS), search("dungeons"))
        assertEquals(listOf(MINECRAFT_DUNGEONS, MINECRAFT), search("mine"))
    }

    @Test
    fun substringSearchFindsWordMiddles() = runTest {
        cacheAll()

        assertEquals(emptyList(), search("craft"))
        val found = dao.observeContainingAll(listOf("craft"), limit = 10).first().map { it.game.name }
        assertEquals(listOf(MINECRAFT_DUNGEONS, MINECRAFT), found)
    }

    @Test
    fun indexFollowsUpdatesAndKeepsCachedDetails() = runTest {
        cacheAll()
        dao.cacheGames(listOf(game(1, MINECRAFT).copy(timeToBeat = 100)), companies = emptyList())

        dao.cacheGames(listOf(game(1, "Minecraft: Java Edition", alternativeNames = listOf("MC"))), emptyList())

        assertEquals(listOf("Minecraft: Java Edition"), search("java"))
        assertEquals(listOf("Minecraft: Java Edition"), search("mc"))
        assertEquals(100, dao.getById(1)?.timeToBeat)
        dao.deleteById(1)
        assertEquals(emptyList(), search("java"))
    }

    @Test
    fun migrationFrom5IndexesExistingGames() {
        val connection = BundledSQLiteDriver().open(":memory:")
        connection.execSQL(GAME_TABLE_V5)
        connection.execSQL("INSERT INTO game (id, name, cachedAt) VALUES (7, 'Pokémon Red', 0)")

        MIGRATION_5_6.migrate(connection)

        val found = connection.prepare("SELECT rowid FROM game_fts WHERE game_fts MATCH 'pokemon*'").use {
            buildList { while (it.step()) add(it.getLong(0)) }
        }
        assertEquals(listOf(7L), found)
        connection.close()
    }

    private suspend fun search(query: String): List<String> {
        val words = GameSearchText.words(query)
        return dao.observeMatchingWordPrefixes(
            match = GameSearchText.prefixMatch(words),
            titlePrefix = words.joinToString(" ") + "%",
            limit = 10,
        ).first().map { it.game.name }
    }

    private suspend fun cacheAll() = dao.cacheGames(
        games = listOf(
            game(1, MINECRAFT, rating = 80.0),
            game(2, MINECRAFT_DUNGEONS, rating = 90.0),
            game(3, WITCHER_3, alternativeNames = listOf("Ведьмак 3: Дикая Охота")),
            game(4, POKEMON),
            game(5, ASSASSINS_CREED),
            game(6, GTA_5, alternativeNames = listOf("GTA V")),
            game(7, HALO, alternativeNames = listOf("Halo CE")),
            game(8, FINAL_FANTASY_7),
            game(9, HEDGEHOG),
            game(10, DUNGEONS_3, rating = 70.0),
        ),
        companies = emptyList(),
    )

    private fun game(id: Long, name: String, alternativeNames: List<String>? = null, rating: Double? = null) =
        GameEntity(
            id = id,
            name = name,
            rating = rating,
            cachedAt = 0,
            timeToBeat = null,
            maxPlayers = null,
            gameReleaseDate = null,
            alternativeNames = alternativeNames,
        )

    private companion object {
        const val MINECRAFT = "Minecraft"
        const val MINECRAFT_DUNGEONS = "Minecraft Dungeons"
        const val WITCHER_3 = "The Witcher 3: Wild Hunt"
        const val POKEMON = "Pokémon Red"
        const val ASSASSINS_CREED = "Assassin’s Creed"
        const val GTA_5 = "Grand Theft Auto V"
        const val HALO = "Halo: Combat Evolved"
        const val FINAL_FANTASY_7 = "Final Fantasy VII"
        const val HEDGEHOG = "Ёжик в тумане"
        const val DUNGEONS_3 = "Dungeons 3"

        /** `game` as of schema 5, see `schemas/.../5.json`. */
        const val GAME_TABLE_V5 = "CREATE TABLE IF NOT EXISTS `game` (`id` INTEGER NOT NULL, " +
            "`name` TEXT NOT NULL, `coverImageId` TEXT, `firstReleaseDate` INTEGER, `rating` REAL, " +
            "`platforms` TEXT, `genres` TEXT, `summary` TEXT, `developerId` INTEGER, `publisherId` INTEGER, " +
            "`cachedAt` INTEGER NOT NULL, `timeToBeat` INTEGER, `maxPlayers` INTEGER, " +
            "`gameReleaseDate` TEXT, PRIMARY KEY(`id`))"
    }
}
