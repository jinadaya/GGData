package com.royaal.data.network.igdb.internal

import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.engine.okhttp.OkHttp

internal actual fun createPlatformHttpClient(configure: HttpClientConfig<*>.() -> Unit): HttpClient =
    HttpClient(OkHttp) { configure(this) }
