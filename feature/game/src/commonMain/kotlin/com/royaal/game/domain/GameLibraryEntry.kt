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
