package com.royaal.designsystem.components.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.royaal.designsystem.components.BottomBarEntry
import com.royaal.designsystem.components.BottomBarType
import com.royaal.designsystem.components.GGBottomBar
import com.royaal.designsystem.components.snackbar.GGSnackbarHost
import com.royaal.designsystem.components.snackbar.LocalSnackbarHostState

/**
 * Часть экрана, перекрытая плавающим [GGBottomBar]. Контент рисуется под баром, поэтому
 * скроллящиеся списки должны добавлять её как `contentPadding` (`LazyColumn(contentPadding = ...)`).
 *
 * Системные бары сюда не входят - экраны обрабатывают их сами. Исключение: пока бар показан, он
 * стоит над навигационной панелью системы, и его отступ уже включает её высоту. На экранах без бара
 * здесь ноль.
 */
val LocalContentPadding: ProvidableCompositionLocal<PaddingValues> = compositionLocalOf { PaddingValues() }

@Composable
fun AppScreen(
    modifier: Modifier = Modifier,
    snackbarState: SnackbarHostState,
    showBottomBar: Boolean,
    currentDestination: BottomBarEntry?,
    destinations: List<BottomBarEntry>,
    onDestinationClicked: (BottomBarEntry) -> Unit,
    content: @Composable () -> Unit,
) {
    Scaffold(
        modifier = modifier,
        // Screens are edge-to-edge and handle system bars themselves.
        contentWindowInsets = WindowInsets(0),
        snackbarHost = {
            GGSnackbarHost(snackbarState)
        },
        bottomBar = {
            AnimatedVisibility(
                visible = showBottomBar,
                enter = slideInVertically { it } + fadeIn(),
                exit = slideOutVertically { it } + fadeOut(),
            ) {
                Box(modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .navigationBarsPadding()
                ) {
                    GGBottomBar(
                        selected = currentDestination,
                        destinations = destinations,
                        type = BottomBarType.Solid,
                        onDestinationClicked = onDestinationClicked,
                    )
                }
            }
        },
        // Scaffold lays the content out full-size under the bar; padding is only reported.
        content = { padding ->
            CompositionLocalProvider(
                LocalSnackbarHostState provides snackbarState,
                LocalContentPadding provides padding,
            ) {
                content()
            }
        },
    )
}
