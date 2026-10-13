package com.royaal.game.ui.common

import com.royaal.entities.data.GamePlayStatus
import ggdata.feature.game.generated.resources.Res
import ggdata.feature.game.generated.resources.statusBacklog
import ggdata.feature.game.generated.resources.statusCompleted
import ggdata.feature.game.generated.resources.statusDropped
import ggdata.feature.game.generated.resources.statusPlaying
import org.jetbrains.compose.resources.StringResource

internal val GamePlayStatus.title: StringResource
    get() = when (this) {
        GamePlayStatus.BACKLOG -> Res.string.statusBacklog
        GamePlayStatus.PLAYING -> Res.string.statusPlaying
        GamePlayStatus.COMPLETED -> Res.string.statusCompleted
        GamePlayStatus.DROPPED -> Res.string.statusDropped
    }
