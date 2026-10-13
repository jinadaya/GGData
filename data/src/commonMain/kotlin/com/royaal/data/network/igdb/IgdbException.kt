package com.royaal.data.network.igdb

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive

/**
 * Everything the client throws (besides `CancellationException` and `IllegalArgumentException`
 * from invalid builder input) is an [IgdbException].
 */
sealed class IgdbException(message: String, cause: Throwable? = null) : Exception(message, cause) {

    /** Non-success HTTP status other than 401/429. IGDB puts details in [body]. */
    class Api(val status: Int, val body: String) :
        IgdbException("IGDB request failed with HTTP $status: ${summarizeIgdbError(body)}")

    /** 401 that could not be fixed by refreshing the token. */
    class Unauthorized(val body: String) :
        IgdbException("IGDB rejected the credentials (401): ${summarizeIgdbError(body)}")

    /** 429 after all retries were used. */
    class RateLimited(val body: String) :
        IgdbException("IGDB rate limit exceeded (429) after retries")

    /** Obtaining a Twitch token failed. */
    class Authentication(message: String, cause: Throwable? = null) : IgdbException(message, cause)

    /** Transport failure (DNS, TLS, timeout, connection reset…). */
    class Network(cause: Throwable) : IgdbException("Network error: ${cause.message ?: cause::class.simpleName}", cause)

    /** Response could not be parsed / decoded into the requested type. [body] is the raw response. */
    class Decoding(val body: String, cause: Throwable) :
        IgdbException("Could not decode IGDB response: ${cause.message}", cause)
}

private val errorJson = Json { ignoreUnknownKeys = true; isLenient = true }

/** IGDB errors look like `[{"title":"Syntax Error","status":400,"cause":"..."}]`. */
internal fun summarizeIgdbError(body: String): String {
    val parsed = runCatching { errorJson.parseToJsonElement(body) }.getOrNull()
    val first = when (parsed) {
        is JsonArray -> parsed.firstOrNull() as? JsonObject
        is JsonObject -> parsed
        else -> null
    } ?: return body.take(500)
    val title = first["title"]?.jsonPrimitive?.contentOrNull
    val cause = first["cause"]?.jsonPrimitive?.contentOrNull ?: first["message"]?.jsonPrimitive?.contentOrNull
    return listOfNotNull(title, cause).joinToString(" — ").ifEmpty { body.take(500) }
}
