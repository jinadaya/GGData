package com.royaal.data.db.search

/**
 * Brings game titles and user queries to the same normalized form, which `game.searchText` and
 * its FTS4 index ([com.royaal.data.db.entity.GameFtsEntity]) are built from:
 * - lowercase in every script: SQLite's own `LIKE`/`lower()` fold ASCII only, so `ВЕДЬМАК` never
 *   matched `ведьмак` there;
 * - Latin diacritics dropped (`Pokémon` → `pokemon`), `ё` → `е`. Other letters stay as they are,
 *   so `й` is not turned into `и`;
 * - apostrophes dropped (`Assassin’s` → `assassins`); any other non-letter/digit (`&`, `:`, `-`)
 *   separates words;
 * - numbers 1..39 are indexed both in Arabic and Roman form, so `witcher iii` finds `Witcher 3`
 *   and `final fantasy 7` finds `Final Fantasy VII`.
 *
 * Done here rather than by the FTS tokenizer: `unicode61` removes diacritics from Latin letters
 * only and knows nothing about `ё`, apostrophes or numerals.
 */
object GameSearchText {

    /** Normalized words of [text], in order. Empty if [text] has no letters or digits. */
    fun words(text: String): List<String> {
        val normalized = StringBuilder(text.length)
        for (char in text.lowercase()) {
            when {
                char in APOSTROPHES -> Unit
                char == COMBINING_BREVE && normalized.lastOrNull() == 'и' -> normalized.setLast('й')
                char in COMBINING_MARKS -> Unit
                char == 'ё' -> normalized.append('е')
                else -> {
                    val folded = FOLDED_LATIN[char]
                    when {
                        folded != null -> normalized.append(folded)
                        char.isLetterOrDigit() -> normalized.append(char)
                        else -> normalized.append(' ')
                    }
                }
            }
        }
        return normalized.split(' ').filter { it.isNotEmpty() }
    }

    /**
     * Value for `game.searchText`: words of [name] first (search ranks titles starting with the
     * query higher), then the words of [alternativeNames] and the other form of every numeral.
     */
    fun index(name: String, alternativeNames: List<String> = emptyList()): String {
        val nameWords = words(name)
        val alternativeWords = alternativeNames.map(::words)
        val numeralTwins = (alternativeWords + listOf(nameWords)).flatMap(::numeralTwins)
        val extraWords = (alternativeWords.flatten() + numeralTwins).distinct() - nameWords.toSet()
        return (nameWords + extraWords).joinToString(" ")
    }

    /** FTS4 `MATCH` expression: every word of the query must start some word of the index. */
    fun prefixMatch(queryWords: List<String>): String = queryWords.joinToString(" ") { "$it*" }

    /**
     * [queryWords] with numerals in their other form (`witcher iii` → `witcher 3`), or `null` if
     * there are none. For remote search, which doesn't know that the two are the same.
     */
    fun withSwappedNumerals(queryWords: List<String>): String? {
        val twins = queryWords.mapIndexed { index, word -> numeralTwin(word, isFirst = index == 0) }
        if (twins.all { it == null }) return null
        return queryWords.zip(twins) { word, twin -> twin ?: word }.joinToString(" ")
    }

    private fun numeralTwins(titleWords: List<String>): List<String> =
        titleWords.mapIndexedNotNull { index, word -> numeralTwin(word, isFirst = index == 0) }

    /**
     * Roman for an Arabic number and vice versa. A leading `i` is the pronoun far more often than
     * the number (`I Am Bread`), so it's left alone.
     */
    private fun numeralTwin(word: String, isFirst: Boolean): String? {
        if (isFirst && word == "i") return null
        val number = word.toIntOrNull()
        if (number != null) {
            return if (number in 1..ROMAN_NUMERALS.size && word == number.toString()) ROMAN_NUMERALS[number - 1] else null
        }
        val index = ROMAN_NUMERALS.indexOf(word)
        return if (index >= 0) (index + 1).toString() else null
    }

    private fun StringBuilder.setLast(char: Char) = set(lastIndex, char)

    private const val COMBINING_BREVE = '\u0306'
    private val COMBINING_MARKS = '\u0300'..'\u036F'
    private val APOSTROPHES = setOf('\'', '’', '‘', 'ʼ', '`', '´', '′')

    /** `i`..`xxxix`. Higher ones are rare in titles and would turn years into noise. */
    private val ROMAN_NUMERALS: List<String> = (1..39).map { number ->
        "x".repeat(number / 10) + listOf("", "i", "ii", "iii", "iv", "v", "vi", "vii", "viii", "ix")[number % 10]
    }

    /** Lowercase Latin letters with diacritics (and ligatures) → their plain form. */
    private val FOLDED_LATIN: Map<Char, String> = buildMap {
        fun fold(chars: String, plain: String) = chars.forEach { put(it, plain) }
        fold("àáâãäåāăąǎ", "a")
        fold("çćĉċč", "c")
        fold("ďđð", "d")
        fold("èéêëēĕėęě", "e")
        fold("ĝğġģ", "g")
        fold("ĥħ", "h")
        fold("ìíîïĩīĭįıǐ", "i")
        fold("ĵ", "j")
        fold("ķ", "k")
        fold("ĺļľŀł", "l")
        fold("ñńņňŉ", "n")
        fold("òóôõöøōŏőǒ", "o")
        fold("ŕŗř", "r")
        fold("śŝşšș", "s")
        fold("ţťŧț", "t")
        fold("ùúûüũūŭůűųǔ", "u")
        fold("ŵ", "w")
        fold("ýÿŷ", "y")
        fold("źżž", "z")
        put('ß', "ss")
        put('æ', "ae")
        put('œ', "oe")
        put('þ', "th")
    }
}
