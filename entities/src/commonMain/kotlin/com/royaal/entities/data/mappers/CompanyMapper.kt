package com.royaal.entities.data.mappers

import com.royaal.data.network.igdb.model.CompanyExpandedResponse
import com.royaal.entities.Mapper
import com.royaal.entities.data.CompanyData

class CompanyMapper : Mapper<CompanyExpandedResponse, CompanyData> {

    override fun map(input: CompanyExpandedResponse): CompanyData = CompanyData(
        id = input.id,
        name = input.name.orEmpty(),
    )
}
