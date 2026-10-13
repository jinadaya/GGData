package com.royaal.entities.data.mappers

import com.royaal.data.network.igdb.model.GenreExpandedResponse
import com.royaal.entities.Mapper
import com.royaal.entities.data.GenreData

class GenreMapper : Mapper<GenreExpandedResponse, GenreData> {

    override fun map(input: GenreExpandedResponse): GenreData = GenreData(
        id = input.id,
        name = input.name.orEmpty(),
    )
}
