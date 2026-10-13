package com.royaal.data.network.igdb.auth

import io.ktor.util.date.getTimeMillis
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

/**
 * How the client authenticates against IGDB.
 *
 * IGDB authenticates with a Twitch **app access token** (client-credentials grant)
 * sent as `Authorization: Bearer <token>` together with a `Client-ID` header.
 *
 * Security note: IGDB expects consumer apps to go through a backend. Anything
 * compiled into an APK / IPA can be extracted, so prefer [Provider] (your backend
 * hands out tokens) or [Proxy] (your backend forwards requests) in production.
 */
sealed class IgdbAuth {

    /**
     * Fetches and refreshes Twitch app tokens on-device using client id + secret.
     * Convenient for prototypes/internal builds — it ships your secret in the binary.
     *
     * @param tokenStorage persist tokens across launches (Keychain / EncryptedSharedPreferences / DataStore).
     * @param refreshMargin token is renewed this long before it actually expires.
     */
    class ClientCredentials(
        val clientId: String,
        val clientSecret: String,
        val tokenStorage: IgdbTokenStorage = InMemoryTokenStorage(),
        val refreshMargin: Duration = 10.minutes,
        val tokenUrl: String = TwitchTokenProvider.DEFAULT_TOKEN_URL,
    ) : IgdbAuth()

    /** A token you obtained elsewhere. Cannot be refreshed: a 401 surfaces as [com.royaal.data.network.igdb.IgdbException.Unauthorized]. */
    class StaticToken(val clientId: String, val accessToken: String) : IgdbAuth()

    /** Recommended for production: tokens come from your own code (e.g. fetched from your backend). */
    class Provider(val clientId: String, val tokenProvider: IgdbTokenProvider) : IgdbAuth()

    /**
     * No IGDB credentials on the device: point `IgdbConfig.baseUrl` at your proxy
     * (your server, or IGDB's AWS proxy template) which injects `Client-ID` / `Authorization`.
     * [headers] are attached to every request (e.g. your own API key).
     */
    class Proxy(val headers: Map<String, String> = emptyMap()) : IgdbAuth()
}

/** Supplies bearer tokens to the client. Implementations must be safe for concurrent calls. */
interface IgdbTokenProvider {

    /** Returns a currently valid access token (fetching one if necessary). */
    suspend fun getToken(): String

    /**
     * Called when IGDB answered 401 for [rejectedToken].
     * Return a fresh token to retry the request once, or `null` to give up.
     * If another coroutine already refreshed, simply return the newer token.
     */
    suspend fun onUnauthorized(rejectedToken: String): String?
}

/** Always returns the same token; never refreshes. */
class StaticTokenProvider(private val token: String) : IgdbTokenProvider {
    override suspend fun getToken(): String = token
    override suspend fun onUnauthorized(rejectedToken: String): String? = null
}

/** A Twitch app access token with its absolute expiry (wall clock, epoch millis). */
class IgdbAccessToken(
    val value: String,
    val expiresAtEpochMillis: Long,
    val tokenType: String = "bearer",
) {
    fun isValid(margin: Duration = Duration.ZERO, nowEpochMillis: Long = getTimeMillis()): Boolean =
        nowEpochMillis + margin.inWholeMilliseconds < expiresAtEpochMillis

    override fun toString(): String =
        "IgdbAccessToken(value=${value.take(4)}…, expiresAtEpochMillis=$expiresAtEpochMillis)"
}

/**
 * Persistence for [IgdbAccessToken]. Twitch app tokens live for roughly two months, so
 * persisting them avoids minting a new token on every app launch.
 */
interface IgdbTokenStorage {
    suspend fun load(): IgdbAccessToken?
    suspend fun save(token: IgdbAccessToken)
    suspend fun clear()
}

class InMemoryTokenStorage : IgdbTokenStorage {
    private var token: IgdbAccessToken? = null
    override suspend fun load(): IgdbAccessToken? = token
    override suspend fun save(token: IgdbAccessToken) {
        this.token = token
    }

    override suspend fun clear() {
        token = null
    }
}
