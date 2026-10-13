package com.royaal.designsystem.uikit

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.structuralEqualityPolicy
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.royaal.designsystem.components.skeleton.SkeletonBox
import com.royaal.designsystem.components.skeleton.SkeletonText
import com.royaal.designsystem.components.skeleton.rememberSkeletonShimmer
import com.royaal.designsystem.generated.resources.Res
import com.royaal.designsystem.generated.resources.collectionCount
import com.royaal.designsystem.theme.AppTheme
import com.royaal.designsystem.theme.backgroundColors
import com.royaal.designsystem.theme.brandColors
import com.royaal.designsystem.theme.textColors
import com.royaal.designsystem.util.AppConst
import com.royaal.designsystem.util.BasePreview
import com.royaal.designsystem.util.baseShadow
import com.royaal.designsystem.util.ignoreIntrinsicWidth
import com.royaal.entities.ui.UiGame
import com.royaal.entities.ui.UiGameCollection
import com.royaal.entities.ui.testCollection
import org.jetbrains.compose.resources.pluralStringResource

@Composable
fun CollectionCard(
    modifier: Modifier = Modifier,
    collection: UiGameCollection,
    preview: List<UiGame> = emptyList(),
    onClick: ((UiGameCollection) -> Unit)? = null,
) {
    val collectionPreview = derivedStateOf(structuralEqualityPolicy()) {
        preview.take(4)
    }
    Column(
        modifier = modifier
            .width(IntrinsicSize.Min)
            .baseShadow(click = { onClick?.invoke(collection) })
            .background(
                color = AppTheme.backgroundColors.background,
                shape = AppConst.cardShape,
            )
            .clip(AppConst.cardShape)
            .padding(8.dp),
    ) {
        Column(
            modifier = Modifier.clip(AppConst.innerCardShape),
            verticalArrangement = Arrangement.spacedBy(1.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(1.dp)
            ) {
                PreviewImage(game = collectionPreview.value.getOrNull(0))
                PreviewImage(game = collectionPreview.value.getOrNull(1))
            }
            Row(
                horizontalArrangement = Arrangement.spacedBy(1.dp)
            ) {
                PreviewImage(game = collectionPreview.value.getOrNull(2))
                PreviewImage(game = collectionPreview.value.getOrNull(3))
            }
        }
        Column(
            modifier = Modifier.ignoreIntrinsicWidth(),
        ) {
            Text(
                text = collection.title,
                style = AppTheme.typography.bodyLarge,
                color = AppTheme.textColors.primary,
                maxLines = 1,
                overflow = TextOverflow.Clip,
            )
            Text(
                text = pluralStringResource(
                    resource = Res.plurals.collectionCount,
                    quantity = collection.gamesCount,
                    collection.gamesCount,
                ),
                style = AppTheme.typography.bodySmall,
                color = AppTheme.textColors.secondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

/** [CollectionCard] while its collection is loading. */
@Composable
fun SkeletonCollectionCard(
    modifier: Modifier = Modifier,
    shimmer: State<Float> = rememberSkeletonShimmer(),
) {
    Column(
        modifier = modifier
            .baseShadow()
            .background(
                color = AppTheme.backgroundColors.background,
                shape = AppConst.cardShape,
            )
            .clip(AppConst.cardShape)
            .padding(8.dp),
    ) {
        Column(
            modifier = Modifier.clip(AppConst.innerCardShape),
            verticalArrangement = Arrangement.spacedBy(1.dp)
        ) {
            repeat(2) {
                Row(horizontalArrangement = Arrangement.spacedBy(1.dp)) {
                    repeat(2) {
                        SkeletonBox(
                            modifier = Modifier
                                .width(48.dp)
                                .aspectRatio(0.95f),
                            shimmer = shimmer,
                            shape = RectangleShape,
                        )
                    }
                }
            }
        }
        SkeletonText(
            style = AppTheme.typography.bodyLarge,
            modifier = Modifier.width(72.dp),
            shimmer = shimmer,
        )
        SkeletonText(
            style = AppTheme.typography.bodySmall,
            modifier = Modifier.width(48.dp),
            shimmer = shimmer,
        )
    }
}

@Composable
private fun PreviewImage(
    modifier: Modifier = Modifier,
    game: UiGame?,
) {
    game?.let { game ->
        GameImage(
            modifier = modifier
                .width(48.dp)
                .aspectRatio(0.95f),
            game = game,
            style = AppTheme.typography.labelMedium,
        )
    } ?: Box(
        modifier
            .width(48.dp)
            .aspectRatio(0.95f)
            .background(AppTheme.brandColors.primaryContainer)
    )
}

@Preview
@Composable
private fun Preview() = BasePreview {
    CollectionCard(
        collection = UiGameCollection.testCollection(),
    )
    SkeletonCollectionCard()
}