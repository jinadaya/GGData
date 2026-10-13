package com.royaal.data.di

import android.content.Context
import android.content.SharedPreferences
import com.royaal.data.network.igdb.auth.IgdbAccessToken
import com.royaal.data.network.igdb.auth.IgdbTokenStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.dsl.module

actual fun igdbPlatformModule(): Module = module {
    single<IgdbTokenStorage> { SharedPreferencesTokenStorage(androidContext()) }
}

/**
 * Persists the Twitch app token in a private SharedPreferences file.
 *
 * This is an app access token (not user credentials) and it expires; private app storage is a
 * reasonable home. Exclude [fileName] from Auto Backup if you don't want it restored on other devices.
 */
class SharedPreferencesTokenStorage(
    context: Context,
    val fileName: String = "igdb_token",
) : IgdbTokenStorage {

    private val prefs: SharedPreferences =
        context.applicationContext.getSharedPreferences(fileName, Context.MODE_PRIVATE)

    override suspend fun load(): IgdbAccessToken? = withContext(Dispatchers.IO) {
        val value = prefs.getString(KEY_VALUE, null) ?: return@withContext null
        IgdbAccessToken(
            value = value,
            expiresAtEpochMillis = prefs.getLong(KEY_EXPIRES_AT, 0L),
            tokenType = prefs.getString(KEY_TYPE, null) ?: "bearer",
        )
    }

    override suspend fun save(token: IgdbAccessToken) {
        withContext(Dispatchers.IO) {
            prefs.edit()
                .putString(KEY_VALUE, token.value)
                .putLong(KEY_EXPIRES_AT, token.expiresAtEpochMillis)
                .putString(KEY_TYPE, token.tokenType)
                .commit()
        }
    }

    override suspend fun clear() {
        withContext(Dispatchers.IO) { prefs.edit().clear().commit() }
    }

    private companion object {
        const val KEY_VALUE = "access_token"
        const val KEY_EXPIRES_AT = "expires_at"
        const val KEY_TYPE = "token_type"
    }
}