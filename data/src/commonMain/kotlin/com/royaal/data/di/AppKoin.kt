package com.royaal.data.di

import com.royaal.data.network.igdb.IgdbClient
import com.royaal.data.network.igdb.IgdbConfig
import com.royaal.data.network.igdb.auth.IgdbAuth
import com.royaal.data.prefs.lastOpenedGamePlatformModule
import org.koin.core.KoinApplication
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.context.startKoin
import org.koin.mp.KoinPlatform

typealias KoinAppDeclaration = KoinApplication.() -> Unit

/**
 * Single entry point both apps call at startup with their locally-sourced IGDB/Twitch
 * credentials (never hardcoded — see `local.properties` on Android, `Secrets.swift` on iOS).
 *
 * Idempotent: a second call is a no-op, since `KoinApplication.init()` on iOS can legitimately
 * run more than once (SwiftUI previews, hot reload).
 */
fun initDataKoin(clientId: String, clientSecret: String, appDeclaration: KoinAppDeclaration = {}) {
    if (KoinPlatform.getKoinOrNull() != null) {
        println("initKoin: Koin already started, ignoring this call (clientId/secret not applied)")
        return
    }
    startKoin {
        appDeclaration()
        modules(
            igdbModule {
                IgdbConfig(
                    auth = IgdbAuth.ClientCredentials(
                        clientId = clientId,
                        clientSecret = clientSecret,
                        tokenStorage = get(),
                    ),
                )
            },
            lastOpenedGamePlatformModule(),
            databaseModule(),
        )
    }
}