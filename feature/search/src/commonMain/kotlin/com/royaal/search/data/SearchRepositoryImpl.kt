package com.royaal.search.data

import com.royaal.arch.mvi.log.Logger
import com.royaal.data.db.GameDao
import com.royaal.data.db.GameWithCompanies
import com.royaal.data.db.search.GameSearchText
import com.royaal.data.network.igdb.IgdbClient
import com.royaal.data.network.igdb.model.GameExpandedResponse
import com.royaal.data.network.igdb.typed.GameFields
import com.royaal.data.network.igdb.typed.IgdbField
import com.royaal.data.network.igdb.typed.typed
import com.royaal.entities.Mapper
import com.royaal.entities.data.GameData
import com.royaal.entities.data.mappers.CachedGame
import com.royaal.entities.data.mappers.GameCacheMapper
import com.royaal.entities.data.mappers.GameEntityMapper
import com.royaal.search.domain.GameSearchResult
import com.royaal.search.domain.RemoteSearchStatus
import com.royaal.search.domain.SearchRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

/**
 * Hybrid search: the cache is the single source of truth, IGDB only fills it.
 *
 * Locally a query matches by word prefixes through the FTS4 index (see [GameSearchText] for the
 * normalization rules), and by substrings when that finds few games. Games IGDB returns are
 * cached and shown too, even those it found by its own rules (e.g. typos) that the local search
 * doesn't match.
 */
internal class SearchRepositoryImpl(
    private val gameDao: GameDao,
    private val client: IgdbClient,
    private val gameEntityMapper: Mapper<GameWithCompanies, GameData> = GameEntityMapper(),
    private val gameCacheMapper: Mapper<GameExpandedResponse, CachedGame> = GameCacheMapper(),
) : SearchRepository {

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun search(query: String): Flow<GameSearchResult> {
        val words = GameSearchText.words(query)
        if (words.isEmpty()) return flowOf(GameSearchResult(emptyList(), RemoteSearchStatus.DONE))

        return channelFlow {
            val remote = MutableStateFlow(RemoteSearch(RemoteSearchStatus.LOADING))
            launch { remote.value = searchRemote(query, words) }

            remote
                .flatMapLatest { remoteSearch ->
                    observeCached(words, remoteSearch.gameIds).map { games ->
                        GameSearchResult(games.map(gameEntityMapper::map), remoteSearch.status)
                    }
                }
                .collect(::send)
        }
    }

    /**
     * Cached games matching [words] by word prefixes, plus by substrings if those are fewer than
     * [FEW_RESULTS]. Then games among [remoteIds] that didn't match, in IGDB's order.
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    private fun observeCached(words: List<String>, remoteIds: List<Long>): Flow<List<GameWithCompanies>> {
        val local = gameDao
            .observeMatchingWordPrefixes(
                match = GameSearchText.prefixMatch(words),
                titlePrefix = words.joinToString(" ") + "%",
                limit = LOCAL_LIMIT,
            )
            .flatMapLatest { byPrefix ->
                if (byPrefix.size >= FEW_RESULTS) {
                    flowOf(byPrefix)
                } else {
                    gameDao.observeContainingAll(words, LOCAL_LIMIT).map { bySubstring -> byPrefix + bySubstring }
                }
            }
        if (remoteIds.isEmpty()) return local.map { games -> games.distinctBy { it.game.id } }

        val remoteOrder = remoteIds.withIndex().associate { (index, id) -> id to index }
        val remote = gameDao.observeWithCompanies(remoteIds).map { games ->
            games.sortedBy { remoteOrder[it.game.id] }
        }
        return combine(local, remote) { localGames, remoteGames ->
            (localGames + remoteGames).distinctBy { it.game.id }
        }
    }

    /** Searches IGDB and caches what it finds. Never throws, except for cancellation. */
    private suspend fun searchRemote(query: String, words: List<String>): RemoteSearch = try {
        val found = coroutineScope {
            val byName = listOfNotNull(query.trim(), GameSearchText.withSwappedNumerals(words)).map { term ->
                async {
                    client.typed.expandedGames.search(term) {
                        fields(*SEARCH_FIELDS)
                        limit(REMOTE_LIMIT)
                    }
                }
            }
            val byAlternativeName = async { searchAlternativeNames(query) }
            (byName + byAlternativeName).awaitAll().flatten().distinctBy { it.id }
        }
        val cached = found.map(gameCacheMapper::map)
        gameDao.cacheGames(
            games = cached.map { it.game },
            companies = cached.flatMap { it.companies }.distinctBy { it.id },
        )
        RemoteSearch(RemoteSearchStatus.DONE, found.map { it.id })
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        Logger.warn(TAG, "IGDB search for \"$query\" failed: $e")
        RemoteSearch(RemoteSearchStatus.FAILED)
    }

    /**
     * Games with an alternative name containing every word of [query] (`GTA 5`, `Ведьмак 3`,
     * `Halo CE`); `search` ranks by the main title and may miss them.
     */
    private suspend fun searchAlternativeNames(query: String): List<GameExpandedResponse> {
        val terms = query.split(WHITESPACE).filter { it.isNotEmpty() }.take(MAX_ALTERNATIVE_NAME_TERMS)
        return client.typed.expandedGames.list {
            fields(*SEARCH_FIELDS)
            where { terms.forEach { GameFields.alternativeNames.name.containing(it) } }
            desc(GameFields.totalRatingCount)
            limit(REMOTE_LIMIT)
        }
    }

    private data class RemoteSearch(
        val status: RemoteSearchStatus,
        val gameIds: List<Long> = emptyList(),
    )

    private companion object {
        const val TAG = "SearchRepository"
        const val LOCAL_LIMIT = 50
        const val FEW_RESULTS = 5
        const val REMOTE_LIMIT = 20
        const val MAX_ALTERNATIVE_NAME_TERMS = 5
        val WHITESPACE = Regex("\\s+")

        /** Everything [GameCacheMapper] stores, including alternative names for local search. */
        val SEARCH_FIELDS = arrayOf<IgdbField<*>>(
            GameFields.id,
            GameFields.name,
            GameFields.alternativeNames.name,
            GameFields.cover.imageId,
            GameFields.firstReleaseDate,
            GameFields.rating,
            GameFields.summary,
            GameFields.platforms.name,
            GameFields.genres.name,
            GameFields.involvedCompanies.developer,
            GameFields.involvedCompanies.publisher,
            GameFields.involvedCompanies.companyName,
        )
    }
}
