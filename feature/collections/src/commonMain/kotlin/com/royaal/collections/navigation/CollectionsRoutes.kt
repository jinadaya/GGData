package com.royaal.collections.navigation

import androidx.navigation3.runtime.NavKey
import com.royaal.entities.data.GamePlayStatus
import kotlinx.serialization.Serializable

/** The collections tab. */
@Serializable
data object CollectionsRoute : NavKey

/** Games marked as favourite. Always available, even with no collections. */
@Serializable
data object FavouritesRoute : NavKey

/** Games with [status]. Opens even if the user hasn't added this status collection. */
@Serializable
data class StatusCollectionRoute(val status: GamePlayStatus) : NavKey

/** A collection by its id: user-created or a status one. */
@Serializable
data class CollectionRoute(val id: Long) : NavKey
