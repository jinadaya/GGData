package com.royaal.journal.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.plus
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.royaal.designsystem.components.button.GGButton
import com.royaal.designsystem.components.button.GGIconButton
import com.royaal.designsystem.components.button.GGIconButtonType
import com.royaal.designsystem.components.chips.GGFilterChip
import com.royaal.designsystem.components.chips.GGFilterChipRow
import com.royaal.designsystem.components.screen.LocalContentPadding
import com.royaal.designsystem.icons.AppIcons
import com.royaal.designsystem.theme.AppTheme
import com.royaal.designsystem.theme.backgroundColors
import com.royaal.designsystem.theme.brandColors
import com.royaal.designsystem.theme.textColors
import com.royaal.designsystem.uikit.GameImage
import com.royaal.designsystem.util.BasePreview
import com.royaal.designsystem.util.TextUtils
import com.royaal.designsystem.util.animatedItem
import com.royaal.designsystem.util.baseShadow
import com.royaal.entities.data.GamePlayStatus
import com.royaal.entities.ui.UiGame
import com.royaal.entities.ui.testGame
import com.royaal.game.navigation.GameRoute
import com.royaal.game.ui.common.JournalStatuses
import com.royaal.game.ui.common.title
import com.royaal.game.ui.journal.JournalEntrySheet
import com.royaal.game.ui.journal.rememberJournalSavedSnackbar
import com.royaal.journal.domain.JournalStats
import com.royaal.navigation.LocalNavigator
import com.royaal.search.navigation.SearchRoute
import com.royaal.search.ui.SearchButton
import ggdata.feature.journal.generated.resources.Res
import ggdata.feature.journal.generated.resources.emptyDescription
import ggdata.feature.journal.generated.resources.emptyTitle
import ggdata.feature.journal.generated.resources.findGame
import ggdata.feature.journal.generated.resources.hours
import ggdata.feature.journal.generated.resources.journal
import ggdata.feature.journal.generated.resources.noGamesWithStatus
import ggdata.feature.journal.generated.resources.rating
import ggdata.feature.journal.generated.resources.statCompleted
import ggdata.feature.journal.generated.resources.statDropped
import ggdata.feature.journal.generated.resources.statHours
import ggdata.feature.journal.generated.resources.statStarted
import ggdata.feature.journal.generated.resources.thisMonth
import ggdata.feature.journal.generated.resources.thisYear
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

private val ScreenHorizontalPadding = 16.dp
private val CardShape = RoundedCornerShape(12.dp)
private val CoverShape = RoundedCornerShape(8.dp)
private val CoverWidth = 48.dp

@Composable
internal fun JournalScreen() {
    val viewModel = koinViewModel<JournalViewModel>()
    val state by viewModel.state.collectAsStateWithLifecycle()
    val navigator = LocalNavigator.current
    var editingGameId by rememberSaveable { mutableStateOf<Long?>(null) }
    val onSaved = rememberJournalSavedSnackbar()

    JournalContent(
        state = state,
        onPeriodClicked = { viewModel.sendEvent(JournalUserEvent.PeriodClicked(it)) },
        onStatusClicked = { viewModel.sendEvent(JournalUserEvent.StatusClicked(it)) },
        onGameClicked = { navigator.navigate(GameRoute(it.id)) },
        onEditClicked = { editingGameId = it.id },
        onFindGameClicked = { navigator.navigate(SearchRoute) },
    )

    state.games?.firstOrNull { it.game.id == editingGameId }?.let { item ->
        JournalEntrySheet(
            game = item.game,
            onDismissRequest = { editingGameId = null },
            onSaved = onSaved,
        )
    }
}

@Composable
private fun JournalContent(
    state: JournalScreenState,
    onPeriodClicked: (StatsPeriod) -> Unit,
    onStatusClicked: (GamePlayStatus) -> Unit,
    onGameClicked: (UiGame) -> Unit,
    onEditClicked: (UiGame) -> Unit,
    onFindGameClicked: () -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        // Unlike the bottom bar, the status bar isn't in the content padding.
        contentPadding = LocalContentPadding.current + PaddingValues(
            top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding() + 8.dp,
            bottom = 16.dp,
        ),
    ) {
        animatedItem(key = "header") {
            Row(
                modifier = Modifier.padding(horizontal = ScreenHorizontalPadding),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    modifier = Modifier.weight(1f),
                    text = stringResource(Res.string.journal),
                    style = AppTheme.typography.headlineLarge,
                )
                SearchButton()
            }
        }
        when {
            // Read from the database: it's there before a skeleton would be seen.
            state.isLoading -> Unit
            state.isEmpty -> animatedItem(key = "empty") {
                EmptyJournal(onFindGameClicked = onFindGameClicked)
            }

            else -> {
                animatedItem(key = "stats") {
                    Stats(
                        stats = state.stats,
                        period = state.period,
                        onPeriodClicked = onPeriodClicked,
                    )
                }
                animatedItem(key = "statuses") {
                    GGFilterChipRow(
                        modifier = Modifier.padding(top = 16.dp),
                        contentPadding = PaddingValues(horizontal = ScreenHorizontalPadding),
                    ) {
                        JournalStatuses.forEach { status ->
                            GGFilterChip(
                                text = stringResource(status.title),
                                count = state.count(status).toString(),
                                selected = status == state.shownStatus,
                                onClick = { onStatusClicked(status) },
                            )
                        }
                    }
                }
                val games = state.shownGames
                if (games.isEmpty()) {
                    animatedItem(key = "noGames") {
                        Text(
                            modifier = Modifier.padding(horizontal = ScreenHorizontalPadding),
                            text = stringResource(Res.string.noGamesWithStatus),
                            style = AppTheme.typography.bodyMedium,
                            color = AppTheme.textColors.secondary,
                        )
                    }
                }
                items(items = games, key = { it.game.id }) { item ->
                    JournalGameCard(
                        modifier = Modifier
                            .animateItem()
                            .padding(horizontal = ScreenHorizontalPadding),
                        item = item,
                        onClick = onGameClicked,
                        onEditClicked = onEditClicked,
                    )
                }
            }
        }
    }
}

@Composable
private fun Stats(
    stats: JournalStats,
    period: StatsPeriod,
    onPeriodClicked: (StatsPeriod) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        GGFilterChipRow(contentPadding = PaddingValues(horizontal = ScreenHorizontalPadding)) {
            GGFilterChip(
                text = stringResource(Res.string.thisMonth),
                selected = period == StatsPeriod.MONTH,
                onClick = { onPeriodClicked(StatsPeriod.MONTH) },
            )
            GGFilterChip(
                text = stringResource(Res.string.thisYear),
                selected = period == StatsPeriod.YEAR,
                onClick = { onPeriodClicked(StatsPeriod.YEAR) },
            )
        }
        Column(
            modifier = Modifier.padding(horizontal = ScreenHorizontalPadding),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            // Tiles of a row as tall as the tallest: a label may take two lines.
            Row(
                modifier = Modifier.height(IntrinsicSize.Min),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                StatTile(Modifier.weight(1f), stats.completed, stringResource(Res.string.statCompleted))
                StatTile(Modifier.weight(1f), stats.started, stringResource(Res.string.statStarted))
            }
            Row(
                modifier = Modifier.height(IntrinsicSize.Min),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                StatTile(Modifier.weight(1f), stats.dropped, stringResource(Res.string.statDropped))
                StatTile(Modifier.weight(1f), stats.hours, stringResource(Res.string.statHours))
            }
        }
    }
}

@Composable
private fun StatTile(
    modifier: Modifier,
    value: Int,
    label: String,
) {
    Column(
        modifier = modifier
            .fillMaxHeight()
            .border(width = 1.dp, color = AppTheme.brandColors.secondaryContainer, shape = CardShape)
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        // Switching the period, a number rolls up as it grows and down as it shrinks.
        AnimatedContent(
            targetState = value,
            transitionSpec = {
                val direction = if (targetState > initialState) 1 else -1
                (slideInVertically { height -> height * direction } + fadeIn()) togetherWith
                    (slideOutVertically { height -> -height * direction } + fadeOut())
            },
            label = "StatValue",
        ) { shownValue ->
            Text(text = shownValue.toString(), style = AppTheme.typography.headlineLarge)
        }
        Text(
            text = label,
            style = AppTheme.typography.labelLarge,
            color = AppTheme.textColors.secondary,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

/** A game with what the user has logged on it: hours, rating, note. */
@Composable
private fun JournalGameCard(
    modifier: Modifier = Modifier,
    item: UiJournalGame,
    onClick: (UiGame) -> Unit,
    onEditClicked: (UiGame) -> Unit,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .baseShadow(click = { onClick(item.game) })
            .background(color = AppTheme.backgroundColors.background, shape = CardShape)
            .border(width = 1.dp, color = AppTheme.brandColors.secondaryContainer, shape = CardShape)
            .padding(8.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        GameImage(
            modifier = Modifier
                .clip(CoverShape)
                .width(CoverWidth)
                .aspectRatio(3 / 4f),
            game = item.game,
            style = AppTheme.typography.bodyMedium,
        )
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(
                text = item.game.title,
                style = AppTheme.typography.titleLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            val details = listOfNotNull(
                item.hoursPlayed?.let { stringResource(Res.string.hours, it) },
                item.rating?.let { stringResource(Res.string.rating, it) },
            )
            if (details.isNotEmpty()) {
                Text(
                    text = details.joinToString(" ${TextUtils.DOT_SEPARATOR} "),
                    style = AppTheme.typography.bodyMedium,
                )
            }
            item.note?.let { note ->
                Text(
                    text = note,
                    style = AppTheme.typography.bodySmall,
                    color = AppTheme.textColors.secondary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
        GGIconButton(
            imageVector = AppIcons.edit,
            type = GGIconButtonType.FLATTENED,
            onClick = { onEditClicked(item.game) },
        )
    }
}

@Composable
private fun EmptyJournal(onFindGameClicked: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 32.dp, vertical = 48.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            modifier = Modifier.size(48.dp),
            imageVector = AppIcons.journal,
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
        GGButton(
            modifier = Modifier.padding(top = 8.dp),
            text = stringResource(Res.string.findGame),
            onClick = onFindGameClicked,
            iconStart = AppIcons.search,
        )
    }
}

@Preview(showBackground = true, heightDp = 900)
@Composable
private fun Preview() = BasePreview {
    JournalContent(
        state = JournalScreenState(
            games = listOf(
                UiJournalGame(UiGame.testGame.copy(id = 1), GamePlayStatus.PLAYING, 40, 8, "Slow start, then great."),
                UiJournalGame(UiGame.testGame.copy(id = 2), GamePlayStatus.PLAYING, null, null, null),
                UiJournalGame(UiGame.testGame.copy(id = 3), GamePlayStatus.COMPLETED, 25, 9, null),
            ),
            monthStats = JournalStats(started = 2, completed = 1, dropped = 0, hours = 25),
        ),
        onPeriodClicked = {},
        onStatusClicked = {},
        onGameClicked = {},
        onEditClicked = {},
        onFindGameClicked = {},
    )
    JournalContent(
        state = JournalScreenState(games = emptyList()),
        onPeriodClicked = {},
        onStatusClicked = {},
        onGameClicked = {},
        onEditClicked = {},
        onFindGameClicked = {},
    )
}
