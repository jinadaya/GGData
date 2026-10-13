package com.royaal.entities.ui

data class UiGame(
    val id: Long,
    val title: String,
    val coverUrl: String?,
    val coverPreviewUrl: String?,
    val rating: Float,
    val devTeamTitle: String?,
    val releaseYear: Int?,
    val platforms: List<String>?,
    val genres: List<String>?,
    val description: String,
    val favourite: Boolean,
    val timeToBeat: Int?,
    val maxPlayers: Int?,
    val releaseDate: String?,
) {
    companion object
}

val UiGame.Companion.testGame: UiGame
    get() = UiGame(
        id = 1L,
        title = "Minecraft",
        coverUrl = "",
        coverPreviewUrl = null,
        rating = 10.0f,
        devTeamTitle = "Mojang",
        releaseYear = 2011,
        platforms = listOf("PC", "PSP", "Nintendo Switch"),
        genres = listOf("Action"),
        description = "Build unique worlds in this " +
                "spectacular, wonderful game. " +
                "Intriguing adventures, a lot of " +
                "enemies and much more friends! " +
                "Join us now!",
        favourite = true,
        timeToBeat = null,
        maxPlayers = null,
        releaseDate = null,
    )