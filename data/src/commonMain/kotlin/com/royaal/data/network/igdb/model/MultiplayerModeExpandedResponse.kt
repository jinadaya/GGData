package com.royaal.data.network.igdb.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MultiplayerModeExpandedResponse(
    @SerialName("id") val id: Long = 0,
    @SerialName("checksum") val checksum: String? = null,
    @SerialName("campaigncoop") val campaigncoop: Boolean? = null,
    @SerialName("dropin") val dropin: Boolean? = null,
    @SerialName("lancoop") val lancoop: Boolean? = null,
    @SerialName("offlinecoop") val offlinecoop: Boolean? = null,
    @SerialName("offlinecoopmax") val offlinecoopmax: Int? = null,
    @SerialName("offlinemax") val offlinemax: Int? = null,
    @SerialName("onlinecoop") val onlinecoop: Boolean? = null,
    @SerialName("onlinecoopmax") val onlinecoopmax: Int? = null,
    @SerialName("onlinemax") val onlinemax: Int? = null,
    @SerialName("splitscreen") val splitscreen: Boolean? = null,
    @SerialName("splitscreenonline") val splitscreenonline: Boolean? = null,
    @SerialName("game") val game: GameExpandedResponse? = null,
)
