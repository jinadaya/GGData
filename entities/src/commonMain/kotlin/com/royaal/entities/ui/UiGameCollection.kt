package com.royaal.entities.ui

data class UiGameCollection(
    val id: Long,
    val title: String,
    val gamesCount: Int,
    val gameIds: List<String>,
) {
    companion object
}

fun UiGameCollection.Companion.testCollection(id: Long = 123) = UiGameCollection(
    id = id,
    title = "Favourite, Favourite, Favourite",
    gamesCount = 23,
    gameIds = listOf(),
)
