package com.royaal.data.network.igdb

import com.royaal.data.network.igdb.auth.IgdbAuth
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

/**
 * @param auth how requests are authenticated, see [IgdbAuth].
 * @param baseUrl IGDB v4 root, or your proxy's URL.
 * @param rateLimit client-side throttling; `null` disables it (e.g. when a proxy already throttles).
 * @param maxRetries retries for 429 / 5xx / network failures on read requests (exponential backoff).
 * @param json used for parsing responses and decoding into your own `@Serializable` classes.
 * @param logger receives request/response lines. Authorization values are never logged.
 */
class IgdbConfig(
    val auth: IgdbAuth,
    val baseUrl: String = DEFAULT_BASE_URL,
    val requestTimeout: Duration = 30.seconds,
    val connectTimeout: Duration = 15.seconds,
    val rateLimit: IgdbRateLimit? = IgdbRateLimit.Default,
    val maxRetries: Int = 3,
    val json: Json = DefaultJson,
    val logger: IgdbLogger? = null,
    val logBodies: Boolean = false,
    val userAgent: String? = null,
) {
    init {
        require(maxRetries >= 0) { "maxRetries must be >= 0" }
        require(baseUrl.startsWith("http://") || baseUrl.startsWith("https://")) { "baseUrl must be an absolute http(s) URL" }
    }

    companion object {
        const val DEFAULT_BASE_URL = "https://api.igdb.com/v4/"

        @OptIn(ExperimentalSerializationApi::class)
        val DefaultJson: Json = Json {
            ignoreUnknownKeys = true
            isLenient = true
            coerceInputValues = true
            explicitNulls = false
        }
    }
}

/** IGDB allows 4 requests per second and up to 8 open requests. */
class IgdbRateLimit(
    val requestsPerSecond: Int = 4,
    val maxConcurrentRequests: Int = 8,
) {
    companion object {
        val Default = IgdbRateLimit()
    }
}

fun interface IgdbLogger {
    fun log(message: String)
}
