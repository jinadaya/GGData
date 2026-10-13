package com.royaal.main.ui

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import com.royaal.designsystem.util.click
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
import ggdata.feature.main.generated.resources.Res
import ggdata.feature.main.generated.resources.backlog
import ggdata.feature.main.generated.resources.collections
import ggdata.feature.main.generated.resources.forYou
import ggdata.feature.main.generated.resources.forYouDesc
import ggdata.feature.main.generated.resources.maybePlayed
import ggdata.feature.main.generated.resources.maybePlayedDesc
import ggdata.feature.main.generated.resources.seeAll
import ggdata.feature.main.generated.resources.start
import ggdata.feature.main.generated.resources.startDesc
import ggdata.feature.main.generated.resources.whatYouLike
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

private const val SKELETON_RECOMMENDATIONS_COUNT = 4
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

    if (state.value.isEmptyState) {
        EmptyMainScreen(
            recommendations = state.value.recommendations,
            genres = state.value.genres,
            selectedGenreId = state.value.selectedGenreId,
            isGenresLoading = state.value.isGenresLoading,
            isRecommendationsLoading = state.value.isRecommendationsLoading,
            onGenreClicked = { viewModel.sendEvent(MainUserEvent.GenreClicked(it.id)) },
            onGameClicked = onGameClicked,
            onAddGameClicked = onAddGameClicked,
            onGameLongClicked = onGameLongClicked,
        )
    } else {
        LazyColumn(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
            // Unlike the bottom bar, the status bar isn't in the content padding.
            contentPadding = uiPaddings + PaddingValues(
                top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding() + 8.dp,
                bottom = 8.dp,
            ),
        ) {
            item {
                ForYou(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = ScreenHorizontalPadding),
                    game = state.value.latestGame,
                    onClicked = onGameClicked,
                    onAddClicked = onAddGameClicked,
                    onFavouriteClicked = { viewModel.sendEvent(MainUserEvent.AddGameToFavClicked(it)) },
                    onLongClicked = onGameLongClicked,
                )
            }
            item {
                Backlog(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = ScreenHorizontalPadding),
                    backlogGames = state.value.backlogGames,
                    onSeeAllClicked = { viewModel.sendEvent(MainUserEvent.SeeBacklog) },
                    onGameClicked = onGameClicked,
                    onAddGameClicked = onAddGameClicked,
                    onGameLongClicked = onGameLongClicked,
                )
            }
            item {
                Collections(
                    modifier = Modifier.fillMaxWidth(),
                    onSeeAllClicked = { viewModel.sendEvent(MainUserEvent.SeeCollections) },
                    collections = state.value.collections,
                    onCollectionClicked = { viewModel.sendEvent(MainUserEvent.CollectionClicked(it)) }
                )
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
            Spacer(Modifier.height(statusBarPaddings.calculateTopPadding()))
            Text(
                text = stringResource(Res.string.start),
                style = AppTheme.typography.headlineLarge,
            )
            Text(
                text = stringResource(Res.string.startDesc),
                style = AppTheme.typography.bodyMedium,
                color = AppTheme.textColors.secondary,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = stringResource(Res.string.whatYouLike),
                style = AppTheme.typography.headlineMedium,
            )
        }
        GGTagRow(contentPadding = PaddingValues(horizontal = ScreenHorizontalPadding)) {
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
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                if (isRecommendationsLoading && shimmer != null) {
                    repeat(SKELETON_RECOMMENDATIONS_COUNT) {
                        SkeletonGameCard(
                            modifier = Modifier.fillMaxWidth(),
                            type = GameCardType.Compact,
                            shimmer = shimmer,
                        )
                    }
                } else {
                    recommendations.forEach { game ->
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
        LazyRow(
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

@Composable
private fun Backlog(
    modifier: Modifier = Modifier,
    backlogGames: List<UiGame>,
    onSeeAllClicked: () -> Unit,
    onGameClicked: (UiGame) -> Unit,
    onAddGameClicked: (UiGame) -> Unit,
    onGameLongClicked: (UiGame) -> Unit,
) {
    if (backlogGames.isEmpty()) return
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
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
        Column(
            verticalArrangement = Arrangement.spacedBy(4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            backlogGames.forEach { game ->
                GameCard(
                    modifier = Modifier.fillMaxWidth(),
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

@Composable
private fun ForYou(
    modifier: Modifier = Modifier,
    game: UiGame?,
    onClicked: (UiGame) -> Unit,
    onAddClicked: (UiGame) -> Unit,
    onFavouriteClicked: (UiGame) -> Unit,
    onLongClicked: (UiGame) -> Unit,
) {
    if (game == null) return
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
        GameCard(
            game = game,
            type = GameCardType.Detailed,
            onAddClicked = onAddClicked,
            onAddToFavouriteClicked = onFavouriteClicked,
            onClick = onClicked,
            onLongClick = onLongClicked,
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
    Backlog(
        backlogGames = listOf(UiGame.testGame, UiGame.testGame, UiGame.testGame),
        onSeeAllClicked = {},
        onGameClicked = {},
        onAddGameClicked = {},
        onGameLongClicked = {},
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