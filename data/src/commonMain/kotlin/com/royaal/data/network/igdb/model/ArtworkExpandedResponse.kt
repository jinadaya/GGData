package com.royaal.data.network.igdb.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ArtworkExpandedResponse(
    @SerialName("id") val id: Long = 0,
    @SerialName("alpha_channel") val alphaChannel: Boolean? = null,
    @SerialName("animated") val animated: Boolean? = null,
    @SerialName("artwork_type") val artworkType: Long? = null,
    @SerialName("checksum") val checksum: String? = null,
    @SerialName("game") val game: GameExpandedResponse? = null,
    @SerialName("height") val height: Int? = null,
    @SerialName("image_id") val imageId: String? = null,
    @SerialName("image_type") val imageType: Long? = null,
    @SerialName("url") val url: String? = null,
    @SerialName("width") val width: Int? = null,
)
