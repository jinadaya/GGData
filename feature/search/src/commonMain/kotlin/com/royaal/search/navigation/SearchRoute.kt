package com.royaal.search.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

/** Search over cached games and IGDB. Opened from [com.royaal.search.ui.SearchButton]. */
@Serializable
data object SearchRoute : NavKey
