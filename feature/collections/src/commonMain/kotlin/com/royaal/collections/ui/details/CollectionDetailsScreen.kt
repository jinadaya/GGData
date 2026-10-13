package com.royaal.collections.ui.details

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Icon
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
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
import com.royaal.designsystem.util.animatedItem
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
import ggdata.feature.collections.generated.resources.done
import ggdata.feature.collections.generated.resources.emptyCustomCollection
import ggdata.feature.collections.generated.resources.emptyMarkedCollection
import ggdata.feature.collections.generated.resources.favouritesDescription
import ggdata.feature.collections.generated.resources.gamesCount
import ggdata.feature.collections.generated.resources.removed
import ggdata.feature.collections.generated.resources.rename
import ggdata.feature.collections.generated.resources.reorderHint
import ggdata.feature.collections.generated.resources.statusDescription
import ggdata.feature.collections.generated.resources.undo
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

private const val SKELETON_GAMES_COUNT = 4
private const val GAME_CONTENT_TYPE = "game"

/** A dragged game is slightly bigger, as if lifted. */
private const val DRAGGED_SCALE = 1.03f

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
    var showRenameSheet by rememberSaveable { mutableStateOf(false) }
    var isEditing by rememberSaveable { mutableStateOf(false) }

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
        isEditing = isEditing && state.canRenameAndReorder,
        onEditClicked = { isEditing = !isEditing },
        onRenameClicked = { showRenameSheet = true },
        onReorder = { viewModel.sendEvent(CollectionUserEvent.Reorder(it)) },
    )

    if (showRenameSheet) {
        RenameCollectionSheet(
            currentName = state.title,
            takenNames = state.otherNames,
            onRename = { viewModel.sendEvent(CollectionUserEvent.Rename(it)) },
            onDismissRequest = { showRenameSheet = false },
        )
    }

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
    isEditing: Boolean,
    onEditClicked: () -> Unit,
    onRenameClicked: () -> Unit,
    onReorder: (gameIds: List<Long>) -> Unit,
) {
    val statusBarPadding = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val bottomBarPadding = LocalContentPadding.current.calculateBottomPadding()
    // Only while skeletons are shown: a remembered shimmer keeps animating, needed or not.
    val shimmer = if (state.isLoading) rememberSkeletonShimmer() else null

    val listState = rememberLazyListState()
    // The order as dragged, until the saved one comes back from the database.
    var movedOrder by remember { mutableStateOf<List<Long>?>(null) }
    val savedOrder = state.games.map { it.id }
    LaunchedEffect(savedOrder) {
        val order = movedOrder ?: return@LaunchedEffect
        // Saved, or the games changed meanwhile: the database's order is the one to show.
        if (order == savedOrder || order.toSet() != savedOrder.toSet()) movedOrder = null
    }
    val games = movedOrder?.let { order ->
        val gamesById = state.games.associateBy { it.id }
        order.mapNotNull(gamesById::get)
    } ?: state.games
    val reorderState = rememberReorderState(
        listState = listState,
        onMove = { fromKey, toKey ->
            val order = (movedOrder ?: savedOrder).toMutableList()
            val from = order.indexOf(fromKey)
            val to = order.indexOf(toKey)
            if (from >= 0 && to >= 0) {
                order.add(to, order.removeAt(from))
                movedOrder = order
            }
        },
        onDrop = { movedOrder?.let(onReorder) },
    )

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        state = listState,
        contentPadding = PaddingValues(
            start = 16.dp,
            end = 16.dp,
            top = statusBarPadding + 8.dp,
            bottom = bottomBarPadding + 16.dp,
        ),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        animatedItem(key = "topBar") {
            TopBar(
                canEdit = state.canEdit,
                canRenameAndReorder = state.canRenameAndReorder,
                isEditing = isEditing,
                pinned = state.pinned,
                onBackClicked = onBackClicked,
                onPinClicked = onPinClicked,
                onDeleteClicked = onDeleteClicked,
                onEditClicked = onEditClicked,
            )
        }
        animatedItem(key = "header") {
            Header(
                key = state.key,
                title = title,
                gamesCount = state.games.size,
                isLoading = state.isLoading,
                isEditing = isEditing,
                onRenameClicked = onRenameClicked,
            )
        }
        when {
            shimmer != null -> items(count = SKELETON_GAMES_COUNT, key = { "skeleton$it" }) {
                SkeletonGameCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .animateItem(),
                    type = GameCardType.Compact,
                    shimmer = shimmer,
                )
            }

            state.games.isEmpty() -> animatedItem(key = "empty") {
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

            else -> items(items = games, key = { it.id }, contentType = { GAME_CONTENT_TYPE }) { game ->
                val isDragged = reorderState.draggedKey == game.id
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .then(
                            if (isDragged) {
                                // Follows the finger above the others, which make way for it.
                                Modifier
                                    .zIndex(1f)
                                    .graphicsLayer {
                                        translationY = reorderState.draggedOffset
                                        scaleX = DRAGGED_SCALE
                                        scaleY = DRAGGED_SCALE
                                    }
                            } else {
                                Modifier.animateItem()
                            }
                        ),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    GameCard(
                        modifier = Modifier.weight(1f),
                        game = game,
                        type = GameCardType.Compact,
                        // While editing, taps would get in the way of dragging.
                        onRemoveClicked = onRemoveGameClicked.takeUnless { isEditing },
                        onClick = onGameClicked.takeUnless { isEditing },
                    )
                    AnimatedVisibility(
                        visible = isEditing,
                        enter = fadeIn() + expandHorizontally(),
                        exit = fadeOut() + shrinkHorizontally(),
                    ) {
                        Icon(
                            modifier = Modifier
                                .padding(start = 4.dp)
                                .size(40.dp)
                                .reorderHandle(reorderState, game.id)
                                .padding(8.dp),
                            imageVector = AppIcons.dragHandle,
                            contentDescription = null,
                            tint = AppTheme.textColors.secondary,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TopBar(
    canEdit: Boolean,
    canRenameAndReorder: Boolean,
    isEditing: Boolean,
    pinned: Boolean,
    onBackClicked: () -> Unit,
    onPinClicked: () -> Unit,
    onDeleteClicked: () -> Unit,
    onEditClicked: () -> Unit,
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
        if (isEditing) {
            GGButton(
                text = stringResource(Res.string.done),
                onClick = onEditClicked,
            )
            return@Row
        }
        if (canRenameAndReorder) {
            GGIconButton(
                imageVector = AppIcons.edit,
                type = GGIconButtonType.OUTLINED,
                onClick = onEditClicked,
            )
        }
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
    isEditing: Boolean,
    onRenameClicked: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Title(modifier = Modifier.weight(1f), text = title)
            AnimatedVisibility(visible = isEditing, enter = fadeIn(), exit = fadeOut()) {
                GGButton(
                    modifier = Modifier.padding(start = 8.dp),
                    text = stringResource(Res.string.rename),
                    onClick = onRenameClicked,
                    buttonType = GGButtonType.OUTLINED,
                )
            }
        }
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
                text = if (isEditing && gamesCount > 1) {
                    stringResource(Res.string.reorderHint)
                } else {
                    pluralStringResource(Res.plurals.gamesCount, gamesCount, gamesCount)
                },
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
        isEditing = false,
        onEditClicked = {},
        onRenameClicked = {},
        onReorder = {},
    )
}
