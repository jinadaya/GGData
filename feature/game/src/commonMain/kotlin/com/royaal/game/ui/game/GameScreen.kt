package com.royaal.game.ui.game

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsTopHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.royaal.arch.mvi.EffectHandler
import com.royaal.designsystem.components.button.GGButton
import com.royaal.designsystem.components.button.GGButtonType
import com.royaal.designsystem.components.button.GGIconButton
import com.royaal.designsystem.components.button.GGIconButtonType
import com.royaal.designsystem.components.image.ProgressiveImage
import com.royaal.designsystem.components.screen.LocalContentPadding
import com.royaal.designsystem.components.skeleton.SkeletonBox
import com.royaal.designsystem.components.skeleton.SkeletonText
import com.royaal.designsystem.components.skeleton.rememberSkeletonShimmer
import com.royaal.designsystem.components.tag.GGTag
import com.royaal.designsystem.components.tag.GGTagFlowRow
import com.royaal.designsystem.components.tag.GGTagType
import com.royaal.designsystem.icons.AppIcons
import com.royaal.designsystem.theme.AppTheme
import com.royaal.designsystem.theme.backgroundColors
import com.royaal.designsystem.theme.textColors
import com.royaal.designsystem.uikit.GameCoverCard
import com.royaal.designsystem.uikit.GameImage
import com.royaal.designsystem.uikit.SkeletonGameCoverCard
import com.royaal.designsystem.uikit.Title
import com.royaal.designsystem.util.BasePreview
import com.royaal.designsystem.util.TextUtils
import com.royaal.designsystem.util.click
import com.royaal.entities.data.CompanyData
import com.royaal.entities.data.GamePlayStatus
import com.royaal.entities.ui.UiGame
import com.royaal.entities.ui.testGame
import com.royaal.game.navigation.CompanyRoute
import com.royaal.game.navigation.GameRoute
import com.royaal.game.ui.common.title
import com.royaal.game.ui.library.AddToLibrarySheet
import com.royaal.game.ui.library.rememberLibrarySavedSnackbar
import com.royaal.navigation.LocalNavigator
import ggdata.feature.game.generated.resources.Res
import ggdata.feature.game.generated.resources.about
import ggdata.feature.game.generated.resources.addToLibrary
import ggdata.feature.game.generated.resources.by
import ggdata.feature.game.generated.resources.editInLibrary
import ggdata.feature.game.generated.resources.editInLibraryNoStatus
import ggdata.feature.game.generated.resources.factPlayers
import ggdata.feature.game.generated.resources.factReleased
import ggdata.feature.game.generated.resources.factTimeToBeat
import ggdata.feature.game.generated.resources.loadFailed
import ggdata.feature.game.generated.resources.moreFrom
import ggdata.feature.game.generated.resources.publishedBy
import ggdata.feature.game.generated.resources.retry
import ggdata.feature.game.generated.resources.seeAll
import ggdata.feature.game.generated.resources.series
import ggdata.feature.game.generated.resources.seriesNoName
import ggdata.feature.game.generated.resources.similar
import ggdata.feature.game.generated.resources.statCollections
import ggdata.feature.game.generated.resources.statFavourite
import ggdata.feature.game.generated.resources.statFavouriteYes
import ggdata.feature.game.generated.resources.statSince
import ggdata.feature.game.generated.resources.statStatus
import ggdata.feature.game.generated.resources.yourStats
import kotlin.math.roundToInt
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

private val ScreenHorizontalPadding = 16.dp
private const val SKELETON_ROW_CARDS = 4

@Composable
internal fun GameScreen(gameId: Long) {
    val viewModel = koinViewModel<GameViewModel> { parametersOf(gameId) }
    val state by viewModel.state.collectAsStateWithLifecycle()
    val navigator = LocalNavigator.current
    var showAddSheet by rememberSaveable { mutableStateOf(false) }
    val onSaved = rememberLibrarySavedSnackbar()

    EffectHandler(viewModel) { effect ->
        when (effect) {
            is GameSideEffect.OpenGame -> navigator.navigate(GameRoute(effect.gameId))
            is GameSideEffect.OpenCompany -> navigator.navigate(CompanyRoute(effect.companyId))
        }
    }

    GameContent(
        state = state,
        onBackClicked = navigator::back,
        onAddClicked = { showAddSheet = true },
        onFavouriteClicked = { viewModel.sendEvent(GameUserEvent.ToggleFavourite) },
        onDeveloperClicked = { viewModel.sendEvent(GameUserEvent.DeveloperClicked) },
        onGameClicked = { viewModel.sendEvent(GameUserEvent.GameClicked(it.id)) },
        onRetryClicked = { viewModel.sendEvent(GameUserEvent.Retry) },
    )

    val game = state.game
    if (showAddSheet && game != null) {
        AddToLibrarySheet(
            game = game,
            onDismissRequest = { showAddSheet = false },
            onSaved = onSaved,
        )
    }
}

@Composable
private fun GameContent(
    state: GameScreenState,
    onBackClicked: () -> Unit,
    onAddClicked: () -> Unit,
    onFavouriteClicked: () -> Unit,
    onDeveloperClicked: () -> Unit,
    onGameClicked: (UiGame) -> Unit,
    onRetryClicked: () -> Unit,
) {
    val game = state.game
    val anyRowLoading = state.series.isLoading || state.similar.isLoading || state.developerGames.isLoading
    // Only while skeletons are shown: a remembered shimmer keeps animating, needed or not.
    val shimmer = if (game == null || (anyRowLoading && !state.isLoadFailed)) rememberSkeletonShimmer() else null
    val bottomPadding = LocalContentPadding.current.calculateBottomPadding()

    val listState = rememberLazyListState()
    // Once the hero has scrolled away, the text would run under the status bar's icons.
    val isHeroGone by remember { derivedStateOf { listState.firstVisibleItemIndex > 0 } }
    val statusBarScrimAlpha by animateFloatAsState(if (isHeroGone) 1f else 0f, label = "StatusBarScrim")

    Box(Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            state = listState,
            contentPadding = PaddingValues(bottom = bottomPadding + 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item(key = "hero") {
                Hero(game = game, heroImageUrl = state.heroImageUrl, shimmer = shimmer)
            }
            when {
                game != null -> gameDetails(
                    state = state,
                    game = game,
                    shimmer = shimmer,
                    onAddClicked = onAddClicked,
                    onFavouriteClicked = onFavouriteClicked,
                    onDeveloperClicked = onDeveloperClicked,
                    onGameClicked = onGameClicked,
                    onRetryClicked = onRetryClicked,
                )

                state.isLoadFailed -> item(key = "failed") {
                    LoadFailed(onRetryClicked = onRetryClicked)
                }

                shimmer != null -> item(key = "skeleton") {
                    DetailsSkeleton(shimmer)
                }
            }
        }
        Box(
            Modifier
                .fillMaxWidth()
                .windowInsetsTopHeight(WindowInsets.statusBars)
                .graphicsLayer { alpha = statusBarScrimAlpha }
                .background(AppTheme.backgroundColors.background),
        )
        // Over the hero, so it stays reachable while the page scrolls.
        Box(
            Modifier
                .statusBarsPadding()
                .padding(start = ScreenHorizontalPadding, top = 8.dp),
        ) {
            GGIconButton(
                imageVector = AppIcons.arrowBack,
                type = GGIconButtonType.OUTLINED,
                onClick = onBackClicked,
            )
        }
    }
}

private fun LazyListScope.gameDetails(
    state: GameScreenState,
    game: UiGame,
    shimmer: State<Float>?,
    onAddClicked: () -> Unit,
    onFavouriteClicked: () -> Unit,
    onDeveloperClicked: () -> Unit,
    onGameClicked: (UiGame) -> Unit,
    onRetryClicked: () -> Unit,
) {
    item(key = "header") {
        Header(
            game = game,
            developer = state.developer,
            publisher = state.publisher,
            onDeveloperClicked = onDeveloperClicked,
        )
    }
    item(key = "tags") {
        Tags(game)
    }
    item(key = "actions") {
        Actions(
            stats = state.stats,
            onAddClicked = onAddClicked,
            onFavouriteClicked = onFavouriteClicked,
        )
    }
    if (!state.stats.isEmpty) {
        item(key = "stats") {
            PlayerStats(state.stats)
        }
    }
    item(key = "facts") {
        Facts(game)
    }
    if (game.description.isNotBlank()) {
        item(key = "about") {
            About(game.description)
        }
    }
    if (state.isLoadFailed) {
        item(key = "failed") {
            LoadFailed(onRetryClicked = onRetryClicked)
        }
    }
    gameRow(
        key = "series",
        title = {
            state.seriesName?.let { stringResource(Res.string.series, it) }
                ?: stringResource(Res.string.seriesNoName)
        },
        row = state.series,
        shimmer = shimmer,
        onGameClicked = onGameClicked,
    )
    state.developer?.let { developer ->
        gameRow(
            key = "developer",
            title = { stringResource(Res.string.moreFrom, developer.name) },
            row = state.developerGames,
            shimmer = shimmer,
            onGameClicked = onGameClicked,
            onSeeAllClicked = onDeveloperClicked,
        )
    }
    gameRow(
        key = "similar",
        title = { stringResource(Res.string.similar) },
        row = state.similar,
        shimmer = shimmer,
        onGameClicked = onGameClicked,
    )
}

/** A wide artwork edge to edge; the cover, cropped, while IGDB hasn't sent one. */
@Composable
private fun Hero(
    game: UiGame?,
    heroImageUrl: String?,
    shimmer: State<Float>?,
) {
    val modifier = Modifier
        .fillMaxWidth()
        .aspectRatio(16 / 9f)
    when {
        heroImageUrl != null -> ProgressiveImage(
            modifier = modifier.background(AppTheme.backgroundColors.background),
            url = heroImageUrl,
            previewUrl = game?.coverPreviewUrl,
            contentDescription = null,
            contentScale = ContentScale.Crop,
        )

        game != null -> GameImage(modifier = modifier, game = game)
        shimmer != null -> SkeletonBox(modifier = modifier, shimmer = shimmer)
        // Failed with nothing cached: the space is kept for the back button.
        else -> Box(modifier.padding(WindowInsets.statusBars.asPaddingValues()))
    }
}

@Composable
private fun Header(
    game: UiGame,
    developer: CompanyData?,
    publisher: CompanyData?,
    onDeveloperClicked: () -> Unit,
) {
    Column(
        modifier = Modifier.padding(horizontal = ScreenHorizontalPadding),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Row(
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Title(
                modifier = Modifier
                    .weight(1f)
                    .alignByBaseline(),
                text = game.title,
            )
            if (game.rating >= 0f) {
                Row(modifier = Modifier.alignByBaseline(), verticalAlignment = Alignment.Bottom) {
                    Text(
                        modifier = Modifier.alignByBaseline(),
                        text = game.rating.roundToInt().toString(),
                        style = AppTheme.typography.headlineLarge,
                    )
                    Text(
                        modifier = Modifier.alignByBaseline(),
                        text = "/100",
                        color = AppTheme.textColors.secondary,
                    )
                }
            }
        }
        developer?.let {
            Text(
                modifier = Modifier.click(onClick = onDeveloperClicked),
                text = stringResource(Res.string.by, it.name),
                style = AppTheme.typography.titleMedium,
                textDecoration = TextDecoration.Underline,
            )
        }
        val details = listOfNotNull(
            game.releaseYear?.toString(),
            publisher?.let { stringResource(Res.string.publishedBy, it.name) },
        )
        if (details.isNotEmpty()) {
            Text(
                text = details.joinToString(" ${TextUtils.DOT_SEPARATOR} "),
                style = AppTheme.typography.bodyMedium,
                color = AppTheme.textColors.secondary,
            )
        }
    }
}

@Composable
private fun Tags(game: UiGame) {
    Column(
        modifier = Modifier.padding(horizontal = ScreenHorizontalPadding),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        game.platforms?.let { platforms ->
            GGTagFlowRow {
                platforms.forEach { GGTag(text = it, type = GGTagType.OUTLINE) }
            }
        }
        game.genres?.let { genres ->
            GGTagFlowRow {
                genres.forEach { GGTag(text = it, type = GGTagType.NEUTRAL) }
            }
        }
    }
}

@Composable
private fun Actions(
    stats: UiPlayerStats,
    onAddClicked: () -> Unit,
    onFavouriteClicked: () -> Unit,
) {
    Row(
        modifier = Modifier.padding(horizontal = ScreenHorizontalPadding),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        val status = stats.status
        GGButton(
            modifier = Modifier.weight(1f),
            text = when {
                status != null -> stringResource(Res.string.editInLibrary, stringResource(status.title))
                !stats.isEmpty -> stringResource(Res.string.editInLibraryNoStatus)
                else -> stringResource(Res.string.addToLibrary)
            },
            onClick = onAddClicked,
            // Once it's in the library, the button edits rather than adds: a quieter one.
            buttonType = if (stats.isEmpty) GGButtonType.NORMAL else GGButtonType.OUTLINED,
            iconStart = if (stats.isEmpty) AppIcons.add else null,
        )
        GGIconButton(
            imageVector = AppIcons.favorite,
            type = GGIconButtonType.OUTLINED,
            selected = stats.favourite,
            onClick = onFavouriteClicked,
        )
    }
}

@Composable
private fun PlayerStats(stats: UiPlayerStats) {
    Section(title = stringResource(Res.string.yourStats)) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            stats.status?.let { StatRow(stringResource(Res.string.statStatus), stringResource(it.title)) }
            stats.statusSince?.let { StatRow(stringResource(Res.string.statSince), it) }
            if (stats.favourite) {
                StatRow(stringResource(Res.string.statFavourite), stringResource(Res.string.statFavouriteYes))
            }
            if (stats.collections.isNotEmpty()) {
                StatRow(stringResource(Res.string.statCollections), stats.collections.joinToString(", "))
            }
        }
    }
}

@Composable
private fun StatRow(label: String, value: String) {
    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(
            modifier = Modifier.width(120.dp),
            text = label,
            style = AppTheme.typography.labelLarge,
            color = AppTheme.textColors.secondary,
        )
        Text(
            modifier = Modifier.weight(1f),
            text = value,
            style = AppTheme.typography.labelLarge,
        )
    }
}

/** Time to beat, players, release date: whichever are known. */
@Composable
private fun Facts(game: UiGame) {
    val facts = listOfNotNull(
        game.timeToBeat?.let { stringResource(Res.string.factTimeToBeat) to TextUtils.formatHours(it) },
        game.maxPlayers?.let { stringResource(Res.string.factPlayers) to it.toString() },
        game.releaseDate?.let { stringResource(Res.string.factReleased) to it },
    )
    if (facts.isEmpty()) return
    Row(modifier = Modifier.padding(horizontal = ScreenHorizontalPadding)) {
        facts.forEach { (label, value) ->
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(text = label, style = AppTheme.typography.labelLarge, color = AppTheme.textColors.secondary)
                Text(text = value, style = AppTheme.typography.labelLarge)
            }
        }
    }
}

@Composable
private fun About(description: String) {
    Section(title = stringResource(Res.string.about)) {
        Text(text = description, style = AppTheme.typography.bodyLarge)
    }
}

@Composable
private fun Section(
    title: String,
    content: @Composable () -> Unit,
) {
    Column(
        modifier = Modifier.padding(horizontal = ScreenHorizontalPadding),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(text = title, style = AppTheme.typography.headlineMedium)
        content()
    }
}

/** A titled horizontal row of covers, edge to edge. Skeletons while loading, nothing once empty. */
private fun LazyListScope.gameRow(
    key: String,
    title: @Composable () -> String,
    row: GameRow,
    shimmer: State<Float>?,
    onGameClicked: (UiGame) -> Unit,
    onSeeAllClicked: (() -> Unit)? = null,
) {
    if (!row.isVisible) return
    item(key = key) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = ScreenHorizontalPadding),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    modifier = Modifier.weight(1f),
                    text = title(),
                    style = AppTheme.typography.headlineMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                if (onSeeAllClicked != null && !row.isLoading) {
                    GGButton(
                        text = stringResource(Res.string.seeAll),
                        onClick = onSeeAllClicked,
                        buttonType = GGButtonType.OUTLINED,
                    )
                }
            }
            LazyRow(
                contentPadding = PaddingValues(horizontal = ScreenHorizontalPadding),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                if (row.isLoading && shimmer != null) {
                    items(count = SKELETON_ROW_CARDS, key = { "skeleton$it" }) {
                        SkeletonGameCoverCard(shimmer = shimmer)
                    }
                } else {
                    items(items = row.games, key = { it.id }) { game ->
                        GameCoverCard(game = game, onClick = onGameClicked)
                    }
                }
            }
        }
    }
}

@Composable
private fun LoadFailed(onRetryClicked: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = ScreenHorizontalPadding),
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

@Composable
private fun DetailsSkeleton(shimmer: State<Float>) {
    Column(
        modifier = Modifier.padding(horizontal = ScreenHorizontalPadding),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        SkeletonText(AppTheme.typography.headlineLarge, Modifier.fillMaxWidth(0.7f), shimmer = shimmer)
        SkeletonText(AppTheme.typography.titleMedium, Modifier.fillMaxWidth(0.4f), shimmer = shimmer)
        SkeletonText(AppTheme.typography.bodyLarge, Modifier.fillMaxWidth(), lines = 4, shimmer = shimmer)
    }
}

@Preview(showBackground = true, heightDp = 1400)
@Composable
private fun Preview() = BasePreview {
    GameContent(
        state = GameScreenState(
            gameId = 1,
            game = UiGame.testGame.copy(releaseDate = "18 Nov 2011", timeToBeat = 100),
            developer = CompanyData(1, "Mojang"),
            publisher = CompanyData(2, "Microsoft"),
            isComplete = true,
            stats = UiPlayerStats(
                status = GamePlayStatus.PLAYING,
                statusSince = "12 Nov 2025",
                favourite = true,
                collections = listOf("Cozy"),
            ),
            series = GameRow(listOf(UiGame.testGame.copy(id = 2)), isLoading = false),
            similar = GameRow(isLoading = false),
            developerGames = GameRow(listOf(UiGame.testGame.copy(id = 3)), isLoading = false),
        ),
        onBackClicked = {},
        onAddClicked = {},
        onFavouriteClicked = {},
        onDeveloperClicked = {},
        onGameClicked = {},
        onRetryClicked = {},
    )
}
