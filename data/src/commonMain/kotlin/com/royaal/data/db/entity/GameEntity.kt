package com.royaal.data.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.royaal.data.db.search.GameSearchText

/**
 * A locally cached snapshot of an IGDB game, independent of any collection. [id] is the IGDB
 * game id, so re-fetching the same game upserts rather than duplicates. Holds IGDB data only;
 * what the user sets on a game lives in [com.royaal.data.db.entity.UserGameEntity].
 *
 * @param firstReleaseDate epoch seconds, as returned by IGDB's `first_release_date`.
 * @param rating IGDB rating, 0..100.
 * @param developerId [CompanyEntity.id] of the involved company flagged as developer, if cached.
 * @param publisherId [CompanyEntity.id] of the involved company flagged as publisher, if cached.
 * @param cachedAt epoch millis this row was last written, for cache invalidation.
 * @param alternativeNames IGDB `alternative_names.name`: abbreviations, localized titles
 *   (`GTA 5`, `Ведьмак 3`, `Halo CE`). `null` if they weren't fetched.
 * @param searchText [name] and [alternativeNames] normalized by [GameSearchText.index], indexed
 *   by [com.royaal.data.db.entity.GameFtsEntity]. Derived - leave the default; note that `copy`
 *   keeps the old value, so build a new entity when changing the names.
 */
@Entity(
    tableName = "game",
    foreignKeys = [
        ForeignKey(
            entity = CompanyEntity::class,
            parentColumns = ["id"],
            childColumns = ["developerId"],
            onDelete = ForeignKey.SET_NULL,
        ),
        ForeignKey(
            entity = CompanyEntity::class,
            parentColumns = ["id"],
            childColumns = ["publisherId"],
            onDelete = ForeignKey.SET_NULL,
        ),
    ],
    indices = [Index("developerId"), Index("publisherId")],
)
data class GameEntity(
    @PrimaryKey val id: Long,
    val name: String,
    val coverImageId: String? = null,
    val firstReleaseDate: Long? = null,
    val rating: Double? = null,
    val platforms: List<String>? = null,
    val genres: List<String>? = null,
    val summary: String? = null,
    val developerId: Long? = null,
    val publisherId: Long? = null,
    val cachedAt: Long,
    val timeToBeat: Int?,
    val maxPlayers: Int?,
    val gameReleaseDate: String?,
    val alternativeNames: List<String>? = null,
    @ColumnInfo(defaultValue = "")
    val searchText: String = GameSearchText.index(name, alternativeNames.orEmpty()),
)

/**
 * This fresh snapshot with the fields it lacks taken from [cached], so a lightweight query (e.g.
 * search results) never erases what a detailed one stored.
 */
internal fun GameEntity.mergedWith(cached: GameEntity): GameEntity = GameEntity(
    id = id,
    name = name,
    coverImageId = coverImageId ?: cached.coverImageId,
    firstReleaseDate = firstReleaseDate ?: cached.firstReleaseDate,
    rating = rating ?: cached.rating,
    platforms = platforms ?: cached.platforms,
    genres = genres ?: cached.genres,
    summary = summary ?: cached.summary,
    developerId = developerId ?: cached.developerId,
    publisherId = publisherId ?: cached.publisherId,
    cachedAt = cachedAt,
    timeToBeat = timeToBeat ?: cached.timeToBeat,
    maxPlayers = maxPlayers ?: cached.maxPlayers,
    gameReleaseDate = gameReleaseDate ?: cached.gameReleaseDate,
    alternativeNames = alternativeNames ?: cached.alternativeNames,
)