package com.royaal.ggdata.navigation

import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation3.runtime.NavKey
import com.royaal.collections.navigation.CollectionRoute
import com.royaal.collections.navigation.CollectionsRoute
import com.royaal.designsystem.components.BottomBarEntry
import com.royaal.designsystem.icons.AppIcons
import com.royaal.main.navigation.MainRoute
import com.royaal.search.navigation.SearchRoute

internal class BottomTab(
    val route: NavKey,
    override val title: String,
    override val icon: ImageVector,
) : BottomBarEntry {
    override val key: Any get() = route
}

/** Bottom navigation tabs, in display order. The first one is the start tab. */
internal val bottomTabs: List<BottomTab> = listOf(
    BottomTab(route = MainRoute, title = "Home", icon = AppIcons.home),
    BottomTab(route = SearchRoute, title = "Search", icon = AppIcons.search),
    BottomTab(route = CollectionsRoute, title = "Collections", icon = AppIcons.collections)
)

internal val bottomTabRoutes: List<NavKey> = bottomTabs.map { it.route }
