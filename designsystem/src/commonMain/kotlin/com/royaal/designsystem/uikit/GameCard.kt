package com.royaal.designsystem.uikit

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastJoinToString
import com.royaal.designsystem.components.button.GGButton
import com.royaal.designsystem.components.button.GGButtonType
import com.royaal.designsystem.components.button.GGIconButton
import com.royaal.designsystem.components.button.GGIconButtonType
import com.royaal.designsystem.components.image.ProgressiveImage
import com.royaal.designsystem.components.skeleton.SkeletonBox
import com.royaal.designsystem.components.skeleton.SkeletonText
import com.royaal.designsystem.components.skeleton.rememberSkeletonShimmer
import com.royaal.designsystem.components.tag.GGSkeletonTag
import com.royaal.designsystem.components.tag.GGTag
import com.royaal.designsystem.components.tag.GGTagType
import com.royaal.designsystem.components.text.Text
import com.royaal.designsystem.generated.resources.Res
import com.royaal.designsystem.generated.resources.addToCollection
import com.royaal.designsystem.generated.resources.maxPlayers
import com.royaal.designsystem.generated.resources.releaseDate
import com.royaal.designsystem.generated.resources.timeToBeat
import com.royaal.designsystem.icons.AppIcons
import com.royaal.designsystem.theme.AppTheme
import com.royaal.designsystem.theme.backgroundColors
import com.royaal.designsystem.theme.brandColors
import com.royaal.designsystem.theme.textColors
import com.royaal.designsystem.util.AppConst
import com.royaal.designsystem.util.BasePreview
import com.royaal.designsystem.util.TextUtils
import com.royaal.designsystem.util.baseShadow
import com.royaal.designsystem.util.textsource.TextSource
import com.royaal.designsystem.util.textsource.asTextSource
import com.royaal.entities.ui.UiGame
import com.royaal.entities.ui.testGame
import kotlin.math.roundToInt
import org.jetbrains.compose.resources.stringResource

private const val MAX_RATING = 100f
private const val SKELETON_TAGS_COUNT = 3
private val SKELETON_BUTTON_HEIGHT = 40.dp

enum class GameCardType {
    Detailed,
    Compact
    ;
}

/** [GameCard] while its game is loading: same frame and layout, placeholders instead of the data. */
@Composable
fun SkeletonGameCard(
    modifier: Modifier = Modifier,
    type: GameCardType = GameCardType.Detailed,
    shimmer: State<Float> = rememberSkeletonShimmer(),
) {
    when (type) {
        GameCardType.Detailed -> DetailedSkeletonGameCard(modifier, shimmer)
        GameCardType.Compact -> CompactSkeletonGameCard(modifier, shimmer)
    }
}

@Composable
private fun DetailedSkeletonGameCard(
    modifier: Modifier,
    shimmer: State<Float>,
) {
    Column(
        modifier = modifier
            .border(
                width = 1.dp,
                color = AppTheme.brandColors.secondaryContainer,
                shape = AppConst.cardShape,
            )
            .baseShadow()
            .background(
                color = AppTheme.backgroundColors.background,
                shape = AppConst.cardShape,
            )
            .clip(AppConst.cardShape),
    ) {
        SkeletonBox(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(16 / 9f),
            shimmer = shimmer,
            shape = RectangleShape,
        )
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                SkeletonText(
                    style = AppTheme.typography.displaySmall,
                    modifier = Modifier.weight(1f),
                    shimmer = shimmer,
                )
                SkeletonText(
                    style = AppTheme.typography.headlineLarge,
                    modifier = Modifier.width(56.dp),
                    shimmer = shimmer,
                )
            }
            SkeletonText(
                style = AppTheme.typography.bodyMedium,
                modifier = Modifier.fillMaxWidth(0.4f),
                shimmer = shimmer,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                repeat(SKELETON_TAGS_COUNT) {
                    GGSkeletonTag(shimmer = shimmer)
                }
            }
            Spacer(Modifier.height(4.dp))
            SkeletonText(
                style = AppTheme.typography.bodyLarge,
                modifier = Modifier.fillMaxWidth(),
                lines = 4,
                shimmer = shimmer,
            )
            Spacer(Modifier.height(4.dp))
            SkeletonBox(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(SKELETON_BUTTON_HEIGHT),
                shimmer = shimmer,
                shape = AppConst.innerCardShape,
            )
        }
    }
}

@Composable
private fun CompactSkeletonGameCard(
    modifier: Modifier,
    shimmer: State<Float>,
) {
    Row(
        modifier = modifier
            .baseShadow()
            .background(
                color = AppTheme.backgroundColors.background,
                shape = AppConst.cardShape,
            )
            .border(
                width = 1.dp,
                color = AppTheme.brandColors.secondaryContainer,
                shape = AppConst.cardShape,
            )
            .padding(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        SkeletonBox(
            modifier = Modifier
                .sizeIn(maxHeight = 64.dp, maxWidth = 48.dp)
                .aspectRatio(3 / 4f),
            shimmer = shimmer,
            shape = AppConst.innerCardShape,
        )
        Column(modifier = Modifier.weight(1f)) {
            SkeletonText(
                style = AppTheme.typography.titleLarge,
                modifier = Modifier.fillMaxWidth(0.7f),
                shimmer = shimmer,
            )
            SkeletonText(
                style = AppTheme.typography.bodyLarge,
                modifier = Modifier.fillMaxWidth(0.5f),
                shimmer = shimmer,
            )
        }
        SkeletonText(
            style = AppTheme.typography.headlineLarge,
            modifier = Modifier.width(48.dp),
            shimmer = shimmer,
        )
    }
}

/**
 * @param onRemoveClicked if set, a compact card shows × (remove) instead of + (add).
 * @param onClick, onLongClick taps on the card itself; while either is set, the card's shadow
 * goes away when pressed, as if it's pushed down to the surface.
 */
@Composable
fun GameCard(
    modifier: Modifier = Modifier,
    game: UiGame,
    onAddClicked: ((game: UiGame) -> Unit)? = null,
    onAddToFavouriteClicked: ((game: UiGame) -> Unit)? = null,
    type: GameCardType = GameCardType.Detailed,
    onRemoveClicked: ((game: UiGame) -> Unit)? = null,
    onClick: ((game: UiGame) -> Unit)? = null,
    onLongClick: ((game: UiGame) -> Unit)? = null,
) {
    val interactionSource = if (onClick != null || onLongClick != null) {
        remember { MutableInteractionSource() }
    } else {
        null
    }
    val cardModifier = modifier.cardClicks(game, interactionSource, onClick, onLongClick)
    when (type) {
        GameCardType.Detailed -> DetailedGameCard(
            cardModifier, game, onAddClicked, onAddToFavouriteClicked, interactionSource,
        )

        GameCardType.Compact -> CompactGameCard(
            cardModifier, game, onAddClicked, onRemoveClicked, interactionSource,
        )
    }
}

@Composable
private fun Modifier.cardClicks(
    game: UiGame,
    interactionSource: MutableInteractionSource?,
    onClick: ((game: UiGame) -> Unit)?,
    onLongClick: ((game: UiGame) -> Unit)?,
): Modifier {
    if (interactionSource == null) return this
    val haptics = LocalHapticFeedback.current
    return combinedClickable(
        interactionSource = interactionSource,
        // The shadow going away is the feedback (see baseShadow); a ripple wouldn't follow the border.
        indication = null,
        onClick = { onClick?.invoke(game) },
        onLongClick = onLongClick?.let { onLongClick ->
            {
                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                onLongClick(game)
            }
        },
    )
}

@Composable
private fun DetailedGameCard(
    modifier: Modifier = Modifier,
    game: UiGame,
    onAddClicked: ((game: UiGame) -> Unit)? = null,
    onAddToFavouriteClicked: ((game: UiGame) -> Unit)? = null,
    interactionSource: MutableInteractionSource? = null,
) {
    Column(
        modifier = modifier
            .border(
                width = 1.dp,
                color = AppTheme.brandColors.secondaryContainer,
                shape = AppConst.cardShape,
            )
            .baseShadow(pressInteractionSource = interactionSource)
            .background(
                color = AppTheme.backgroundColors.background,
                shape = AppConst.cardShape,
            )
            .clip(AppConst.cardShape),
    ) {
        val coverUrl = game.coverUrl
        if (coverUrl.isNullOrBlank()) {
            Box(
                Modifier
                    .aspectRatio(16 / 9f)
                    .background(AppTheme.brandColors.secondary),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = remember(game.title) {
                        game.title.take(1).asTextSource()
                    },
                    color = AppTheme.brandColors.onSecondary,
                    style = AppTheme.typography.displayLarge,
                )
            }
        } else {
            ProgressiveImage(
                modifier = Modifier.aspectRatio(16 / 9f),
                url = coverUrl,
                previewUrl = game.coverPreviewUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
            )
        }
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Title(
                    text = game.title,
                    modifier = Modifier
                        .weight(1f)
                        .alignByBaseline()
                )
                GameRating(rating = game.rating)
            }
            val devYear: TextSource? = remember(game.devTeamTitle, game.releaseYear) {
                val dev = game.devTeamTitle
                val year = game.releaseYear
                buildString {
                    if (dev != null) append("$dev ${TextUtils.DOT_SEPARATOR}")
                    if (year != null) append(year)
                }.takeIf { it.isNotBlank() }?.asTextSource()
            }
            devYear?.let { devYear ->
                Text(
                    text = devYear,
                    style = AppTheme.typography.bodyMedium,
                    color = AppTheme.textColors.secondary,
                )
            }
            game.platforms?.let { platforms ->
                FlowRow(
                    modifier = Modifier.scrollable(
                        state = rememberScrollState(),
                        orientation = Orientation.Horizontal,
                    ),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    itemVerticalAlignment = Alignment.CenterVertically,
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    platforms.forEach { platform ->
                        GGTag(
                            text = platform,
                            type = GGTagType.OUTLINE,
                        )
                    }
                }
            }
            game.genres?.let { genres ->
                Row(
                    modifier = Modifier.scrollable(
                        state = rememberScrollState(),
                        orientation = Orientation.Horizontal,
                    ),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    genres.forEach { genre ->
                        GGTag(
                            text = genre,
                            type = GGTagType.NEUTRAL,
                        )
                    }
                }
            }
            Spacer(Modifier.height(4.dp))
            Text(
                text = game.description.asTextSource(),
                maxLines = 4,
                style = AppTheme.typography.bodyLarge,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                game.timeToBeat?.let { timeToBeat ->
                    GameInfoBlock(
                        title = stringResource(Res.string.timeToBeat),
                        value = TextUtils.formatHours(timeToBeat),
                    )
                }
                game.maxPlayers?.let { maxPlayers ->
                    GameInfoBlock(
                        title = stringResource(Res.string.maxPlayers),
                        value = maxPlayers.toString(),
                    )
                }
                game.releaseDate?.let { releaseDate ->
                    GameInfoBlock(
                        title = stringResource(Res.string.releaseDate),
                        value = releaseDate,
                    )
                }
            }
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                GGButton(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    text = stringResource(Res.string.addToCollection),
                    onClick = {
                        onAddClicked?.invoke(game)
                    },
                    buttonType = GGButtonType.NORMAL,
                    iconStart = AppIcons.add,
                )
                GGIconButton(
                    imageVector = AppIcons.favorite,
                    type = GGIconButtonType.OUTLINED,
                    onClick = {
                        onAddToFavouriteClicked?.invoke(game)
                    },
                )
            }
        }
    }
}

@Composable
internal fun RowScope.GameInfoBlock(
    title: String,
    value: String,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .weight(1f),
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(
            text = title.asTextSource(),
            style = AppTheme.typography.labelLarge,
            color = AppTheme.textColors.secondary,
        )
        Text(
            text = value.asTextSource(),
            style = AppTheme.typography.labelLarge,
            color = AppTheme.textColors.primary,
        )
    }
}

@Composable
internal fun GameRating(
    modifier: Modifier = Modifier,
    rating: Float,
    maxRating: Float = MAX_RATING,
) {
    if (rating < 0f) return
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.Bottom,
    ) {
        Text(
            modifier = Modifier.alignByBaseline(),
            text = remember(rating) { rating.roundToInt().toString().asTextSource() },
            style = AppTheme.typography.headlineLarge,
            color = AppTheme.textColors.primary,
        )
        Text(
            modifier = Modifier.alignByBaseline(),
            text = remember(maxRating) {
                "/${maxRating.roundToInt()}".asTextSource()
            },
            color = AppTheme.textColors.secondary,
        )
    }
}

@Composable
private fun CompactGameCard(
    modifier: Modifier = Modifier,
    game: UiGame,
    onAddClicked: ((game: UiGame) -> Unit)? = null,
    onRemoveClicked: ((game: UiGame) -> Unit)? = null,
    interactionSource: MutableInteractionSource? = null,
) {
    Row(
        modifier = modifier
            .baseShadow(pressInteractionSource = interactionSource)
            .background(
                color = AppTheme.backgroundColors.background,
                shape = AppConst.cardShape,
            )
            .border(
                width = 1.dp,
                color = AppTheme.brandColors.secondaryContainer,
                shape = AppConst.cardShape,
            )
            .padding(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        val coverUrl = game.coverUrl
        GameImage(
            modifier = Modifier
                .clip(AppConst.innerCardShape)
                .background(color = AppTheme.brandColors.secondary)
                .sizeIn(maxHeight = 64.dp, maxWidth = 48.dp)
                .aspectRatio(3/4f),
            game = game,
            style = AppTheme.typography.bodyMedium,
        )
        Column(
            modifier = Modifier.weight(1f),
        ) {
            Text(
                text = game.title.asTextSource(),
                style = AppTheme.typography.titleLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = remember(game.releaseYear, game.platforms) {
                    buildString {
                        val year = game.releaseYear
                        val platforms = game.platforms
                        if (year != null) {
                            append(year)
                            append(' ')
                            append(TextUtils.DOT_SEPARATOR)
                            append(' ')
                        }
                        if (!platforms.isNullOrEmpty()) {
                            append(
                                platforms.fastJoinToString(
                                    separator = ", ",
                                )
                            )
                        }
                    }.asTextSource()
                },
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,

            )
        }
        GameRating(
            modifier = Modifier,
            rating = game.rating
        )
        // Inside a collection the card's action takes the game out of it instead of adding.
        if (onRemoveClicked != null) {
            GGIconButton(
                imageVector = AppIcons.close,
                onClick = { onRemoveClicked(game) },
                type = GGIconButtonType.FLATTENED,
            )
        } else {
            GGIconButton(
                imageVector = AppIcons.add,
                onClick = {
                    onAddClicked?.invoke(game)
                },
                type = GGIconButtonType.FLATTENED,
            )
        }
    }
}

@Preview
@Composable
private fun Preview() = BasePreview {
    GameCard(
        game = UiGame.testGame,
    )
    GameCard(
        game = UiGame.testGame,
        onAddClicked = {},
        type = GameCardType.Compact
    )
    SkeletonGameCard(type = GameCardType.Detailed)
    SkeletonGameCard(type = GameCardType.Compact)
}