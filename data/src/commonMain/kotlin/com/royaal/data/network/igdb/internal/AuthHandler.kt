package com.royaal.data.network.igdb.internal

import com.royaal.data.network.igdb.auth.IgdbAuth
import com.royaal.data.network.igdb.auth.IgdbTokenProvider
import com.royaal.data.network.igdb.auth.StaticTokenProvider
import com.royaal.data.network.igdb.auth.TwitchTokenProvider
import io.ktor.client.HttpClient
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.header
import io.ktor.http.HttpHeaders
import kotlinx.serialization.json.Json

/** Resolves an [IgdbAuth] into headers + refresh behaviour. */
internal class AuthHandler(
    private val clientId: String?,
    val tokenProvider: IgdbTokenProvider?,
    private val extraHeaders: Map<String, String>,
) {
    suspend fun currentToken(): String? = tokenProvider?.getToken()

    suspend fun onUnauthorized(rejectedToken: String?): String? {
        val provider = tokenProvider ?: return null
        return rejectedToken?.let { provider.onUnauthorized(it) }
    }

    fun apply(builder: HttpRequestBuilder, token: String?) {
        clientId?.let { builder.header(CLIENT_ID_HEADER, it) }
        token?.let { builder.header(HttpHeaders.Authorization, "Bearer $it") }
        extraHeaders.forEach { (name, value) -> builder.header(name, value) }
    }

    companion object {
        const val CLIENT_ID_HEADER = "Client-ID"

        fun create(auth: IgdbAuth, httpClient: HttpClient, json: Json): AuthHandler = when (auth) {
            is IgdbAuth.ClientCredentials -> AuthHandler(
                clientId = auth.clientId,
                tokenProvider = TwitchTokenProvider(
                    clientId = auth.clientId,
                    clientSecret = auth.clientSecret,
                    httpClient = httpClient,
                    storage = auth.tokenStorage,
                    refreshMargin = auth.refreshMargin,
                    tokenUrl = auth.tokenUrl,
                    json = json,
                ),
                extraHeaders = emptyMap(),
            )

            is IgdbAuth.StaticToken -> AuthHandler(auth.clientId, StaticTokenProvider(auth.accessToken), emptyMap())
            is IgdbAuth.Provider -> AuthHandler(auth.clientId, auth.tokenProvider, emptyMap())
            is IgdbAuth.Proxy -> AuthHandler(clientId = null, tokenProvider = null, extraHeaders = auth.headers)
        }
    }
}
