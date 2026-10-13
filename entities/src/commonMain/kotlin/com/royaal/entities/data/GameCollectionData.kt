package com.royaal.entities.data

data class GameCollectionData(
    val id: Long,
    val title: String,
    val gamesCount: Int,
    val gameIds: List<Long>,
)
