package com.royaal.data.network.igdb.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PlatformExpandedResponse(
    @SerialName("id") val id: Long = 0,
    @SerialName("abbreviation") val abbreviation: String? = null,
    @SerialName("alternative_name") val alternativeName: String? = null,
    @SerialName("category") val category: Int? = null,
    @SerialName("checksum") val checksum: String? = null,
    @SerialName("created_at") val createdAt: Long? = null,
    @SerialName("generation") val generation: Int? = null,
    @SerialName("name") val name: String? = null,
    @SerialName("platform_family") val platformFamily: PlatformFamilyExpandedResponse? = null,
    @SerialName("platform_logo") val platformLogo: PlatformLogoExpandedResponse? = null,
    @SerialName("platform_type") val platformType: PlatformExpandedResponse? = null,
    @SerialName("slug") val slug: String? = null,
    @SerialName("summary") val summary: String? = null,
    @SerialName("updated_at") val updatedAt: Long? = null,
    @SerialName("url") val url: String? = null,
    @SerialName("versions") val versions: List<PlatformExpandedResponse> = emptyList(),
    @SerialName("websites") val websites: List<WebsiteExpandedResponse> = emptyList(),
)
