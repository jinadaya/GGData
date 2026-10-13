package com.royaal.collections.ui.list

import com.royaal.arch.mvi.InnerEvent
import com.royaal.arch.mvi.ScreenState
import com.royaal.arch.mvi.SideEffect
import com.royaal.arch.mvi.UserEvent
import com.royaal.collections.domain.CollectionKey
import com.royaal.collections.ui.common.UiCollection
import com.royaal.entities.data.GamePlayStatus

/** @param collections favourites first, then the user's ones: pinned first. */
internal data class CollectionsScreenState(
    val collections: List<UiCollection> = emptyList(),
    val isLoading: Boolean = true,
) : ScreenState {

    /** Favourites (always there) and pinned collections. */
    val topCollections: List<UiCollection>
        get() = collections.filter { it.key == CollectionKey.Favourites || it.pinned }

    val otherCollections: List<UiCollection>
        get() = collections.filter { it.key != CollectionKey.Favourites && !it.pinned }

    /** Nothing but favourites: offer to start with status collections. */
    val isEmpty: Boolean
        get() = !isLoading && collections.none { it.key != CollectionKey.Favourites }

    /** Statuses the user has no collection for yet, in their natural order. */
    val statusesToAdd: List<GamePlayStatus>
        get() {
            val added = collections.mapNotNull { (it.key as? CollectionKey.Status)?.status }.toSet()
            return GamePlayStatus.entries.filter { it !in added }
        }

    /** Lowercased, to tell a taken name regardless of case. */
    val customNames: Set<String>
        get() = collections.filter { it.key is CollectionKey.Custom }.map { it.name.lowercase() }.toSet()
}

internal sealed interface CollectionsUserEvent : UserEvent {
    data class CollectionClicked(val key: CollectionKey) : CollectionsUserEvent
    data class CreateCollection(val name: String) : CollectionsUserEvent
    data class AddStatusCollections(val statuses: List<GamePlayStatus>) : CollectionsUserEvent
}

internal sealed interface CollectionsSideEffect : SideEffect {
    data class OpenCollection(val key: CollectionKey) : CollectionsSideEffect
}

internal sealed interface CollectionsInnerEvent : InnerEvent {
    data class UpdateCollections(val collections: List<UiCollection>) : CollectionsInnerEvent
}
