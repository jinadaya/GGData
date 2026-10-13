package com.royaal.data.network.igdb.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AgeRatingResponse(
    @SerialName("id") val id: Long = 0,
    @SerialName("category") val category: Long? = null,
    @SerialName("checksum") val checksum: String? = null,
    @SerialName("content_descriptions") val contentDescriptions: List<Long> = emptyList(),
    @SerialName("rating") val rating: Int? = null,
    @SerialName("rating_cover_url") val ratingCoverUrl: String? = null,
    @SerialName("synopsis") val synopsis: String? = null,
)