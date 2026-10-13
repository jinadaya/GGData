package com.royaal.game.domain

import com.royaal.entities.data.GamePlayStatus

/** Where a game is in the user's library: what the add-to-library sheet shows and edits. */
internal data class GameLibraryEntry(
    val status: GamePlayStatus?,
    val favourite: Boolean,
    /** The user's own collections, oldest first; status ones follow [status] instead. */
    val collections: List<LibraryCollection>,
) {
    val collectionIds: Set<Long>
        get() = collections.filter { it.containsGame }.mapTo(mutableSetOf()) { it.id }
}

internal data class LibraryCollection(
    val id: Long,
    val name: String,
    val containsGame: Boolean,
)

/**
 * What the user keeps on a game in their journal: what the journal entry sheet shows and edits.
 *
 * @param rating the user's own, 1 to 10.
 */
internal data class JournalEntry(
    val status: GamePlayStatus?,
    val hoursPlayed: Int?,
    val rating: Int?,
    val note: String?,
)
