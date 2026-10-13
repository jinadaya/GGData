package com.royaal.search.ui

import androidx.compose.runtime.Composable
import com.royaal.designsystem.components.button.GGIconButton
import com.royaal.designsystem.components.button.GGIconButtonType
import com.royaal.designsystem.icons.AppIcons
import com.royaal.navigation.LocalNavigator
import com.royaal.search.navigation.SearchRoute

/** Opens search on top of the current tab. Sits at the top end of every tab's first screen. */
@Composable
fun SearchButton() {
    val navigator = LocalNavigator.current
    GGIconButton(
        imageVector = AppIcons.search,
        type = GGIconButtonType.OUTLINED,
        onClick = { navigator.navigate(SearchRoute) },
    )
}
