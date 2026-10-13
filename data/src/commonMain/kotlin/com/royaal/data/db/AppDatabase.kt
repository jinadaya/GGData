package com.royaal.data.db

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor
import androidx.room.TypeConverters
import com.royaal.data.db.converters.StringListConverter
import com.royaal.data.db.dao.GameCollectionCrossRefDao
import com.royaal.data.db.dao.UserGameDao
import com.royaal.data.db.entity.GameCollectionCrossRef
import com.royaal.data.db.entity.GameFtsEntity
import com.royaal.data.db.entity.UserGameEntity

@Database(
    entities = [
        GameEntity::class,
        GameCollectionEntity::class,
        GameCollectionCrossRef::class,
        CompanyEntity::class,
        UserGameEntity::class,
        GameFtsEntity::class,
    ],
    version = 7,
    exportSchema = true,
)
@ConstructedBy(AppDatabaseConstructor::class)
@TypeConverters(StringListConverter::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun gameDao(): GameDao
    abstract fun gameCollectionDao(): GameCollectionDao
    abstract fun gameCollectionCrossRefDao(): GameCollectionCrossRefDao
    abstract fun companyDao(): CompanyDao
    abstract fun userGameDao(): UserGameDao
}

/** The Room compiler generates the `actual` implementation for each target. */
@Suppress("KotlinNoActualForExpect")
expect object AppDatabaseConstructor : RoomDatabaseConstructor<AppDatabase> {
    override fun initialize(): AppDatabase
}
