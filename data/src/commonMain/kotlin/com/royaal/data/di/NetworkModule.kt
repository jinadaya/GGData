package com.royaal.data.di

import com.royaal.data.network.igdb.IgdbClient
import com.royaal.data.network.igdb.IgdbConfig
import io.ktor.client.HttpClient
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.core.scope.Scope
import org.koin.dsl.module
import org.koin.dsl.onClose

/**
 * Optional qualifier: register an `HttpClient` under it to make [IgdbClient] use your client
 * (shared app client, `MockEngine` in tests). Without it, IgdbClient creates and owns its own.
 *
 * The client you provide must have `expectSuccess = false`, and you are responsible for closing it.
 */
val IgdbHttpClient = named("igdb.httpClient")

/**
 * Koin wiring for the IGDB client.
 *
 * - [IgdbConfig] is built lazily by [config], inside a Koin [Scope], so it can `get()`
 *   other dependencies (token storage, your backend token provider, build flags…).
 * - [IgdbClient] is a singleton (it owns the rate limiter and the token cache) and is closed
 *   when Koin stops.
 * - Platform defaults ([igdbPlatformModule]): an `IgdbTokenStorage` backed by SharedPreferences
 *   on Android and NSUserDefaults on iOS. Override it by declaring your own `IgdbTokenStorage`
 *   in a module loaded after this one.
 *
 * Endpoint facades (`client.games`, …) are cheap wrappers, so inject [IgdbClient] and use them
 * directly rather than registering each endpoint.
 *
 * ```
 * modules(
 *     igdbModule {
 *         IgdbConfig(auth = IgdbAuth.ClientCredentials(id, secret, tokenStorage = get()))
 *     },
 * )
 * ```
 */
fun igdbModule(config: Scope.() -> IgdbConfig): Module = module {
    includes(igdbPlatformModule())

    single<IgdbConfig> { config() }

    single<IgdbClient> {
        IgdbClient(
            config = get(),
            httpClient = getOrNull<HttpClient>(IgdbHttpClient),
        )
    } onClose { it?.close() }
}

/** Platform-specific defaults (token storage). Android requires `androidContext(...)` at startup. */
expect fun igdbPlatformModule(): Module