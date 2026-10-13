package com.royaal.search.ui

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.plus
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.royaal.designsystem.components.button.GGIconButton
import com.royaal.designsystem.components.button.GGIconButtonType
import com.royaal.designsystem.components.input.GGSearchInput
import com.royaal.designsystem.components.screen.LocalContentPadding
import com.royaal.designsystem.components.skeleton.rememberSkeletonShimmer
import com.royaal.designsystem.icons.AppIcons
import com.royaal.designsystem.theme.AppTheme
import com.royaal.designsystem.theme.textColors
import com.royaal.designsystem.uikit.GameCard
import com.royaal.designsystem.uikit.GameCardType
import com.royaal.designsystem.uikit.SkeletonGameCard
import com.royaal.designsystem.util.BasePreview
import com.royaal.designsystem.util.clearFocusOnScroll
import com.royaal.designsystem.util.clearFocusOnTap
import com.royaal.entities.ui.UiGame
import com.royaal.entities.ui.testGame
import com.royaal.game.navigation.GameRoute
import com.royaal.game.ui.library.AddToLibrarySheet
import com.royaal.game.ui.library.rememberLibrarySavedSnackbar
import com.royaal.navigation.LocalNavigator
import ggdata.feature.search.generated.resources.Res
import ggdata.feature.search.generated.resources.emptyDescription
import ggdata.feature.search.generated.resources.emptyTitle
import ggdata.feature.search.generated.resources.nothingFound
import ggdata.feature.search.generated.resources.remoteSearchFailed
import ggdata.feature.search.generated.resources.searchHint
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

private const val SKELETON_RESULTS_COUNT = 5

@Composable
internal fun SearchScreen() {
    val viewModel = koinViewModel<SearchScreenViewModel>()
    val state = viewModel.state.collectAsStateWithLifecycle()
    val navigator = LocalNavigator.current
    var addingGameId by rememberSaveable { mutableStateOf<Long?>(null) }
    val onSaved = rememberLibrarySavedSnackbar()

    SearchContent(
        state = state.value,
        onBackClicked = navigator::back,
        onQueryChanged = { viewModel.sendEvent(SearchUserEvent.QueryChanged(it)) },
        onGameClicked = { navigator.navigate(GameRoute(it.id)) },
        onAddGameClicked = { addingGameId = it.id },
    )

    state.value.games.firstOrNull { it.id == addingGameId }?.let { game ->
        AddToLibrarySheet(
            game = game,
            onDismissRequest = { addingGameId = null },
            onSaved = onSaved,
        )
    }
}

@Composable
private fun SearchContent(
    state: SearchScreenState,
    onBackClicked: () -> Unit,
    onQueryChanged: (String) -> Unit,
    onGameClicked: (UiGame) -> Unit,
    onAddGameClicked: (UiGame) -> Unit,
    modifier: Modifier = Modifier,
) {
    // Only while skeletons are shown: a remembered shimmer keeps animating, needed or not.
    val shimmer = if (state.isLoading && state.games.isEmpty()) rememberSkeletonShimmer() else null
    Column(
        modifier = modifier
            // Full size, so that taps anywhere below the results reach clearFocusOnTap.
            .fillMaxSize()
            .statusBarsPadding()
            // The iOS keyboard can't be closed by itself: tapping aside or scrolling closes it.
            .clearFocusOnTap()
            .clearFocusOnScroll(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        // Opened to type: the keyboard comes up at once, but not again on coming back from a game.
        val focusRequester = remember { FocusRequester() }
        var isFocusRequested by rememberSaveable { mutableStateOf(false) }
        LaunchedEffect(Unit) {
            if (!isFocusRequested) {
                isFocusRequested = true
                focusRequester.requestFocus()
            }
        }
        Row(
            modifier = Modifier.padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            GGIconButton(
                imageVector = AppIcons.arrowBack,
                type = GGIconButtonType.OUTLINED,
                onClick = onBackClicked,
            )
            GGSearchInput(
                modifier = Modifier
                    .weight(1f)
                    .focusRequester(focusRequester),
                initialValue = state.query,
                hint = stringResource(Res.string.searchHint),
                loading = state.isLoading,
                subtitle = if (state.isRemoteFailed) stringResource(Res.string.remoteSearchFailed) else null,
                onSearchClicked = {},
                onValueChanged = onQueryChanged,
            )
        }
        if (state.isNothingFound) {
            Text(
                modifier = Modifier.padding(horizontal = 16.dp),
                text = stringResource(Res.string.nothingFound),
                style = AppTheme.typography.bodyMedium,
                color = AppTheme.textColors.secondary,
            )
        }
        // Typing the first letters, the hint gives way to the results smoothly, and back on clearing.
        Crossfade(
            modifier = Modifier.weight(1f),
            targetState = state.isIdle,
            label = "SearchIdle",
        ) { isIdle ->
            if (isIdle) {
                SearchEmptyState(modifier = Modifier.fillMaxSize())
                return@Crossfade
            }
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(4.dp),
                contentPadding = LocalContentPadding.current + PaddingValues(horizontal = 16.dp),
            ) {
                if (shimmer != null) {
                    items(count = SKELETON_RESULTS_COUNT, key = { "skeleton$it" }) {
                        SkeletonGameCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .animateItem(),
                            type = GameCardType.Compact,
                            shimmer = shimmer,
                        )
                    }
                }
                items(items = state.games, key = { it.id }) { game ->
                    GameCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .animateItem(),
                        game = game,
                        type = GameCardType.Compact,
                        onClick = onGameClicked,
                        onAddClicked = onAddGameClicked,
                    )
                }
            }
        }
    }
}

/** Shown before anything is typed, centered in the space above the bottom bar. */
@Composable
private fun SearchEmptyState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(LocalContentPadding.current.calculateBottomPadding().let { PaddingValues(bottom = it) })
            .padding(horizontal = 32.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            modifier = Modifier.size(48.dp),
            imageVector = AppIcons.search,
            contentDescription = null,
            tint = AppTheme.textColors.secondary,
        )
        Text(
            text = stringResource(Res.string.emptyTitle),
            style = AppTheme.typography.headlineMedium,
            textAlign = TextAlign.Center,
        )
        Text(
            text = stringResource(Res.string.emptyDescription),
            style = AppTheme.typography.bodyMedium,
            color = AppTheme.textColors.secondary,
            textAlign = TextAlign.Center,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun Preview() = BasePreview {
    SearchContent(
        state = SearchScreenState(
            query = "mine",
            games = listOf(UiGame.testGame.copy(id = 1), UiGame.testGame.copy(id = 2)),
            isLoading = true,
        ),
        onBackClicked = {},
        onQueryChanged = {},
        onGameClicked = {},
        onAddGameClicked = {},
    )
    SearchContent(
        state = SearchScreenState(query = "mine", isLoading = true),
        onBackClicked = {},
        onQueryChanged = {},
        onGameClicked = {},
        onAddGameClicked = {},
    )
    SearchContent(
        state = SearchScreenState(),
        onBackClicked = {},
        onQueryChanged = {},
        onGameClicked = {},
        onAddGameClicked = {},
    )
}
