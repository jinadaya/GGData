package com.royaal.entities.data.mappers

import com.royaal.data.db.CompanyEntity
import com.royaal.entities.Mapper
import com.royaal.entities.data.CompanyData

class CompanyEntityMapper : Mapper<CompanyEntity, CompanyData> {

    override fun map(input: CompanyEntity): CompanyData = CompanyData(
        id = input.id,
        name = input.name,
    )
}
