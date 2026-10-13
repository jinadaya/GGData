package com.royaal.data.di

import com.royaal.data.db.AppDatabase
import com.royaal.data.db.CompanyDao
import com.royaal.data.db.DatabaseFactory
import com.royaal.data.db.GameCollectionDao
import com.royaal.data.db.GameDao
import com.royaal.data.db.dao.GameCollectionCrossRefDao
import com.royaal.data.db.dao.UserGameDao
import org.koin.core.module.Module
import org.koin.dsl.module
import org.koin.dsl.onClose

/**
 * Koin wiring for the Room database.
 *
 * - [AppDatabase] is a singleton (one connection pool per process), built lazily on first DAO
 *   request and closed when Koin stops.
 * - Every DAO is exposed on its own, so repositories depend on the DAOs they need
 *   (`get<GameDao>()`) rather than on the whole database.
 * - Platform part ([databasePlatformModule]) provides the [DatabaseFactory]; Android requires
 *   `androidContext(...)` at startup.
 */
fun databaseModule(): Module = module {
    includes(databasePlatformModule())

    single<AppDatabase> { get<DatabaseFactory>().create() } onClose { it?.close() }

    single<GameDao> { get<AppDatabase>().gameDao() }
    single<GameCollectionDao> { get<AppDatabase>().gameCollectionDao() }
    single<GameCollectionCrossRefDao> { get<AppDatabase>().gameCollectionCrossRefDao() }
    single<CompanyDao> { get<AppDatabase>().companyDao() }
    single<UserGameDao> { get<AppDatabase>().userGameDao() }
}

/** Platform-specific [DatabaseFactory] (needs a Context on Android, nothing on iOS). */
expect fun databasePlatformModule(): Module
