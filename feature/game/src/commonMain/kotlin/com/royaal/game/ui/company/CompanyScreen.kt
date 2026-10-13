package com.royaal.game.ui.company

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
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
import com.royaal.designsystem.components.skeleton.SkeletonText
import com.royaal.designsystem.components.skeleton.rememberSkeletonShimmer
import com.royaal.designsystem.icons.AppIcons
import com.royaal.designsystem.theme.AppTheme
import com.royaal.designsystem.theme.textColors
import com.royaal.designsystem.uikit.GameCard
import com.royaal.designsystem.uikit.GameCardType
import com.royaal.designsystem.uikit.SkeletonGameCard
import com.royaal.designsystem.util.BasePreview
import com.royaal.designsystem.util.animatedItem
import com.royaal.entities.ui.UiGame
import com.royaal.entities.ui.testGame
import com.royaal.game.domain.CompanyDetails
import com.royaal.game.navigation.GameRoute
import com.royaal.game.ui.library.AddToLibrarySheet
import com.royaal.game.ui.library.rememberLibrarySavedSnackbar
import com.royaal.navigation.LocalNavigator
import ggdata.feature.game.generated.resources.Res
import ggdata.feature.game.generated.resources.companyLoadFailed
import ggdata.feature.game.generated.resources.developedGames
import ggdata.feature.game.generated.resources.founded
import ggdata.feature.game.generated.resources.noGames
import ggdata.feature.game.generated.resources.retry
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

private val ScreenHorizontalPadding = 16.dp
private const val SKELETON_GAMES_COUNT = 5

@Composable
internal fun CompanyScreen(companyId: Long) {
    val viewModel = koinViewModel<CompanyViewModel> { parametersOf(companyId) }
    val state by viewModel.state.collectAsStateWithLifecycle()
    val navigator = LocalNavigator.current
    var addingGameId by rememberSaveable { mutableStateOf<Long?>(null) }
    val onSaved = rememberLibrarySavedSnackbar()

    EffectHandler(viewModel) { effect ->
        when (effect) {
            is CompanySideEffect.OpenGame -> navigator.navigate(GameRoute(effect.gameId))
        }
    }

    CompanyContent(
        state = state,
        onBackClicked = navigator::back,
        onGameClicked = { viewModel.sendEvent(CompanyUserEvent.GameClicked(it.id)) },
        onAddGameClicked = { addingGameId = it.id },
        onRetryClicked = { viewModel.sendEvent(CompanyUserEvent.Retry) },
    )

    state.games.firstOrNull { it.id == addingGameId }?.let { game ->
        AddToLibrarySheet(
            game = game,
            onDismissRequest = { addingGameId = null },
            onSaved = onSaved,
        )
    }
}

@Composable
private fun CompanyContent(
    state: CompanyScreenState,
    onBackClicked: () -> Unit,
    onGameClicked: (UiGame) -> Unit,
    onAddGameClicked: (UiGame) -> Unit,
    onRetryClicked: () -> Unit,
) {
    val statusBarPadding = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val bottomBarPadding = LocalContentPadding.current.calculateBottomPadding()
    // Only while skeletons are shown: a remembered shimmer keeps animating, needed or not.
    val shimmer = if (state.isLoading) rememberSkeletonShimmer() else null

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = ScreenHorizontalPadding,
            end = ScreenHorizontalPadding,
            top = statusBarPadding + 8.dp,
            bottom = bottomBarPadding + 16.dp,
        ),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        animatedItem(key = "back") {
            GGIconButton(
                imageVector = AppIcons.arrowBack,
                type = GGIconButtonType.FLATTENED,
                onClick = onBackClicked,
            )
        }
        val company = state.company
        when {
            shimmer != null -> {
                animatedItem(key = "headerSkeleton") {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        SkeletonText(AppTheme.typography.headlineLarge, Modifier.fillMaxWidth(0.6f), shimmer = shimmer)
                        SkeletonText(AppTheme.typography.bodyMedium, Modifier.fillMaxWidth(), lines = 3, shimmer = shimmer)
                    }
                }
                items(count = SKELETON_GAMES_COUNT, key = { "skeleton$it" }) {
                    SkeletonGameCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .animateItem(),
                        type = GameCardType.Compact,
                        shimmer = shimmer,
                    )
                }
            }

            state.isLoadFailed || company == null -> animatedItem(key = "failed") {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = stringResource(Res.string.companyLoadFailed),
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

            else -> {
                animatedItem(key = "header") {
                    Header(company)
                }
                animatedItem(key = "gamesTitle") {
                    Text(
                        modifier = Modifier.padding(top = 16.dp),
                        text = stringResource(Res.string.developedGames),
                        style = AppTheme.typography.headlineMedium,
                    )
                }
                if (state.games.isEmpty()) {
                    animatedItem(key = "noGames") {
                        Text(
                            text = stringResource(Res.string.noGames),
                            style = AppTheme.typography.bodyMedium,
                            color = AppTheme.textColors.secondary,
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

@Composable
private fun Header(company: CompanyDetails) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        company.logoUrl?.let { logoUrl ->
            ProgressiveImage(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                url = logoUrl,
                previewUrl = null,
                contentDescription = null,
                contentScale = ContentScale.Fit,
                alignment = Alignment.Center,
            )
        }
        Text(
            text = company.name,
            style = AppTheme.typography.headlineLarge,
        )
        company.foundedYear?.let { year ->
            Text(
                text = stringResource(Res.string.founded, year),
                style = AppTheme.typography.bodyMedium,
                color = AppTheme.textColors.secondary,
            )
        }
        company.description?.let { description ->
            Text(
                text = description,
                style = AppTheme.typography.bodyLarge,
            )
        }
    }
}

@Preview(showBackground = true, heightDp = 900)
@Composable
private fun Preview() = BasePreview {
    CompanyContent(
        state = CompanyScreenState(
            companyId = 1,
            company = CompanyDetails(
                id = 1,
                name = "Mojang Studios",
                description = "A Swedish video game developer based in Stockholm.",
                logoUrl = null,
                foundedYear = 2009,
            ),
            games = listOf(UiGame.testGame.copy(id = 1), UiGame.testGame.copy(id = 2)),
            isLoading = false,
        ),
        onBackClicked = {},
        onGameClicked = {},
        onAddGameClicked = {},
        onRetryClicked = {},
    )
}
