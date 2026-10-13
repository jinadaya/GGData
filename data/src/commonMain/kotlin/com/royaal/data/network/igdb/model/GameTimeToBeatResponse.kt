package com.royaal.data.network.igdb.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GameTimeToBeatResponse(
    @SerialName("id") val id: Long = 0,
    @SerialName("completely") val completely: Int? = null,
    @SerialName("game_id") val gameId: Long? = null,
    @SerialName("hastily") val hastily: Int? = null,
    @SerialName("normally") val normally: Int? = null,
)