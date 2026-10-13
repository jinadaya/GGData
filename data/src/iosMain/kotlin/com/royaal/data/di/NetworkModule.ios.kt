package com.royaal.data.di

import com.royaal.data.network.igdb.auth.IgdbAccessToken
import com.royaal.data.network.igdb.auth.IgdbTokenStorage
import org.koin.core.module.Module
import org.koin.dsl.module
import platform.Foundation.NSUserDefaults

actual fun igdbPlatformModule(): Module = module {
    single<IgdbTokenStorage> { UserDefaultsTokenStorage() }
}

/**
 * Persists the Twitch app token in NSUserDefaults.
 *
 * This is an app access token (not user credentials) and it expires. If your security policy
 * requires it, implement [IgdbTokenStorage] on top of the Keychain and register it instead.
 */
class UserDefaultsTokenStorage(
    private val defaults: NSUserDefaults = NSUserDefaults.standardUserDefaults,
    private val keyPrefix: String = "igdb.token",
) : IgdbTokenStorage {

    private val keyValue get() = "$keyPrefix.value"
    private val keyExpiresAt get() = "$keyPrefix.expiresAt"
    private val keyType get() = "$keyPrefix.type"

    override suspend fun load(): IgdbAccessToken? {
        val value = defaults.stringForKey(keyValue) ?: return null
        return IgdbAccessToken(
            value = value,
            // Stored as a Double: epoch millis fit exactly (< 2^53).
            expiresAtEpochMillis = defaults.doubleForKey(keyExpiresAt).toLong(),
            tokenType = defaults.stringForKey(keyType) ?: "bearer",
        )
    }

    override suspend fun save(token: IgdbAccessToken) {
        defaults.setObject(token.value, forKey = keyValue)
        defaults.setDouble(token.expiresAtEpochMillis.toDouble(), forKey = keyExpiresAt)
        defaults.setObject(token.tokenType, forKey = keyType)
    }

    override suspend fun clear() {
        defaults.removeObjectForKey(keyValue)
        defaults.removeObjectForKey(keyExpiresAt)
        defaults.removeObjectForKey(keyType)
    }
}