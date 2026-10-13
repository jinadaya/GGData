package com.royaal.data.db

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert

@Dao
interface CompanyDao {

    @Upsert
    suspend fun upsert(company: CompanyEntity)

    @Upsert
    suspend fun upsertAll(companies: List<CompanyEntity>)

    @Query("SELECT * FROM company WHERE id = :id")
    suspend fun getById(id: Long): CompanyEntity?
}
