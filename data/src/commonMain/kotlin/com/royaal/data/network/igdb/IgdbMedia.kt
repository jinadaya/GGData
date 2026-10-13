package com.royaal.data.network.igdb

/** Predefined IGDB image sizes (`t_{slug}`). Dimensions are the documented targets. */
enum class IgdbImageSize(val slug: String, val width: Int, val height: Int) {
    CoverSmall("cover_small", 90, 128),
    CoverBig("cover_big", 264, 374),
    ScreenshotMed("screenshot_med", 569, 320),
    ScreenshotBig("screenshot_big", 889, 500),
    ScreenshotHuge("screenshot_huge", 1280, 720),
    LogoMed("logo_med", 284, 160),
    Thumb("thumb", 90, 90),
    Micro("micro", 35, 35),
    Hd720("720p", 1280, 720),
    Hd1080("1080p", 1920, 1080),
}

enum class IgdbImageFormat(val extension: String) { Jpg("jpg"), Png("png"), Webp("webp") }

/**
 * URLs for IGDB media. Select `image_id` (e.g. `cover.image_id`) in your query and build URLs here
 * instead of using IGDB's protocol-relative `url` field.
 */
object IgdbMedia {
    const val IMAGE_BASE_URL = "https://images.igdb.com/igdb/image/upload"

    /** `https://images.igdb.com/igdb/image/upload/t_cover_big_2x/co1wyy.jpg` */
    fun imageUrl(
        imageId: String,
        size: IgdbImageSize = IgdbImageSize.CoverBig,
        retina: Boolean = false,
        format: IgdbImageFormat = IgdbImageFormat.Jpg,
    ): String {
        require(imageId.isNotBlank()) { "imageId must not be blank" }
        val sizeSlug = if (retina) "${size.slug}_2x" else size.slug
        return "$IMAGE_BASE_URL/t_$sizeSlug/$imageId.${format.extension}"
    }

    /**
     * Converts an IGDB `url` field (`//images.igdb.com/.../t_thumb/abc.jpg`) to an absolute URL,
     * optionally swapping the size.
     */
    fun normalizeImageUrl(url: String, size: IgdbImageSize? = null, retina: Boolean = false): String {
        val absolute = if (url.startsWith("//")) "https:$url" else url
        if (size == null) return absolute
        val sizeSlug = if (retina) "${size.slug}_2x" else size.slug
        return absolute.replace(Regex("/t_[a-z0-9_]+/"), "/t_$sizeSlug/")
    }

    /** `game_videos.video_id` is a YouTube id. */
    fun youtubeUrl(videoId: String): String = "https://www.youtube.com/watch?v=$videoId"

    fun youtubeThumbnailUrl(videoId: String): String = "https://img.youtube.com/vi/$videoId/hqdefault.jpg"
}
