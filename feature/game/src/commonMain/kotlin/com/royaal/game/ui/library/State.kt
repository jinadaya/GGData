package com.royaal.game.ui.library

import com.royaal.arch.mvi.InnerEvent
import com.royaal.arch.mvi.ScreenState
import com.royaal.arch.mvi.SideEffect
import com.royaal.arch.mvi.UserEvent
import com.royaal.entities.data.GamePlayStatus
import com.royaal.game.domain.GameLibraryEntry

/** @param entry where the game already is; `null` until loaded. */
internal data class LibraryState(
    val entry: GameLibraryEntry? = null,
) : ScreenState

internal sealed interface LibraryUserEvent : UserEvent {
    data class Save(
        val status: GamePlayStatus?,
        val favourite: Boolean,
        val collectionIds: Set<Long>,
    ) : LibraryUserEvent
}

internal sealed interface LibrarySideEffect : SideEffect

internal sealed interface LibraryInnerEvent : InnerEvent {
    data class Update(val entry: GameLibraryEntry) : LibraryInnerEvent
}
