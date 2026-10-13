package com.royaal.designsystem.uikit

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import com.royaal.designsystem.components.image.ProgressiveImage
import com.royaal.designsystem.components.text.Text
import com.royaal.designsystem.theme.AppTheme
import com.royaal.designsystem.theme.brandColors
import com.royaal.designsystem.util.textsource.asTextSource
import com.royaal.entities.ui.UiGame

@Composable
fun GameImage(
    modifier: Modifier = Modifier,
    gameTitle: String,
    coverUrl: String?,
    coverPreviewUrl: String?,
    style: TextStyle = AppTheme.typography.displayLarge,
) {
    if (coverUrl.isNullOrBlank()) {
        Box(
            modifier.background(AppTheme.brandColors.secondary),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = remember(gameTitle) {
                    gameTitle.take(1).asTextSource()
                },
                color = AppTheme.brandColors.onSecondary,
                style = style,
            )
        }
    } else {
        ProgressiveImage(
            modifier = modifier,
            url = coverUrl,
            previewUrl = coverPreviewUrl,
            contentDescription = null,
            contentScale = ContentScale.Crop,
        )
    }
}

@Composable
fun GameImage(
    modifier: Modifier = Modifier,
    game: UiGame,
    style: TextStyle = AppTheme.typography.displayLarge,
) = GameImage(
    modifier = modifier,
    gameTitle = game.title,
    coverUrl = game.coverUrl,
    coverPreviewUrl = game.coverPreviewUrl,
    style = style,
)