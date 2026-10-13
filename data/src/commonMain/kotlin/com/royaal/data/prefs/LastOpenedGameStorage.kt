package com.royaal.data.prefs

import org.koin.core.module.Module

/**
 * Persists just the id of the last game the user opened, across launches. A single mutable
 * pointer, not a collection - deliberately kept out of Room (see `GameDao.getById` to look it
 * back up once loaded).
 */
interface LastOpenedGameStorage {
    suspend fun load(): Long?
    suspend fun save(gameId: Long)
    suspend fun clear()
}

/** Platform defaults (SharedPreferences on Android, NSUserDefaults on iOS). Android requires `androidContext(...)` at startup. */
expect fun lastOpenedGamePlatformModule(): Module
