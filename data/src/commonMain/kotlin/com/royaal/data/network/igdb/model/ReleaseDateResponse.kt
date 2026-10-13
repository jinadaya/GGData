package com.royaal.data.network.igdb.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ReleaseDateResponse(
    @SerialName("id") val id: Long = 0,
    @SerialName("category") val category: Int? = null,
    @SerialName("checksum") val checksum: String? = null,
    @SerialName("created_at") val createdAt: Long? = null,
    @SerialName("date") val date: Long? = null,
    @SerialName("date_format") val dateFormat: Long? = null,
    @SerialName("game") val game: Long? = null,
    @SerialName("human") val human: String? = null,
    @SerialName("m") val m: Int? = null,
    @SerialName("platform") val platform: Long? = null,
    @SerialName("region") val region: Int? = null,
    @SerialName("release_region") val releaseRegion: Long? = null,
    @SerialName("status") val status: Long? = null,
    @SerialName("updated_at") val updatedAt: Long? = null,
    @SerialName("y") val y: Int? = null,
)