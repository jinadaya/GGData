package com.royaal.data.db.search

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class GameSearchTextTest {

    @Test
    fun lowercasesEveryScript() {
        assertEquals(listOf("ведьмак", "the", "witcher"), GameSearchText.words("ВЕДЬМАК The WITCHER"))
    }

    @Test
    fun removesLatinDiacritics() {
        assertEquals(listOf("pokemon"), GameSearchText.words("Pokémon"))
        assertEquals(listOf("pokemon"), GameSearchText.words("Poke\u0301mon"))
        assertEquals(listOf("okami", "strasse"), GameSearchText.words("Ōkami Straße"))
    }

    @Test
    fun treatsYoAsYe() {
        assertEquals(listOf("еж"), GameSearchText.words("Ёж"))
    }

    @Test
    fun keepsShortI() {
        assertEquals(listOf("мой", "мой"), GameSearchText.words("Мой Мои\u0306"))
    }

    @Test
    fun dropsApostrophesAndSplitsOnPunctuation() {
        assertEquals(listOf("assassins", "creed"), GameSearchText.words("Assassin’s Creed"))
        assertEquals(listOf("assassins"), GameSearchText.words("assassin's"))
        assertEquals(listOf("ratchet", "clank"), GameSearchText.words("Ratchet & Clank"))
        assertEquals(listOf("halo", "combat", "evolved"), GameSearchText.words("Halo: Combat-Evolved"))
        assertEquals(emptyList(), GameSearchText.words(" & ! "))
    }

    @Test
    fun indexStartsWithNameThenAlternativeNamesAndNumeralTwins() {
        assertEquals(
            "the witcher 3 wild hunt ведьмак дикая охота iii",
            GameSearchText.index("The Witcher 3: Wild Hunt", listOf("Ведьмак 3: Дикая Охота")),
        )
        assertEquals("final fantasy vii 7", GameSearchText.index("Final Fantasy VII"))
        assertEquals("grand theft auto v gta 5", GameSearchText.index("Grand Theft Auto V", listOf("GTA 5")))
    }

    @Test
    fun leadingIIsNotANumeral() {
        assertEquals("i am bread", GameSearchText.index("I Am Bread"))
        assertEquals("star wars episode i 1", GameSearchText.index("Star Wars Episode I"))
    }

    @Test
    fun onlyNumbersUpTo39AreTwinned() {
        assertEquals("fifa 22 xxii", GameSearchText.index("FIFA 22"))
        assertEquals("cyberpunk 2077", GameSearchText.index("Cyberpunk 2077"))
        assertEquals("agent 007", GameSearchText.index("Agent 007"))
    }

    @Test
    fun prefixMatch() {
        assertEquals("ведь* охота*", GameSearchText.prefixMatch(GameSearchText.words("ведь охота")))
    }

    @Test
    fun withSwappedNumerals() {
        assertEquals("witcher 3", GameSearchText.withSwappedNumerals(listOf("witcher", "iii")))
        assertEquals("gta v", GameSearchText.withSwappedNumerals(listOf("gta", "5")))
        assertNull(GameSearchText.withSwappedNumerals(listOf("i", "am", "bread")))
        assertNull(GameSearchText.withSwappedNumerals(listOf("minecraft")))
    }
}
