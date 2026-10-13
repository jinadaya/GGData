package com.royaal.data.di

import com.royaal.data.db.DatabaseFactory
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.dsl.module

actual fun databasePlatformModule(): Module = module {
    single<DatabaseFactory> { DatabaseFactory(androidContext()) }
}
