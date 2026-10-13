package com.royaal.designsystem.uikit

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.royaal.designsystem.components.tag.GGTag
import com.royaal.designsystem.components.tag.GGTagFlowRow
import com.royaal.designsystem.components.tag.GGTagType
import com.royaal.designsystem.components.text.Text
import com.royaal.designsystem.generated.resources.Res
import com.royaal.designsystem.generated.resources.maxPlayers
import com.royaal.designsystem.generated.resources.releaseDate
import com.royaal.designsystem.generated.resources.timeToBeat
import com.royaal.designsystem.theme.AppTheme
import com.royaal.designsystem.theme.textColors
import com.royaal.designsystem.util.AppConst
import com.royaal.designsystem.util.BasePreview
import com.royaal.designsystem.util.TextUtils
import com.royaal.designsystem.util.textsource.asTextSource
import com.royaal.entities.ui.UiGame
import com.royaal.entities.ui.testGame
import org.jetbrains.compose.resources.stringResource

/**
 * Everything known about a game, unclipped: the full description and all platforms and genres.
 * No frame or actions of its own, for a sheet or a screen to put around it.
 *
 * @param showCover `false` when the cover is shown apart, e.g. pinned above the scrolling
 * details with [GameCover].
 */
@Composable
fun GameDetails(
    game: UiGame,
    modifier: Modifier = Modifier,
    showCover: Boolean = true,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        if (showCover) GameCover(game)
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Title(
                    text = game.title,
                    modifier = Modifier
                        .weight(1f)
                        .alignByBaseline(),
                )
                GameRating(rating = game.rating)
            }
            val devYear = remember(game.devTeamTitle, game.releaseYear) {
                listOfNotNull(game.devTeamTitle, game.releaseYear?.toString())
                    .joinToString(" ${TextUtils.DOT_SEPARATOR} ")
                    .takeIf { it.isNotBlank() }
                    ?.asTextSource()
            }
            devYear?.let {
                Text(
                    text = it,
                    style = AppTheme.typography.bodyMedium,
                    color = AppTheme.textColors.secondary,
                )
            }
        }
        game.platforms?.takeIf { it.isNotEmpty() }?.let { platforms ->
            GGTagFlowRow {
                platforms.forEach { GGTag(text = it, type = GGTagType.OUTLINE) }
            }
        }
        game.genres?.takeIf { it.isNotEmpty() }?.let { genres ->
            GGTagFlowRow {
                genres.forEach { GGTag(text = it, type = GGTagType.NEUTRAL) }
            }
        }
        if (game.description.isNotBlank()) {
            Text(
                text = game.description.asTextSource(),
                style = AppTheme.typography.bodyLarge,
            )
        }
        if (game.timeToBeat != null || game.maxPlayers != null || game.releaseDate != null) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                game.timeToBeat?.let {
                    GameInfoBlock(title = stringResource(Res.string.timeToBeat), value = TextUtils.formatHours(it))
                }
                game.maxPlayers?.let {
                    GameInfoBlock(title = stringResource(Res.string.maxPlayers), value = it.toString())
                }
                game.releaseDate?.let {
                    GameInfoBlock(title = stringResource(Res.string.releaseDate), value = it)
                }
            }
        }
    }
}

/** The wide cover of [GameDetails], or the title's first letter if there's none. */
@Composable
fun GameCover(
    game: UiGame,
    modifier: Modifier = Modifier,
) {
    GameImage(
        modifier = modifier
            .fillMaxWidth()
            .clip(AppConst.innerCardShape)
            .aspectRatio(16 / 9f),
        game = game,
    )
}

@Preview(showBackground = true)
@Composable
private fun Preview() = BasePreview {
    GameDetails(game = UiGame.testGame.copy(releaseDate = "12 Nov 2011"))
}
