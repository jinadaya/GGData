package com.royaal.main.ui

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.royaal.designsystem.components.bottomSheet.GGBottomSheetDialog
import com.royaal.designsystem.components.button.GGButton
import com.royaal.designsystem.components.button.GGButtonType
import com.royaal.designsystem.icons.AppIcons
import com.royaal.designsystem.uikit.GameCover
import com.royaal.designsystem.uikit.GameDetails
import com.royaal.entities.data.GamePlayStatus
import com.royaal.entities.ui.UiGame
import ggdata.feature.main.generated.resources.Res
import ggdata.feature.main.generated.resources.addToLibrary
import ggdata.feature.main.generated.resources.close
import ggdata.feature.main.generated.resources.statusBacklog
import ggdata.feature.main.generated.resources.statusCompleted
import ggdata.feature.main.generated.resources.statusDropped
import ggdata.feature.main.generated.resources.statusPlaying
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

/** Everything about [game]; "Add to library" closes it and opens the add-to-library sheet. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun GameDetailsDialog(
    game: UiGame,
    onAddClicked: (UiGame) -> Unit,
    onDismissRequest: () -> Unit,
) {
    // The add dialog opens once this one has slid away, not on top of it.
    var addAfterDismiss by remember { mutableStateOf(false) }

    GGBottomSheetDialog(
        onDismissRequest = { if (addAfterDismiss) onAddClicked(game) else onDismissRequest() },
        actions = { dismiss ->
            GGButton(
                text = stringResource(Res.string.close),
                onClick = dismiss,
                buttonType = GGButtonType.OUTLINED,
            )
            GGButton(
                text = stringResource(Res.string.addToLibrary),
                iconStart = AppIcons.add,
                onClick = {
                    addAfterDismiss = true
                    dismiss()
                },
            )
        },
        // Pinned: the cover stays at the top while the details scroll under it.
        header = { GameCover(game) },
    ) {
        GameDetails(game = game, showCover = false)
    }
}

internal val GamePlayStatus.title: StringResource
    get() = when (this) {
        GamePlayStatus.BACKLOG -> Res.string.statusBacklog
        GamePlayStatus.PLAYING -> Res.string.statusPlaying
        GamePlayStatus.COMPLETED -> Res.string.statusCompleted
        GamePlayStatus.DROPPED -> Res.string.statusDropped
    }
