package com.royaal.data.network.igdb.auth

import com.royaal.data.network.igdb.IgdbException
import io.ktor.client.HttpClient
import io.ktor.client.request.forms.submitForm
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpHeaders
import io.ktor.http.isSuccess
import io.ktor.http.parameters
import io.ktor.util.date.getTimeMillis
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.longOrNull
import kotlin.coroutines.cancellation.CancellationException
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

/**
 * Twitch OAuth client-credentials flow:
 * `POST https://id.twitch.tv/oauth2/token` with `client_id`, `client_secret`, `grant_type=client_credentials`.
 *
 * Tokens are cached in memory and in [storage]; concurrent callers share a single refresh.
 */
class TwitchTokenProvider(
    private val clientId: String,
    private val clientSecret: String,
    private val httpClient: HttpClient,
    private val storage: IgdbTokenStorage = InMemoryTokenStorage(),
    private val refreshMargin: Duration = 10.minutes,
    private val tokenUrl: String = DEFAULT_TOKEN_URL,
    private val json: Json = Json { ignoreUnknownKeys = true },
) : IgdbTokenProvider {

    private val mutex = Mutex()
    private var cached: IgdbAccessToken? = null

    override suspend fun getToken(): String = mutex.withLock {
        currentValidToken()?.value ?: fetchAndStore().value
    }

    override suspend fun onUnauthorized(rejectedToken: String): String = mutex.withLock {
        val current = cached
        // Someone else already replaced the rejected token while we waited for the lock.
        if (current != null && current.value != rejectedToken && current.isValid(refreshMargin)) {
            return@withLock current.value
        }
        cached = null
        storage.clear()
        fetchAndStore().value
    }

    /** Forces a new token regardless of the cached one. */
    suspend fun refresh(): IgdbAccessToken = mutex.withLock { fetchAndStore() }

    /** Current token without network access (may be expired), or null. */
    suspend fun peek(): IgdbAccessToken? = mutex.withLock { cached ?: storage.load()?.also { cached = it } }

    /**
     * `GET https://id.twitch.tv/oauth2/validate` — returns true if Twitch still accepts the cached token.
     * Twitch asks apps to validate tokens periodically; this does not fetch a new one.
     */
    suspend fun validate(): Boolean {
        val token = peek() ?: return false
        val response = guarded { httpClient.get(VALIDATE_URL) { header(HttpHeaders.Authorization, "OAuth ${token.value}") } }
        return response.status.isSuccess()
    }

    /** `POST https://id.twitch.tv/oauth2/revoke` and clears local storage. */
    suspend fun revoke() = mutex.withLock {
        val token = cached ?: storage.load()
        if (token != null) {
            guarded {
                httpClient.submitForm(
                    url = REVOKE_URL,
                    formParameters = parameters {
                        append("client_id", clientId)
                        append("token", token.value)
                    },
                )
            }
        }
        cached = null
        storage.clear()
    }

    private suspend fun currentValidToken(): IgdbAccessToken? {
        val token = cached ?: storage.load()?.also { cached = it }
        return token?.takeIf { it.isValid(refreshMargin) }
    }

    private suspend fun fetchAndStore(): IgdbAccessToken {
        val response = guarded {
            httpClient.submitForm(
                url = tokenUrl,
                formParameters = parameters {
                    append("client_id", clientId)
                    append("client_secret", clientSecret)
                    append("grant_type", "client_credentials")
                },
            )
        }
        val text = response.bodyAsText()
        if (!response.status.isSuccess()) {
            throw IgdbException.Authentication("Twitch token request failed (HTTP ${response.status.value}): $text")
        }

        val obj = runCatching { json.parseToJsonElement(text) as JsonObject }.getOrNull()
            ?: throw IgdbException.Authentication("Unexpected Twitch token response: $text")
        val accessToken = obj["access_token"]?.jsonPrimitive?.contentOrNull
            ?: throw IgdbException.Authentication("Twitch token response has no access_token")
        val expiresInSeconds = obj["expires_in"]?.jsonPrimitive?.longOrNull ?: 0L
        val tokenType = obj["token_type"]?.jsonPrimitive?.contentOrNull ?: "bearer"

        val token = IgdbAccessToken(
            value = accessToken,
            expiresAtEpochMillis = getTimeMillis() + expiresInSeconds * 1000,
            tokenType = tokenType,
        )
        cached = token
        storage.save(token)
        return token
    }

    private inline fun guarded(block: () -> HttpResponse): HttpResponse = try {
        block()
    } catch (e: CancellationException) {
        throw e
    } catch (e: IgdbException) {
        throw e
    } catch (e: Exception) {
        throw IgdbException.Authentication("Could not reach Twitch auth server: ${e.message}", e)
    }

    companion object {
        const val DEFAULT_TOKEN_URL = "https://id.twitch.tv/oauth2/token"
        const val VALIDATE_URL = "https://id.twitch.tv/oauth2/validate"
        const val REVOKE_URL = "https://id.twitch.tv/oauth2/revoke"
    }
}
