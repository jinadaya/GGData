package com.royaal.data.network.igdb.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GameTimeToBeatExpandedResponse(
    @SerialName("id") val id: Long = 0,
    @SerialName("completely") val completely: Int? = null,
    @SerialName("hastily") val hastily: Int? = null,
    @SerialName("normally") val normally: Int? = null,
    @SerialName("game") val game: GameExpandedResponse? = null,
)
