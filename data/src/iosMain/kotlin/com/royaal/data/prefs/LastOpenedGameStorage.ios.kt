package com.royaal.data.prefs

import org.koin.core.module.Module
import org.koin.dsl.module
import platform.Foundation.NSUserDefaults

actual fun lastOpenedGamePlatformModule(): Module = module {
    single<LastOpenedGameStorage> { UserDefaultsLastOpenedGameStorage() }
}

class UserDefaultsLastOpenedGameStorage(
    private val defaults: NSUserDefaults = NSUserDefaults.standardUserDefaults,
    private val key: String = "lastOpenedGame.id",
) : LastOpenedGameStorage {

    override suspend fun load(): Long? {
        if (defaults.objectForKey(key) == null) return null
        return defaults.doubleForKey(key).toLong()
    }

    override suspend fun save(gameId: Long) {
        defaults.setDouble(gameId.toDouble(), forKey = key)
    }

    override suspend fun clear() {
        defaults.removeObjectForKey(key)
    }
}
