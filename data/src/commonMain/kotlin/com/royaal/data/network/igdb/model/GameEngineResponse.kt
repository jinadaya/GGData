package com.royaal.data.network.igdb.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GameEngineResponse(
    @SerialName("id") val id: Long = 0,
    @SerialName("checksum") val checksum: String? = null,
    @SerialName("companies") val companies: List<Long> = emptyList(),
    @SerialName("created_at") val createdAt: Long? = null,
    @SerialName("description") val description: String? = null,
    @SerialName("logo") val logo: Long? = null,
    @SerialName("name") val name: String? = null,
    @SerialName("platforms") val platforms: List<Long> = emptyList(),
    @SerialName("slug") val slug: String? = null,
    @SerialName("updated_at") val updatedAt: Long? = null,
    @SerialName("url") val url: String? = null,
)
