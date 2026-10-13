package com.royaal.data.network.igdb.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GameLocalizationExpandedResponse(
    @SerialName("id") val id: Long = 0,
    @SerialName("checksum") val checksum: String? = null,
    @SerialName("cover") val cover: CoverExpandedResponse? = null,
    @SerialName("game") val game: GameExpandedResponse? = null,
    @SerialName("language") val language: LanguageExpandedResponse? = null,
    @SerialName("name") val name: String? = null,
)
