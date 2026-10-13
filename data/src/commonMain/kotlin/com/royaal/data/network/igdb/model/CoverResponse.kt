package com.royaal.data.network.igdb.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@Serializable
data class CoverResponse(
    @SerialName("id") val id: Long = 0,
    @SerialName("alpha_channel") val alphaChannel: Boolean? = null,
    @SerialName("animated") val animated: Boolean? = null,
    @SerialName("checksum") val checksum: String? = null,
    @SerialName("game") val game: Long? = null,
    @SerialName("game_localization") val gameLocalization: Long? = null,
    @SerialName("height") val height: Int? = null,
    @SerialName("image_id") val imageId: String? = null,
    @SerialName("url") val url: String? = null,
    @SerialName("width") val width: Int? = null,
) {
    /** Builds a direct IGDB image URL from imageId. */
    fun imageUrl(size: IgdbImageSizeResponse = IgdbImageSizeResponse.CoverBig): String? =
        imageId?.let {
            "https://images.igdb.com/igdb/image/upload/t_${size.value}/$it.jpg"
        }

    val coverUrl: String?
        get() = imageUrl()
}

enum class IgdbImageSizeResponse(val value: String) {
    CoverSmall("cover_small"),
    CoverBig("cover_big"),
    CoverBig2x("cover_big_2x"),
    ScreenshotMed("screenshot_med"),
    ScreenshotBig("screenshot_big"),
    ScreenshotHuge("screenshot_huge"),
    Thumb("thumb"),
    Micro("micro"),
    LogoMed("logo_med"),
    LogoBig("logo_big"),
}
