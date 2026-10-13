package com.royaal.collections.ui.details

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.royaal.arch.mvi.EffectHandler
import com.royaal.collections.domain.CollectionKey
import com.royaal.collections.ui.common.title
import com.royaal.designsystem.components.button.GGButton
import com.royaal.designsystem.components.button.GGButtonType
import com.royaal.designsystem.components.button.GGIconButton
import com.royaal.designsystem.components.button.GGIconButtonType
import com.royaal.designsystem.components.dialog.GGDialog
import com.royaal.designsystem.components.screen.LocalContentPadding
import com.royaal.designsystem.components.skeleton.rememberSkeletonShimmer
import com.royaal.designsystem.components.snackbar.LocalSnackbarHostState
import com.royaal.designsystem.components.snackbar.showGGSnackbar
import com.royaal.designsystem.icons.AppIcons
import com.royaal.designsystem.theme.AppTheme
import com.royaal.designsystem.theme.textColors
import com.royaal.designsystem.uikit.GameCard
import com.royaal.designsystem.uikit.GameCardType
import com.royaal.designsystem.uikit.SkeletonGameCard
import com.royaal.designsystem.uikit.Title
import com.royaal.designsystem.util.BasePreview
import com.royaal.entities.data.GamePlayStatus
import com.royaal.entities.ui.UiGame
import com.royaal.entities.ui.testGame
import com.royaal.game.navigation.GameRoute
import com.royaal.navigation.LocalNavigator
import ggdata.feature.collections.generated.resources.Res
import ggdata.feature.collections.generated.resources.cancel
import ggdata.feature.collections.generated.resources.delete
import ggdata.feature.collections.generated.resources.deleteCustomText
import ggdata.feature.collections.generated.resources.deleteStatusText
import ggdata.feature.collections.generated.resources.deleteTitle
import ggdata.feature.collections.generated.resources.emptyCustomCollection
import ggdata.feature.collections.generated.resources.emptyMarkedCollection
import ggdata.feature.collections.generated.resources.favouritesDescription
import ggdata.feature.collections.generated.resources.gamesCount
import ggdata.feature.collections.generated.resources.removed
import ggdata.feature.collections.generated.resources.statusDescription
import ggdata.feature.collections.generated.resources.undo
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

private const val SKELETON_GAMES_COUNT = 4

/** Games with [playStatus], whether or not the user has added this status collection. */
@Composable
fun CollectionDetailsScreen(
    playStatus: GamePlayStatus,
) = CollectionDetailsScreen(CollectionKey.Status(playStatus))

/** A collection by id: user-created or a status one. */
@Composable
fun CollectionDetailsScreen(
    collectionId: Long,
) = CollectionDetailsScreen(CollectionKey.Custom(collectionId))

/** Games marked as favourite. */
@Composable
fun FavouritesScreen() = CollectionDetailsScreen(CollectionKey.Favourites)

@Composable
private fun CollectionDetailsScreen(key: CollectionKey) {
    val viewModel = koinViewModel<CollectionViewModel> { parametersOf(key) }
    val state by viewModel.state.collectAsStateWithLifecycle()
    val navigator = LocalNavigator.current
    val snackbarHostState = LocalSnackbarHostState.current
    val scope = rememberCoroutineScope()
    var showDeleteDialog by rememberSaveable { mutableStateOf(false) }

    val title = state.key.title(state.title)
    val removedMessage = stringResource(Res.string.removed, title)
    val undoLabel = stringResource(Res.string.undo)

    EffectHandler(viewModel) { effect ->
        when (effect) {
            CollectionSideEffect.Close -> navigator.back()
            // Launched: the snackbar waits for its timeout, and effects must not be held up meanwhile.
            CollectionSideEffect.ShowRemoved -> scope.launch {
                val result = snackbarHostState.showGGSnackbar(removedMessage, actionLabel = undoLabel)
                if (result == SnackbarResult.ActionPerformed) {
                    viewModel.sendEvent(CollectionUserEvent.UndoRemove)
                }
            }
        }
    }

    CollectionContent(
        state = state,
        title = title,
        onBackClicked = navigator::back,
        onPinClicked = { viewModel.sendEvent(CollectionUserEvent.TogglePin) },
        onDeleteClicked = { showDeleteDialog = true },
        onRemoveGameClicked = { viewModel.sendEvent(CollectionUserEvent.RemoveGame(it.id)) },
        onGameClicked = { navigator.navigate(GameRoute(it.id)) },
    )

    if (showDeleteDialog) {
        GGDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = stringResource(Res.string.deleteTitle, title),
            text = stringResource(
                if (state.key is CollectionKey.Status) Res.string.deleteStatusText else Res.string.deleteCustomText
            ),
        ) {
            GGButton(
                text = stringResource(Res.string.cancel),
                onClick = { showDeleteDialog = false },
                buttonType = GGButtonType.OUTLINED,
            )
            GGButton(
                text = stringResource(Res.string.delete),
                onClick = {
                    showDeleteDialog = false
                    viewModel.sendEvent(CollectionUserEvent.Delete)
                },
            )
        }
    }
}

@Composable
private fun CollectionContent(
    state: CollectionScreenState,
    title: String,
    onBackClicked: () -> Unit,
    onPinClicked: () -> Unit,
    onDeleteClicked: () -> Unit,
    onRemoveGameClicked: (UiGame) -> Unit,
    onGameClicked: (UiGame) -> Unit,
) {
    val statusBarPadding = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val bottomBarPadding = LocalContentPadding.current.calculateBottomPadding()
    // Only while skeletons are shown: a remembered shimmer keeps animating, needed or not.
    val shimmer = if (state.isLoading) rememberSkeletonShimmer() else null

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = 16.dp,
            end = 16.dp,
            top = statusBarPadding + 8.dp,
            bottom = bottomBarPadding + 16.dp,
        ),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        item(key = "topBar") {
            TopBar(
                canEdit = state.canEdit,
                pinned = state.pinned,
                onBackClicked = onBackClicked,
                onPinClicked = onPinClicked,
                onDeleteClicked = onDeleteClicked,
            )
        }
        item(key = "header") {
            Header(
                key = state.key,
                title = title,
                gamesCount = state.games.size,
                isLoading = state.isLoading,
            )
        }
        when {
            shimmer != null -> items(count = SKELETON_GAMES_COUNT, key = { "skeleton$it" }) {
                SkeletonGameCard(
                    modifier = Modifier.fillMaxWidth(),
                    type = GameCardType.Compact,
                    shimmer = shimmer,
                )
            }

            state.games.isEmpty() -> item(key = "empty") {
                Text(
                    modifier = Modifier.padding(top = 24.dp),
                    text = stringResource(
                        if (state.key is CollectionKey.Custom) {
                            Res.string.emptyCustomCollection
                        } else {
                            Res.string.emptyMarkedCollection
                        }
                    ),
                    style = AppTheme.typography.bodyMedium,
                    color = AppTheme.textColors.secondary,
                )
            }

            else -> items(items = state.games, key = { it.id }) { game ->
                GameCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .animateItem(),
                    game = game,
                    type = GameCardType.Compact,
                    onRemoveClicked = onRemoveGameClicked,
                    onClick = onGameClicked,
                )
            }
        }
    }
}

@Composable
private fun TopBar(
    canEdit: Boolean,
    pinned: Boolean,
    onBackClicked: () -> Unit,
    onPinClicked: () -> Unit,
    onDeleteClicked: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        GGIconButton(
            imageVector = AppIcons.arrowBack,
            type = GGIconButtonType.FLATTENED,
            onClick = onBackClicked,
        )
        Spacer(Modifier.weight(1f))
        if (canEdit) {
            GGIconButton(
                imageVector = AppIcons.pin,
                type = GGIconButtonType.OUTLINED,
                selected = pinned,
                onClick = onPinClicked,
            )
            GGIconButton(
                imageVector = AppIcons.delete,
                type = GGIconButtonType.OUTLINED,
                onClick = onDeleteClicked,
            )
        }
    }
}

@Composable
private fun Header(
    key: CollectionKey,
    title: String,
    gamesCount: Int,
    isLoading: Boolean,
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Title(text = title)
        val description = when (key) {
            CollectionKey.Favourites -> stringResource(Res.string.favouritesDescription)
            is CollectionKey.Status -> stringResource(Res.string.statusDescription, key.status.title())
            is CollectionKey.Custom -> null
        }
        description?.let {
            Text(
                text = it,
                style = AppTheme.typography.bodyMedium,
                color = AppTheme.textColors.secondary,
            )
        }
        if (!isLoading) {
            Text(
                text = pluralStringResource(Res.plurals.gamesCount, gamesCount, gamesCount),
                style = AppTheme.typography.labelLarge,
                color = AppTheme.textColors.secondary,
            )
        }
    }
}

@Composable
private fun GamePlayStatus.title(): String = CollectionKey.Status(this).title(name = "")

@Preview(showBackground = true, heightDp = 800)
@Composable
private fun Preview() = BasePreview {
    CollectionContent(
        state = CollectionScreenState(
            key = CollectionKey.Custom(1),
            title = "Cozy evenings",
            collectionId = 1,
            pinned = true,
            games = listOf(UiGame.testGame.copy(id = 1), UiGame.testGame.copy(id = 2)),
            isLoading = false,
        ),
        title = "Cozy evenings",
        onBackClicked = {},
        onPinClicked = {},
        onDeleteClicked = {},
        onRemoveGameClicked = {},
        onGameClicked = {},
    )
}
