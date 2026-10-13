package com.royaal.data.db

import androidx.room.RoomDatabase
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.royaal.data.db.migrations.MIGRATION_4_5
import com.royaal.data.db.migrations.MIGRATION_5_6
import com.royaal.data.db.migrations.MIGRATION_6_7
import com.royaal.data.db.migrations.MIGRATION_7_8
import com.royaal.data.db.migrations.MIGRATION_8_9
import kotlinx.coroutines.Dispatchers

internal const val DB_FILE_NAME = "app.db"

expect class DatabaseFactory {
    fun create(): AppDatabase
}

internal fun RoomDatabase.Builder<AppDatabase>.buildDatabase(): AppDatabase =
    setDriver(BundledSQLiteDriver())
        .setQueryCoroutineContext(Dispatchers.Default)
        .addMigrations(MIGRATION_4_5, MIGRATION_5_6, MIGRATION_6_7, MIGRATION_7_8, MIGRATION_8_9)
        .build()
