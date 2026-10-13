package com.royaal.collections.ui.details

import com.royaal.arch.mvi.InnerEvent
import com.royaal.arch.mvi.ScreenState
import com.royaal.arch.mvi.SideEffect
import com.royaal.arch.mvi.UserEvent
import com.royaal.collections.domain.CollectionKey
import com.royaal.entities.ui.UiGame

/**
 * @param key resolved: a status collection opened by id has [CollectionKey.Status] here.
 * @param title the user's title of a [CollectionKey.Custom] collection; others are titled by [key].
 * @param collectionId the user's collection row; `null` for favourites and a status collection
 *   the user hasn't added - those can't be pinned or deleted.
 */
internal data class CollectionScreenState(
    val key: CollectionKey,
    val title: String = "",
    val collectionId: Long? = null,
    val pinned: Boolean = false,
    val games: List<UiGame> = emptyList(),
    val isLoading: Boolean = true,
) : ScreenState {
    val canEdit: Boolean get() = collectionId != null
}

internal sealed interface CollectionUserEvent : UserEvent {
    data object TogglePin : CollectionUserEvent
    data object Delete : CollectionUserEvent
    data class RemoveGame(val gameId: Long) : CollectionUserEvent
    data object UndoRemove : CollectionUserEvent
}

internal sealed interface CollectionSideEffect : SideEffect {
    data object Close : CollectionSideEffect
    data object ShowRemoved : CollectionSideEffect
}

internal sealed interface CollectionInnerEvent : InnerEvent {
    data class Update(
        val key: CollectionKey,
        val title: String,
        val collectionId: Long?,
        val pinned: Boolean,
        val games: List<UiGame>,
    ) : CollectionInnerEvent
}
