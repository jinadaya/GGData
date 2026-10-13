package com.royaal.data.network.igdb.internal

import com.royaal.data.network.igdb.IgdbConfig
import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.UserAgent

/** Creates an [HttpClient] backed by the platform engine (OkHttp on Android, Darwin on iOS). */
internal expect fun createPlatformHttpClient(configure: HttpClientConfig<*>.() -> Unit): HttpClient

internal fun createDefaultHttpClient(config: IgdbConfig): HttpClient = createPlatformHttpClient {
    expectSuccess = false
    install(HttpTimeout) {
        requestTimeoutMillis = config.requestTimeout.inWholeMilliseconds
        connectTimeoutMillis = config.connectTimeout.inWholeMilliseconds
        socketTimeoutMillis = config.requestTimeout.inWholeMilliseconds
    }
    config.userAgent?.let { ua -> install(UserAgent) { agent = ua } }
}
