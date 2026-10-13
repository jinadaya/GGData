package com.royaal.data.db

import android.content.Context
import androidx.room.Room

actual class DatabaseFactory(private val context: Context) {
    actual fun create(): AppDatabase {
        val dbFile = context.applicationContext.getDatabasePath(DB_FILE_NAME)
        return Room.databaseBuilder<AppDatabase>(context.applicationContext, dbFile.absolutePath)
            .buildDatabase()
    }
}
