package com.royaal.journal.data

import com.royaal.data.db.GameWithCompanies
import com.royaal.data.db.dao.UserGameDao
import com.royaal.data.db.entity.PlayStatus
import com.royaal.entities.Mapper
import com.royaal.entities.data.GameData
import com.royaal.entities.data.GamePlayStatus
import com.royaal.entities.data.mappers.GameEntityMapper
import com.royaal.entities.data.mappers.PlayStatusMapper
import com.royaal.journal.domain.JournalGame
import com.royaal.journal.domain.JournalRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

internal class JournalRepositoryImpl(
    private val userGameDao: UserGameDao,
    private val gameEntityMapper: Mapper<GameWithCompanies, GameData> = GameEntityMapper(),
    private val playStatusMapper: Mapper<PlayStatus, GamePlayStatus> = PlayStatusMapper(),
) : JournalRepository {

    override fun observeGames(): Flow<List<JournalGame>> =
        userGameDao.observeGamesWithAnyStatus().map { games ->
            games.mapNotNull { game ->
                val userGame = game.userGame ?: return@mapNotNull null
                val status = userGame.status ?: return@mapNotNull null
                JournalGame(
                    game = gameEntityMapper.map(game),
                    status = playStatusMapper.map(status),
                    hoursPlayed = userGame.hoursPlayed,
                    rating = userGame.rating,
                    note = userGame.note,
                    statusChangedAt = userGame.statusChangedAt,
                    startedAt = userGame.startedAt,
                    completedAt = userGame.completedAt,
                )
            }
        }
}
