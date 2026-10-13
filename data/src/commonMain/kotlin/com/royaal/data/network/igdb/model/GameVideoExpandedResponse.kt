package com.royaal.data.network.igdb.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GameVideoExpandedResponse(
    @SerialName("id") val id: Long = 0,
    @SerialName("checksum") val checksum: String? = null,
    @SerialName("game") val game: GameExpandedResponse? = null,
    @SerialName("name") val name: String? = null,
    @SerialName("video_id") val videoId: String? = null,
)
