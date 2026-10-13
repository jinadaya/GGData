package com.royaal.data.db

import androidx.room.Room
import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSUserDomainMask

actual class DatabaseFactory {
    actual fun create(): AppDatabase {
        val dbFilePath = documentDirectory() + "/" + DB_FILE_NAME
        return Room.databaseBuilder<AppDatabase>(dbFilePath).buildDatabase()
    }

    @OptIn(ExperimentalForeignApi::class)
    private fun documentDirectory(): String {
        val dir = NSFileManager.defaultManager.URLForDirectory(
            directory = NSDocumentDirectory,
            inDomain = NSUserDomainMask,
            appropriateForURL = null,
            create = false,
            error = null,
        )
        return requireNotNull(dir?.path)
    }
}
