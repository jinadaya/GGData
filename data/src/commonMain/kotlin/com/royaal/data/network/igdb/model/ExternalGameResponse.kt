package com.royaal.data.network.igdb.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ExternalGameResponse(
    @SerialName("id") val id: Long = 0,
    @SerialName("category") val category: Long? = null,
    @SerialName("checksum") val checksum: String? = null,
    @SerialName("countries") val countries: List<Long> = emptyList(),
    @SerialName("game") val game: Long? = null,
    @SerialName("media") val media: Int? = null,
    @SerialName("name") val name: String? = null,
    @SerialName("platform") val platform: Long? = null,
    @SerialName("uid") val uid: String? = null,
    @SerialName("url") val url: String? = null,
)