package com.royaal.entities.ui.mappers

import com.royaal.entities.Mapper
import com.royaal.entities.data.GenreData
import com.royaal.entities.ui.UiGenre

class GenreUiMapper : Mapper<GenreData, UiGenre> {

    override fun map(input: GenreData): UiGenre = UiGenre(
        id = input.id,
        name = input.name,
    )
}
