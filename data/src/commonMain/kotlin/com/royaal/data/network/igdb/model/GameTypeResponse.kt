package com.royaal.data.network.igdb.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GameTypeResponse(
    @SerialName("id") val id: Long = 0,
    @SerialName("checksum") val checksum: String? = null,
    @SerialName("created_at") val createdAt: Long? = null,
    @SerialName("type") val type: String? = null,
    @SerialName("updated_at") val updatedAt: Long? = null,
)