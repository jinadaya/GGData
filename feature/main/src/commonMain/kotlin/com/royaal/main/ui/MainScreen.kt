package com.royaal.main.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.Crossfade
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.plus
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.royaal.arch.mvi.EffectHandler
import com.royaal.collections.navigation.CollectionRoute
import com.royaal.collections.navigation.CollectionsRoute
import com.royaal.collections.navigation.StatusCollectionRoute
import com.royaal.designsystem.components.button.GGButton
import com.royaal.designsystem.components.button.GGButtonType
import com.royaal.designsystem.components.screen.LocalContentPadding
import com.royaal.designsystem.components.skeleton.SkeletonText
import com.royaal.designsystem.components.skeleton.rememberSkeletonShimmer
import com.royaal.designsystem.components.tag.GGSkeletonTag
import com.royaal.designsystem.components.tag.GGTag
import com.royaal.designsystem.components.tag.GGTagRow
import com.royaal.designsystem.components.tag.GGTagType
import com.royaal.designsystem.theme.AppTheme
import com.royaal.designsystem.theme.textColors
import com.royaal.designsystem.uikit.CollectionCard
import com.royaal.designsystem.uikit.GameCard
import com.royaal.designsystem.uikit.GameCardType
import com.royaal.designsystem.uikit.SkeletonGameCard
import com.royaal.designsystem.util.BasePreview
import com.royaal.designsystem.util.animatedItem
import com.royaal.designsystem.util.click
import com.royaal.designsystem.util.horizontalScrollHint
import com.royaal.entities.data.GamePlayStatus
import com.royaal.entities.ui.UiGame
import com.royaal.entities.ui.UiGameCollection
import com.royaal.entities.ui.UiGenre
import com.royaal.entities.ui.testCollection
import com.royaal.entities.ui.testGame
import com.royaal.game.navigation.GameRoute
import com.royaal.game.ui.library.AddToLibrarySheet
import com.royaal.game.ui.library.rememberLibrarySavedSnackbar
import com.royaal.navigation.LocalNavigator
import com.royaal.navigation.Navigator
import com.royaal.search.ui.SearchButton
import ggdata.feature.main.generated.resources.Res
import ggdata.feature.main.generated.resources.*
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

private const val SKELETON_RECOMMENDATIONS_COUNT = 4
private const val FOR_YOU_LOADING = "loading"
private const val FOR_YOU_FAILED = "failed"
private const val SKELETON_BACKLOG_COUNT = 3
private val SKELETON_GENRE_WIDTHS = listOf(88.dp, 56.dp, 80.dp, 72.dp, 64.dp)

/** Side padding of the content; horizontal scrolls stay edge to edge and pad their items instead. */
private val ScreenHorizontalPadding = 16.dp

@Composable
internal fun MainScreen() {
    val viewModel = koinViewModel<MainScreenViewModel>()
    val state = viewModel.state.collectAsStateWithLifecycle()
    val uiPaddings = LocalContentPadding.current
    val navigator = LocalNavigator.current
    val onSaved = rememberLibrarySavedSnackbar()

    EffectHandler(viewModel) {
        when (it) {
            is MainSideEffect.NavigateTo -> navigator.navigateTo(it.destination)
        }
    }

    val onGameClicked: (UiGame) -> Unit = { viewModel.sendEvent(MainUserEvent.GameClicked(it)) }
    val onAddGameClicked: (UiGame) -> Unit = { viewModel.sendEvent(MainUserEvent.AddGameClicked(it)) }
    val onGameLongClicked: (UiGame) -> Unit = { viewModel.sendEvent(MainUserEvent.GameLongClicked(it)) }

    val mode = when {
        // Not the onboarding: the library is most likely there, it's just being read.
        state.value.isLibraryLoading -> MainMode.LOADING
        state.value.isEmptyState -> MainMode.ONBOARDING
        else -> MainMode.LIBRARY
    }
    Crossfade(targetState = mode, label = "MainMode") { shownMode ->
        when (shownMode) {
            MainMode.LOADING -> {
                LoadingMainScreen()
            }
            MainMode.ONBOARDING -> {
                EmptyMainScreen(
                    recommendations = state.value.recommendations,
                    genres = state.value.genres,
                    selectedGenreId = state.value.selectedGenreId,
                    isGenresLoading = state.value.isGenresLoading,
                    isRecommendationsLoading = state.value.isRecommendationsLoading,
                    isGenresFailed = state.value.isGenresFailed,
                    isRecommendationsFailed = state.value.isRecommendationsFailed,
                    onRetryClicked = { viewModel.sendEvent(MainUserEvent.RetryRecommendations) },
                    onGenreClicked = { viewModel.sendEvent(MainUserEvent.GenreClicked(it.id)) },
                    onGameClicked = onGameClicked,
                    onAddGameClicked = onAddGameClicked,
                    onGameLongClicked = onGameLongClicked,
                )
            }
            else -> {
                LazyColumn(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    // Unlike the bottom bar, the status bar isn't in the content padding.
                    contentPadding = uiPaddings + PaddingValues(
                        top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding() + 8.dp,
                        bottom = 8.dp,
                    ),
                ) {
                    animatedItem(key = "search") {
                        SearchRow(Modifier.padding(horizontal = ScreenHorizontalPadding))
                    }
                    animatedItem(key = "forYou") {
                        ForYou(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = ScreenHorizontalPadding),
                            game = state.value.latestGame,
                            isLoading = state.value.isLatestGameLoading,
                            isFailed = state.value.isLatestGameFailed,
                            onRetryClicked = { viewModel.sendEvent(MainUserEvent.RetryLatestGame) },
                            onClicked = onGameClicked,
                            onAddClicked = onAddGameClicked,
                            onFavouriteClicked = { viewModel.sendEvent(MainUserEvent.AddGameToFavClicked(it)) },
                            onLongClicked = onGameLongClicked,
                        )
                    }
                    backlog(
                        backlogGames = state.value.backlogGames,
                        onSeeAllClicked = { viewModel.sendEvent(MainUserEvent.SeeBacklog) },
                        onGameClicked = onGameClicked,
                        onAddGameClicked = onAddGameClicked,
                        onGameLongClicked = onGameLongClicked,
                    )
                    animatedItem(key = "collections") {
                        Collections(
                            modifier = Modifier.fillMaxWidth(),
                            onSeeAllClicked = { viewModel.sendEvent(MainUserEvent.SeeCollections) },
                            collections = state.value.collections,
                            onCollectionClicked = { viewModel.sendEvent(MainUserEvent.CollectionClicked(it)) }
                        )
                    }
                }
            }
        }
    }

    val dismissDialog = { viewModel.sendEvent(MainUserEvent.DialogDismissed) }
    state.value.addingGame?.let { game ->
        AddToLibrarySheet(
            game = game,
            onDismissRequest = dismissDialog,
            onSaved = onSaved,
        )
    }
    state.value.detailsGame?.let { game ->
        GameDetailsDialog(
            game = game,
            onAddClicked = onAddGameClicked,
            onDismissRequest = dismissDialog,
        )
    }
}

internal fun Navigator.navigateTo(
    destination: NavDestination
) {
    when (destination) {
        // A tab root: switches to the collections tab.
        NavDestination.AllCollections -> navigate(CollectionsRoute)
        NavDestination.Backlog -> navigate(StatusCollectionRoute(GamePlayStatus.BACKLOG))
        is NavDestination.Collection -> navigate(CollectionRoute(destination.id))
        is NavDestination.Game -> navigate(GameRoute(destination.id))
    }
}

/** What the onboarding shows under "Played any of these?". */
private sealed interface Recommendations {
    data object Failed : Recommendations
    data object Loading : Recommendations
    data class Loaded(val games: List<UiGame>) : Recommendations
}

/** What Discover shows as a whole; switching between them crossfades. */
private enum class MainMode { LOADING, ONBOARDING, LIBRARY }

/** Search at the top end: the screen has no title row of its own to put it in. */
@Composable
private fun SearchRow(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End,
    ) {
        SearchButton()
    }
}

/** Looks like the library: For You and a few backlog games. */
@Composable
private fun LoadingMainScreen(modifier: Modifier = Modifier) {
    val shimmer = rememberSkeletonShimmer()
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(LocalContentPadding.current)
            .padding(
                top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding() + 8.dp,
                start = ScreenHorizontalPadding,
                end = ScreenHorizontalPadding,
            ),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        SearchRow()
        SkeletonText(AppTheme.typography.headlineMedium, Modifier.width(120.dp), shimmer = shimmer)
        SkeletonText(AppTheme.typography.bodyMedium, Modifier.fillMaxWidth(0.7f), shimmer = shimmer)
        SkeletonGameCard(
            modifier = Modifier.fillMaxWidth(),
            type = GameCardType.Detailed,
            shimmer = shimmer,
        )
        Spacer(Modifier.height(4.dp))
        SkeletonText(AppTheme.typography.headlineMedium, Modifier.width(96.dp), shimmer = shimmer)
        repeat(SKELETON_BACKLOG_COUNT) {
            SkeletonGameCard(
                modifier = Modifier.fillMaxWidth(),
                type = GameCardType.Compact,
                shimmer = shimmer,
            )
        }
    }
}

@Composable
private fun EmptyMainScreen(
    modifier: Modifier = Modifier,
    genres: List<UiGenre>,
    selectedGenreId: Long?,
    recommendations: List<UiGame>,
    onGenreClicked: (UiGenre) -> Unit,
    onGameClicked: (UiGame) -> Unit,
    onAddGameClicked: (UiGame) -> Unit,
    onGameLongClicked: (UiGame) -> Unit,
    isGenresLoading: Boolean = false,
    isRecommendationsLoading: Boolean = false,
    isGenresFailed: Boolean = false,
    isRecommendationsFailed: Boolean = false,
    onRetryClicked: () -> Unit = {},
) {
    // Only while skeletons are shown: a remembered shimmer keeps animating, needed or not.
    val shimmer =
        if (isGenresLoading || isRecommendationsLoading) rememberSkeletonShimmer() else null
    val statusBarPaddings = WindowInsets.statusBars.asPaddingValues()
    val contentPaddings = LocalContentPadding.current.plus(
        PaddingValues(bottom = 8.dp)
    )
    Column(
        modifier = modifier.verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(4.dp),
        horizontalAlignment = Alignment.Start,
    ) {
        Column(
            modifier = Modifier.padding(horizontal = ScreenHorizontalPadding),
        ) {
            Spacer(Modifier.height(statusBarPaddings.calculateTopPadding() + 8.dp))
            Row(verticalAlignment = Alignment.Top) {
                Text(
                    modifier = Modifier.weight(1f),
                    text = stringResource(Res.string.start),
                    style = AppTheme.typography.headlineLarge,
                )
                SearchButton()
            }
            Text(
                text = stringResource(Res.string.startDesc),
                style = AppTheme.typography.bodyMedium,
                color = AppTheme.textColors.secondary,
            )
            // Without genres there's nothing to pick; the error under the games covers both.
            if (!isGenresFailed) {
                Spacer(Modifier.height(4.dp))
                Text(
                    text = stringResource(Res.string.whatYouLike),
                    style = AppTheme.typography.headlineMedium,
                )
            }
        }
        if (!isGenresFailed) GGTagRow(contentPadding = PaddingValues(horizontal = ScreenHorizontalPadding)) {
            if (isGenresLoading && shimmer != null) {
                SKELETON_GENRE_WIDTHS.forEach { width ->
                    GGSkeletonTag(width = width, shimmer = shimmer)
                }
            }
            genres.forEach { genre ->
                GGTag(
                    modifier = Modifier.click { onGenreClicked(genre) },
                    text = genre.name,
                    type = if (genre.id == selectedGenreId) GGTagType.SOLID else GGTagType.OUTLINE,
                )
            }
        }
        Column(
            modifier = Modifier.padding(horizontal = ScreenHorizontalPadding),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Spacer(Modifier.height(4.dp))
            Text(
                text = stringResource(Res.string.maybePlayed),
                style = AppTheme.typography.headlineMedium,
            )
            Text(
                text = stringResource(Res.string.maybePlayedDesc),
                style = AppTheme.typography.labelSmall,
                color = AppTheme.textColors.secondary,
            )
            val shown = when {
                isGenresFailed || isRecommendationsFailed -> Recommendations.Failed
                isRecommendationsLoading -> Recommendations.Loading
                else -> Recommendations.Loaded(recommendations)
            }
            // Another genre's games crossfade in, and the list grows or shrinks to them smoothly.
            AnimatedContent(
                targetState = shown,
                transitionSpec = { fadeIn() togetherWith fadeOut() using SizeTransform(clip = false) },
                contentAlignment = Alignment.TopCenter,
                label = "Recommendations",
            ) { target ->
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    if (target == Recommendations.Failed) {
                        LoadFailed(onRetryClicked = onRetryClicked)
                    } else if (target == Recommendations.Loading) {
                        if (shimmer != null) repeat(SKELETON_RECOMMENDATIONS_COUNT) {
                            SkeletonGameCard(
                                modifier = Modifier.fillMaxWidth(),
                                type = GameCardType.Compact,
                                shimmer = shimmer,
                            )
                        }
                    } else if (target is Recommendations.Loaded) {
                        target.games.forEach { game ->
                            GameCard(
                                game = game,
                                type = GameCardType.Compact,
                                onClick = onGameClicked,
                                onAddClicked = onAddGameClicked,
                                onLongClick = onGameLongClicked,
                            )
                        }
                    }
                }
            }
            Spacer(Modifier.height(contentPaddings.calculateBottomPadding()))
        }
    }
}

@Composable
private fun Collections(
    modifier: Modifier = Modifier,
    collections: List<UiGameCollectionWithUiGames>,
    onCollectionClicked: (UiGameCollection) -> Unit,
    onSeeAllClicked: () -> Unit,
) {
    if (collections.isEmpty()) return
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = ScreenHorizontalPadding),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                modifier = Modifier
                    .weight(1f)
                    .alignByBaseline(),
                text = stringResource(Res.string.collections),
                style = AppTheme.typography.headlineMedium,
            )
            GGButton(
                text = stringResource(Res.string.seeAll),
                onClick = onSeeAllClicked,
                buttonType = GGButtonType.OUTLINED,
            )
        }
        val listState = rememberLazyListState()
        LazyRow(
            modifier = Modifier.horizontalScrollHint(listState, nudge = true),
            state = listState,
            contentPadding = PaddingValues(horizontal = ScreenHorizontalPadding),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(
                count = collections.size,
                key = {
                    collections[it].collection.id
                },
                contentType = {
                    collections[it]::class
                }
            ) {
                val item = collections[it]
                CollectionCard(
                    modifier = Modifier.animateItem(),
                    // A status collection has no name of its own.
                    collection = item.status?.let { status ->
                        item.collection.copy(title = stringResource(status.title))
                    } ?: item.collection,
                    preview = item.games,
                    onClick = onCollectionClicked,
                )
            }
        }
    }
}

/** The backlog's title and its games, each an item of its own: they come and go animated. */
private fun LazyListScope.backlog(
    backlogGames: List<UiGame>,
    onSeeAllClicked: () -> Unit,
    onGameClicked: (UiGame) -> Unit,
    onAddGameClicked: (UiGame) -> Unit,
    onGameLongClicked: (UiGame) -> Unit,
) {
    if (backlogGames.isEmpty()) return
    animatedItem(key = "backlogTitle") {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = ScreenHorizontalPadding),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                modifier = Modifier
                    .weight(1f)
                    .alignByBaseline(),
                text = stringResource(Res.string.backlog),
                style = AppTheme.typography.headlineMedium,
            )
            GGButton(
                text = stringResource(Res.string.seeAll),
                onClick = onSeeAllClicked,
                buttonType = GGButtonType.OUTLINED,
            )
        }
    }
    items(items = backlogGames, key = { "backlog${it.id}" }) { game ->
        GameCard(
            modifier = Modifier
                .animateItem()
                .fillMaxWidth()
                .padding(horizontal = ScreenHorizontalPadding),
            game = game,
            type = GameCardType.Compact,
            onClick = onGameClicked,
            onAddClicked = onAddGameClicked,
            onLongClick = onGameLongClicked,
        )
    }
}

@Composable
private fun ForYou(
    modifier: Modifier = Modifier,
    game: UiGame?,
    onClicked: (UiGame) -> Unit,
    onAddClicked: (UiGame) -> Unit,
    onFavouriteClicked: (UiGame) -> Unit,
    onLongClicked: (UiGame) -> Unit,
    /** The game is known but its details are still coming from the network. */
    isLoading: Boolean = false,
    isFailed: Boolean = false,
    onRetryClicked: () -> Unit = {},
) {
    if (game == null && !isLoading && !isFailed) return
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(
            text = stringResource(Res.string.forYou),
            color = AppTheme.textColors.primary,
            style = AppTheme.typography.headlineMedium,
        )
        Text(
            text = stringResource(Res.string.forYouDesc),
            color = AppTheme.textColors.secondary,
            style = AppTheme.typography.bodyMedium,
        )
        AnimatedContent(
            targetState = game ?: if (isFailed) FOR_YOU_FAILED else FOR_YOU_LOADING,
            transitionSpec = { fadeIn() togetherWith fadeOut() using SizeTransform(clip = false) },
            contentKey = { (it as? UiGame)?.id ?: it },
            label = "ForYou",
        ) { target ->
            when (target) {
                FOR_YOU_FAILED -> LoadFailed(onRetryClicked = onRetryClicked)
                FOR_YOU_LOADING -> SkeletonGameCard(modifier = Modifier.fillMaxWidth(), type = GameCardType.Detailed)
                is UiGame -> GameCard(
                    game = target,
                    type = GameCardType.Detailed,
                    onAddClicked = onAddClicked,
                    onAddToFavouriteClicked = onFavouriteClicked,
                    onClick = onClicked,
                    onLongClick = onLongClicked,
                )
            }
        }
    }
}

@Composable
private fun LoadFailed(
    onRetryClicked: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.Start,
    ) {
        Text(
            text = stringResource(Res.string.loadFailed),
            style = AppTheme.typography.bodyMedium,
            color = AppTheme.textColors.secondary,
        )
        GGButton(
            text = stringResource(Res.string.retry),
            onClick = onRetryClicked,
            buttonType = GGButtonType.OUTLINED,
        )
    }
}

@Preview(
    heightDp = 2000,
    showBackground = true,
)
@Composable
private fun Preview() = BasePreview(
    false
) {
    EmptyMainScreen(
        recommendations = listOf(UiGame.testGame, UiGame.testGame),
        genres = listOf(UiGenre(1, "Action"), UiGenre(2, "RPG"), UiGenre(3, "Metroidvania")),
        selectedGenreId = 2,
        onGenreClicked = {},
        onGameClicked = {},
        onAddGameClicked = {},
        onGameLongClicked = {},
    )
    ForYou(
        game = UiGame.testGame,
        onClicked = {},
        onAddClicked = {},
        onFavouriteClicked = {},
        onLongClicked = {},
    )
    Collections(
        collections = listOf(
            UiGameCollectionWithUiGames(
                collection = UiGameCollection.testCollection(id = 1),
                games = listOf(UiGame.testGame, UiGame.testGame, UiGame.testGame),
            ),
            UiGameCollectionWithUiGames(
                collection = UiGameCollection.testCollection(id = 2),
                games = listOf(UiGame.testGame, UiGame.testGame, UiGame.testGame),
            ),
            UiGameCollectionWithUiGames(
                collection = UiGameCollection.testCollection(id = 3),
                games = listOf(UiGame.testGame, UiGame.testGame, UiGame.testGame),
            ),
            UiGameCollectionWithUiGames(
                collection = UiGameCollection.testCollection(id = 4),
                games = listOf(UiGame.testGame, UiGame.testGame, UiGame.testGame),
            )
        ),
        onCollectionClicked = {},
        onSeeAllClicked = {}
    )
}