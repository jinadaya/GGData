package com.royaal.designsystem.uikit

import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.royaal.designsystem.components.skeleton.SkeletonBox
import com.royaal.designsystem.components.skeleton.SkeletonText
import com.royaal.designsystem.components.skeleton.rememberSkeletonShimmer
import com.royaal.designsystem.components.text.Text
import com.royaal.designsystem.theme.AppTheme
import com.royaal.designsystem.theme.textColors
import com.royaal.designsystem.util.AppConst
import com.royaal.designsystem.util.BasePreview
import com.royaal.designsystem.util.baseShadow
import com.royaal.designsystem.util.textsource.asTextSource
import com.royaal.entities.ui.UiGame
import com.royaal.entities.ui.testGame

/** Width of [GameCoverCard] in a horizontal row. */
val GameCoverCardWidth: Dp = 120.dp

/**
 * A portrait cover with the title and the year under it, for horizontal rows of games ("From the
 * same series", "Similar games").
 */
@Composable
fun GameCoverCard(
    game: UiGame,
    onClick: (UiGame) -> Unit,
    modifier: Modifier = Modifier,
    onLongClick: ((UiGame) -> Unit)? = null,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val haptics = LocalHapticFeedback.current
    Column(
        modifier = modifier
            .width(GameCoverCardWidth)
            .combinedClickable(
                interactionSource = interactionSource,
                // Like GameCard: a pressed card loses its shadow instead of shrinking.
                indication = null,
                onClick = { onClick(game) },
                onLongClick = onLongClick?.let { onLongClick ->
                    {
                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                        onLongClick(game)
                    }
                },
            ),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        GameImage(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(3 / 4f)
                .baseShadow(shadowShape = { AppConst.innerCardShape }, pressInteractionSource = interactionSource)
                .clip(AppConst.innerCardShape),
            game = game,
        )
        Text(
            text = game.title.asTextSource(),
            style = AppTheme.typography.labelLarge,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
        game.releaseYear?.let { year ->
            Text(
                text = year.toString().asTextSource(),
                style = AppTheme.typography.labelMedium,
                color = AppTheme.textColors.secondary,
            )
        }
    }
}

@Composable
fun SkeletonGameCoverCard(
    modifier: Modifier = Modifier,
    shimmer: State<Float> = rememberSkeletonShimmer(),
) {
    Column(
        modifier = modifier.width(GameCoverCardWidth),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        SkeletonBox(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(3 / 4f),
            shimmer = shimmer,
            shape = AppConst.innerCardShape,
        )
        SkeletonText(AppTheme.typography.labelLarge, Modifier.fillMaxWidth(), shimmer = shimmer)
        SkeletonText(AppTheme.typography.labelMedium, Modifier.width(32.dp), shimmer = shimmer)
    }
}

@Preview(showBackground = true)
@Composable
private fun Preview() = BasePreview {
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        GameCoverCard(game = UiGame.testGame, onClick = {})
        SkeletonGameCoverCard()
    }
}
