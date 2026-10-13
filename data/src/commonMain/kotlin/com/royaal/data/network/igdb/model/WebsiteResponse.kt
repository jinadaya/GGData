package com.royaal.data.network.igdb.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class WebsiteResponse(
    @SerialName("id") val id: Long = 0,
    @SerialName("category") val category: Long? = null,
    @SerialName("checksum") val checksum: String? = null,
    @SerialName("game") val game: Long? = null,
    @SerialName("trusted") val trusted: Boolean? = null,
    @SerialName("type") val type: Long? = null,
    @SerialName("url") val url: String? = null,
)