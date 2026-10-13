package com.royaal.collections.ui.list

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.royaal.arch.mvi.EffectHandler
import com.royaal.collections.domain.CollectionKey
import com.royaal.collections.ui.common.UiCollection
import com.royaal.collections.ui.common.route
import com.royaal.collections.ui.common.title
import com.royaal.designsystem.components.button.GGButton
import com.royaal.designsystem.components.button.GGButtonType
import com.royaal.designsystem.components.chips.GGFilterChip
import com.royaal.designsystem.components.screen.LocalContentPadding
import com.royaal.designsystem.components.skeleton.rememberSkeletonShimmer
import com.royaal.designsystem.icons.AppIcons
import com.royaal.designsystem.theme.AppTheme
import com.royaal.designsystem.theme.textColors
import com.royaal.designsystem.uikit.CollectionCard
import com.royaal.designsystem.uikit.SkeletonCollectionCard
import com.royaal.designsystem.util.BasePreview
import com.royaal.entities.data.GamePlayStatus
import com.royaal.entities.ui.UiGame
import com.royaal.entities.ui.UiGameCollection
import com.royaal.entities.ui.testGame
import com.royaal.navigation.LocalNavigator
import ggdata.feature.collections.generated.resources.Res
import ggdata.feature.collections.generated.resources.addStatusCollections
import ggdata.feature.collections.generated.resources.allCollections
import ggdata.feature.collections.generated.resources.collections
import ggdata.feature.collections.generated.resources.createOwn
import ggdata.feature.collections.generated.resources.emptyDescription
import ggdata.feature.collections.generated.resources.emptyTitle
import ggdata.feature.collections.generated.resources.newCollection
import ggdata.feature.collections.generated.resources.pinned
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

private const val SKELETON_CARDS_COUNT = 4

/** Suggested on the empty screen; dropped games are rarely what people start a library with. */
private val DefaultEmptyStatuses = setOf(GamePlayStatus.BACKLOG, GamePlayStatus.PLAYING, GamePlayStatus.COMPLETED)

@Composable
internal fun CollectionsScreen() {
    val viewModel = koinViewModel<CollectionsViewModel>()
    val state = viewModel.state.collectAsStateWithLifecycle()
    val navigator = LocalNavigator.current
    var showCreateDialog by rememberSaveable { mutableStateOf(false) }

    EffectHandler(viewModel) { effect ->
        when (effect) {
            is CollectionsSideEffect.OpenCollection -> navigator.navigate(effect.key.route)
        }
    }

    CollectionsContent(
        state = state.value,
        onCollectionClicked = { viewModel.sendEvent(CollectionsUserEvent.CollectionClicked(it)) },
        onCreateClicked = { showCreateDialog = true },
        onAddStatusCollections = { viewModel.sendEvent(CollectionsUserEvent.AddStatusCollections(it)) },
    )

    if (showCreateDialog) {
        CreateCollectionDialog(
            takenNames = state.value.customNames,
            statusesToAdd = state.value.statusesToAdd,
            onCreate = { viewModel.sendEvent(CollectionsUserEvent.CreateCollection(it)) },
            onAddStatus = { viewModel.sendEvent(CollectionsUserEvent.AddStatusCollections(listOf(it))) },
            onDismissRequest = { showCreateDialog = false },
        )
    }
}

@Composable
private fun CollectionsContent(
    state: CollectionsScreenState,
    onCollectionClicked: (CollectionKey) -> Unit,
    onCreateClicked: () -> Unit,
    onAddStatusCollections: (List<GamePlayStatus>) -> Unit,
) {
    val statusBarPadding = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val bottomBarPadding = LocalContentPadding.current.calculateBottomPadding()
    // Only while skeletons are shown: a remembered shimmer keeps animating, needed or not.
    val shimmer = if (state.isLoading) rememberSkeletonShimmer() else null

    LazyVerticalGrid(
        modifier = Modifier.fillMaxSize(),
        columns = GridCells.Fixed(3),
        contentPadding = PaddingValues(
            start = 16.dp,
            end = 16.dp,
            top = statusBarPadding + 8.dp,
            bottom = bottomBarPadding + 16.dp,
        ),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        fullWidthItem(key = "header") {
            Header(onCreateClicked = onCreateClicked)
        }
        if (shimmer != null) {
            items(count = SKELETON_CARDS_COUNT, key = { "skeleton$it" }) {
                Box(contentAlignment = Alignment.Center) {
                    SkeletonCollectionCard(shimmer = shimmer)
                }
            }
            return@LazyVerticalGrid
        }

        fullWidthItem(key = "pinnedTitle") {
            SectionTitle(stringResource(Res.string.pinned))
        }
        collectionItems(state.topCollections, onCollectionClicked)

        if (state.isEmpty) {
            fullWidthItem(key = "empty") {
                EmptyCollections(
                    statuses = state.statusesToAdd,
                    onAddStatusCollections = onAddStatusCollections,
                    onCreateClicked = onCreateClicked,
                )
            }
        } else if (state.otherCollections.isNotEmpty()) {
            fullWidthItem(key = "allTitle") {
                SectionTitle(stringResource(Res.string.allCollections))
            }
            collectionItems(state.otherCollections, onCollectionClicked)
        }
    }
}

private fun LazyGridScope.fullWidthItem(key: String, content: @Composable () -> Unit) {
    item(key = key, span = { GridItemSpan(maxLineSpan) }) { content() }
}

private fun LazyGridScope.collectionItems(
    collections: List<UiCollection>,
    onCollectionClicked: (CollectionKey) -> Unit,
) {
    items(count = collections.size, key = { collections[it].key.toString() }) { index ->
        val collection = collections[index]
        Box(contentAlignment = Alignment.Center) {
            CollectionCard(
                collection = UiGameCollection(
                    id = (collection.key as? CollectionKey.Custom)?.id ?: -1,
                    title = collection.key.title(collection.name),
                    gamesCount = collection.gamesCount,
                    gameIds = collection.preview.map { it.id.toString() },
                ),
                preview = collection.preview,
                onClick = { onCollectionClicked(collection.key) },
            )
        }
    }
}

@Composable
private fun Header(onCreateClicked: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            modifier = Modifier.weight(1f),
            text = stringResource(Res.string.collections),
            style = AppTheme.typography.headlineLarge,
        )
        GGButton(
            text = stringResource(Res.string.newCollection),
            onClick = onCreateClicked,
            buttonType = GGButtonType.OUTLINED,
            iconStart = AppIcons.add,
        )
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        modifier = Modifier.padding(top = 8.dp),
        text = text,
        style = AppTheme.typography.headlineMedium,
    )
}

/** No collections but favourites: offers the status ones in one go, or a custom one. */
@Composable
private fun EmptyCollections(
    statuses: List<GamePlayStatus>,
    onAddStatusCollections: (List<GamePlayStatus>) -> Unit,
    onCreateClicked: () -> Unit,
) {
    var selected by remember { mutableStateOf(DefaultEmptyStatuses) }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = stringResource(Res.string.emptyTitle),
            style = AppTheme.typography.headlineMedium,
        )
        Text(
            text = stringResource(Res.string.emptyDescription),
            style = AppTheme.typography.bodyMedium,
            color = AppTheme.textColors.secondary,
        )
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            statuses.forEach { status ->
                val isSelected = status in selected
                GGFilterChip(
                    text = stringResource(status.title),
                    selected = isSelected,
                    onClick = { selected = if (isSelected) selected - status else selected + status },
                )
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            GGButton(
                text = stringResource(Res.string.addStatusCollections),
                enabled = statuses.any { it in selected },
                onClick = { onAddStatusCollections(statuses.filter { it in selected }) },
            )
            GGButton(
                text = stringResource(Res.string.createOwn),
                onClick = onCreateClicked,
                buttonType = GGButtonType.OUTLINED,
            )
        }
    }
}

@Preview(showBackground = true, heightDp = 900)
@Composable
private fun Preview() = BasePreview {
    val favourites = UiCollection(
        key = CollectionKey.Favourites,
        name = "",
        pinned = false,
        gamesCount = 2,
        preview = listOf(UiGame.testGame, UiGame.testGame),
    )
    CollectionsContent(
        state = CollectionsScreenState(collections = listOf(favourites), isLoading = false),
        onCollectionClicked = {},
        onCreateClicked = {},
        onAddStatusCollections = {},
    )
}
