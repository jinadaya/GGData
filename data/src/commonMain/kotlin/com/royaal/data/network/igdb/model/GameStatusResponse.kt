package com.royaal.data.network.igdb.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GameStatusResponse(
    @SerialName("id") val id: Long = 0,
    @SerialName("checksum") val checksum: String? = null,
    @SerialName("created_at") val createdAt: Long? = null,
    @SerialName("status") val status: String? = null,
    @SerialName("updated_at") val updatedAt: Long? = null,
)