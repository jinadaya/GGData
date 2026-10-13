package com.royaal.data.network.igdb.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LanguageSupportResponse(
    @SerialName("id") val id: Long = 0,
    @SerialName("checksum") val checksum: String? = null,
    @SerialName("language") val language: Long? = null,
    @SerialName("language_support_type") val languageSupportType: Long? = null,
    @SerialName("game") val game: Long? = null,
)