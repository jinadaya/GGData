package com.royaal.data.network.igdb.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PlatformResponse(
    @SerialName("id") val id: Long = 0,
    @SerialName("abbreviation") val abbreviation: String? = null,
    @SerialName("alternative_name") val alternativeName: String? = null,
    @SerialName("category") val category: Int? = null,
    @SerialName("checksum") val checksum: String? = null,
    @SerialName("created_at") val createdAt: Long? = null,
    @SerialName("generation") val generation: Int? = null,
    @SerialName("name") val name: String? = null,
    @SerialName("platform_family") val platformFamily: Long? = null,
    @SerialName("platform_logo") val platformLogo: Long? = null,
    @SerialName("platform_type") val platformType: Long? = null,
    @SerialName("slug") val slug: String? = null,
    @SerialName("summary") val summary: String? = null,
    @SerialName("updated_at") val updatedAt: Long? = null,
    @SerialName("url") val url: String? = null,
    @SerialName("versions") val versions: List<Long> = emptyList(),
    @SerialName("websites") val websites: List<Long> = emptyList(),
)