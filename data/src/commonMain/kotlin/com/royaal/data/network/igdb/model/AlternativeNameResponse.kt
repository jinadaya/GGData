package com.royaal.data.network.igdb.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AlternativeNameResponse(
    @SerialName("id") val id: Long = 0,
    @SerialName("checksum") val checksum: String? = null,
    @SerialName("comment") val comment: String? = null,
    @SerialName("game") val game: Long? = null,
    @SerialName("name") val name: String? = null,
)