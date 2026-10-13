package com.royaal.entities.data

data class GameData(
    val id: Long,
    val title: String,
    val coverUrl: String?,
    val coverPreviewUrl: String?,
    val description: String?,
    val releaseYear: Int?,
    val rating: Double?,
    val platforms: List<String>,
    val genres: List<String>,
    val developer: CompanyData?,
    val publisher: CompanyData?,
    val releaseDate: String?,
    val maxPlayers: Int?,
    val timeToBeat: Int?,
    val favourite: Boolean = false,
    val status: GamePlayStatus? = null,
)
