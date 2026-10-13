package com.royaal.designsystem.components.image

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.ContentScale
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.compose.rememberAsyncImagePainter
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.royaal.designsystem.components.skeleton.SkeletonBox

private const val CROSSFADE_DURATION_MS = 300

/**
 * Loads [previewUrl] (small, fast) first and crossfades into [url] once it's ready.
 *
 * The preview is used as the full image's placeholder, so it's only requested while the full
 * image is actually loading - nothing extra is fetched when [url] is already cached.
 */
@Composable
fun ProgressiveImage(
    url: String,
    previewUrl: String?,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop,
    alignment: Alignment = Alignment.Center,
) {
    val context = LocalPlatformContext.current
    val previewPainter = previewUrl?.let {
        rememberAsyncImagePainter(
            model = it,
            contentScale = contentScale,
        )
    }
    val request = remember(context, url) {
        ImageRequest.Builder(context)
            .data(url)
            .crossfade(CROSSFADE_DURATION_MS)
            .build()
    }
    var isLoading by remember(url) { mutableStateOf(true) }
    // Min constraints are passed on, so the image is sized exactly as if the modifier were its own.
    Box(modifier = modifier, propagateMinConstraints = true) {
        // Composed only while loading, so loaded images don't keep a shimmer animation running.
        // The preview, drawn over it, hides it as soon as it arrives.
        if (isLoading) {
            SkeletonBox(modifier = Modifier.matchParentSize(), shape = RectangleShape)
        }
        AsyncImage(
            model = request,
            contentDescription = contentDescription,
            placeholder = previewPainter,
            error = previewPainter,
            contentScale = contentScale,
            alignment = alignment,
            onLoading = { isLoading = true },
            onSuccess = { isLoading = false },
            onError = { isLoading = false },
        )
    }
}
