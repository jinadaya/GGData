package com.royaal.data

import com.royaal.data.network.createHttpClient
import io.ktor.client.HttpClient
import org.koin.dsl.module

val dataModule = module {
    single<HttpClient> { createHttpClient() }
}
