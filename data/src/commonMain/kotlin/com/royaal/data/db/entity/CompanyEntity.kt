package com.royaal.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

/** A locally cached snapshot of an IGDB company (referenced as a game's developer/publisher). */
@Entity(tableName = "company")
data class CompanyEntity(
    @PrimaryKey val id: Long,
    val name: String,
)
