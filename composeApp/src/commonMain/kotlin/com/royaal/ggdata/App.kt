package com.royaal.ggdata

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.royaal.designsystem.components.screen.AppScreen
import com.royaal.designsystem.theme.AppTheme
import com.royaal.ggdata.navigation.BottomTab
import com.royaal.ggdata.navigation.bottomTabRoutes
import com.royaal.ggdata.navigation.bottomTabs
import com.royaal.navigation.LocalNavigator
import com.royaal.navigation.NavHost

@Composable
fun App() = AppTheme {
    val snackbarState = remember { SnackbarHostState() }

    NavHost(tabs = bottomTabRoutes) { showBottomBar, content ->
        val navigator = LocalNavigator.current
        AppScreen(
            snackbarState = snackbarState,
            showBottomBar = showBottomBar && bottomTabs.size > 1,
            currentDestination = bottomTabs.first { it.route == navigator.currentTab },
            destinations = bottomTabs,
            onDestinationClicked = { navigator.selectTab((it as BottomTab).route) },
            content = content,
        )
    }
}
