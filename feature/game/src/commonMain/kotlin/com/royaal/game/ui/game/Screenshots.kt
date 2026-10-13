package com.royaal.game.ui.game

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.royaal.designsystem.components.button.GGIconButton
import com.royaal.designsystem.components.button.GGIconButtonType
import com.royaal.designsystem.components.image.ProgressiveImage
import com.royaal.designsystem.components.skeleton.SkeletonBox
import com.royaal.designsystem.icons.AppIcons
import com.royaal.designsystem.theme.AppTheme
import com.royaal.designsystem.util.click
import com.royaal.designsystem.util.horizontalScrollHint
import com.royaal.game.domain.GameScreenshot
import ggdata.feature.game.generated.resources.Res
import ggdata.feature.game.generated.resources.screenshotCounter
import ggdata.feature.game.generated.resources.screenshots
import org.jetbrains.compose.resources.stringResource

private val ScreenshotWidth = 280.dp
private val ScreenshotShape = RoundedCornerShape(12.dp)
private const val SCREENSHOT_ASPECT_RATIO = 16 / 9f
private const val SKELETON_SCREENSHOTS = 2

/**
 * A titled row of screenshots, edge to edge. Skeletons while [screenshots] are `null`, nothing
 * once they're known to be empty.
 */
@Composable
internal fun ScreenshotsRow(
    screenshots: List<GameScreenshot>?,
    horizontalPadding: PaddingValues,
    shimmer: State<Float>?,
    onScreenshotClicked: (index: Int) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            modifier = Modifier.padding(horizontalPadding),
            text = stringResource(Res.string.screenshots),
            style = AppTheme.typography.headlineMedium,
        )
        val listState = rememberLazyListState()
        val itemModifier = Modifier
            .width(ScreenshotWidth)
            .aspectRatio(SCREENSHOT_ASPECT_RATIO)
            .clip(ScreenshotShape)
        LazyRow(
            modifier = Modifier.horizontalScrollHint(listState, nudge = screenshots != null),
            state = listState,
            contentPadding = horizontalPadding,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            if (screenshots == null) {
                if (shimmer != null) {
                    items(count = SKELETON_SCREENSHOTS, key = { "skeleton$it" }) {
                        SkeletonBox(modifier = Modifier.animateItem().then(itemModifier), shimmer = shimmer)
                    }
                }
            } else {
                itemsIndexed(items = screenshots, key = { _, screenshot -> screenshot.url }) { index, screenshot ->
                    ProgressiveImage(
                        modifier = Modifier
                            .animateItem()
                            .click { onScreenshotClicked(index) }
                            .then(itemModifier),
                        url = screenshot.previewUrl,
                        previewUrl = null,
                        contentDescription = null,
                    )
                }
            }
        }
    }
}

/** The screenshots full screen, from [initialIndex]; swiped through sideways. */
@Composable
internal fun ScreenshotViewer(
    screenshots: List<GameScreenshot>,
    initialIndex: Int,
    onDismissRequest: () -> Unit,
) {
    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        val pagerState = rememberPagerState(initialPage = initialIndex) { screenshots.size }
        Box(
            Modifier
                .fillMaxSize()
                .background(Color.Black),
        ) {
            HorizontalPager(
                modifier = Modifier.fillMaxSize(),
                state = pagerState,
                pageSpacing = 16.dp,
                key = { screenshots[it].url },
            ) { page ->
                Box(contentAlignment = Alignment.Center) {
                    val screenshot = screenshots[page]
                    // The preview is already loaded by the row: it's shown until the full size comes.
                    ProgressiveImage(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(SCREENSHOT_ASPECT_RATIO),
                        url = screenshot.url,
                        previewUrl = screenshot.previewUrl,
                        contentDescription = null,
                        contentScale = ContentScale.Fit,
                    )
                }
            }
            Box(
                Modifier
                    .fillMaxWidth()
                    .safeDrawingPadding()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
            ) {
                GGIconButton(
                    imageVector = AppIcons.close,
                    type = GGIconButtonType.OUTLINED,
                    onClick = onDismissRequest,
                )
                if (screenshots.size > 1) {
                    Text(
                        modifier = Modifier.align(Alignment.CenterEnd),
                        text = stringResource(
                            Res.string.screenshotCounter,
                            pagerState.currentPage + 1,
                            screenshots.size,
                        ),
                        style = AppTheme.typography.labelLarge,
                        color = Color.White,
                    )
                }
            }
        }
    }
}
