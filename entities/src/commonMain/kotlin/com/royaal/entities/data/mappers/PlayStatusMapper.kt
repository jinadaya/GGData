package com.royaal.entities.data.mappers

import com.royaal.data.db.entity.PlayStatus
import com.royaal.entities.Mapper
import com.royaal.entities.data.GamePlayStatus

class PlayStatusMapper : Mapper<PlayStatus, GamePlayStatus> {

    override fun map(input: PlayStatus): GamePlayStatus = when (input) {
        PlayStatus.BACKLOG -> GamePlayStatus.BACKLOG
        PlayStatus.PLAYING -> GamePlayStatus.PLAYING
        PlayStatus.COMPLETED -> GamePlayStatus.COMPLETED
        PlayStatus.DROPPED -> GamePlayStatus.DROPPED
    }
}
