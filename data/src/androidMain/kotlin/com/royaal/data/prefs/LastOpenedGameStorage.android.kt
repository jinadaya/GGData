package com.royaal.data.prefs

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.dsl.module

actual fun lastOpenedGamePlatformModule(): Module = module {
    single<LastOpenedGameStorage> { SharedPreferencesLastOpenedGameStorage(androidContext()) }
}

class SharedPreferencesLastOpenedGameStorage(
    context: Context,
    private val fileName: String = "last_opened_game",
) : LastOpenedGameStorage {

    private val prefs = context.applicationContext.getSharedPreferences(fileName, Context.MODE_PRIVATE)

    override suspend fun load(): Long? = withContext(Dispatchers.IO) {
        if (!prefs.contains(KEY_GAME_ID)) null else prefs.getLong(KEY_GAME_ID, 0L)
    }

    override suspend fun save(gameId: Long) {
        withContext(Dispatchers.IO) {
            prefs.edit().putLong(KEY_GAME_ID, gameId).commit()
        }
    }

    override suspend fun clear() {
        withContext(Dispatchers.IO) { prefs.edit().clear().commit() }
    }

    private companion object {
        const val KEY_GAME_ID = "game_id"
    }
}
