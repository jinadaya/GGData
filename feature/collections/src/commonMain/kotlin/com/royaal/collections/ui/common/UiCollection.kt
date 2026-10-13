package com.royaal.collections.ui.common

import androidx.compose.runtime.Composable
import androidx.navigation3.runtime.NavKey
import com.royaal.collections.domain.CollectionKey
import com.royaal.collections.navigation.CollectionRoute
import com.royaal.collections.navigation.FavouritesRoute
import com.royaal.collections.navigation.StatusCollectionRoute
import com.royaal.entities.data.GamePlayStatus
import com.royaal.entities.ui.UiGame
import ggdata.feature.collections.generated.resources.Res
import ggdata.feature.collections.generated.resources.createNameTaken
import ggdata.feature.collections.generated.resources.createNameTooLong
import ggdata.feature.collections.generated.resources.favourites
import ggdata.feature.collections.generated.resources.statusBacklog
import ggdata.feature.collections.generated.resources.statusCompleted
import ggdata.feature.collections.generated.resources.statusDropped
import ggdata.feature.collections.generated.resources.statusPlaying
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

/** A collection as shown on the collections screen. [name] is set for user-created ones only. */
internal data class UiCollection(
    val key: CollectionKey,
    val name: String,
    val pinned: Boolean,
    val gamesCount: Int,
    val preview: List<UiGame>,
)

/** The user's title, or the localized one of a favourites / status collection. */
@Composable
internal fun CollectionKey.title(name: String): String = when (this) {
    CollectionKey.Favourites -> stringResource(Res.string.favourites)
    is CollectionKey.Status -> stringResource(status.title)
    is CollectionKey.Custom -> name
}

internal val GamePlayStatus.title: StringResource
    get() = when (this) {
        GamePlayStatus.BACKLOG -> Res.string.statusBacklog
        GamePlayStatus.PLAYING -> Res.string.statusPlaying
        GamePlayStatus.COMPLETED -> Res.string.statusCompleted
        GamePlayStatus.DROPPED -> Res.string.statusDropped
    }

internal val CollectionKey.route: NavKey
    get() = when (this) {
        CollectionKey.Favourites -> FavouritesRoute
        is CollectionKey.Status -> StatusCollectionRoute(status)
        is CollectionKey.Custom -> CollectionRoute(id)
    }

/** Longer names don't fit a collection card anyway. */
internal const val MAX_COLLECTION_NAME_LENGTH = 40

/**
 * Why [name] can't name a collection, `null` if it can (or is empty: there's nothing to say yet).
 *
 * @param takenNames lowercased names of the user's other collections.
 */
@Composable
internal fun collectionNameError(name: String, takenNames: Set<String>): String? {
    val trimmedName = name.trim()
    return when {
        trimmedName.length > MAX_COLLECTION_NAME_LENGTH ->
            stringResource(Res.string.createNameTooLong, MAX_COLLECTION_NAME_LENGTH)
        trimmedName.lowercase() in takenNames -> stringResource(Res.string.createNameTaken)
        else -> null
    }
}
