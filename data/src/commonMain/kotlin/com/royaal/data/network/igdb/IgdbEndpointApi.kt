package com.royaal.data.network.igdb

import com.royaal.data.network.igdb.query.ApicalypseBuilder
import com.royaal.data.network.igdb.query.ApicalypseQuery
import com.royaal.data.network.igdb.query.ApicalypseValues
import com.royaal.data.network.igdb.query.Filter
import com.royaal.data.network.igdb.query.apicalypse
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.longOrNull
import kotlinx.serialization.serializer

/**
 * Requests bound to a single endpoint. Obtain via `client.games`, `client.platforms`, … or `client.endpoint(...)`.
 *
 * Untyped results are `String` / `JsonArray` / `JsonObject`; typed variants are the `...As<T>()`
 * extensions below, where `T` is your own `@Serializable` class.
 */
class IgdbEndpointApi internal constructor(
    val endpoint: IgdbEndpoint,
    val client: IgdbClient,
) {
    // ---------------------------------------------------------------- generic

    suspend fun raw(body: String): String = client.rawQuery(endpoint, body)

    suspend fun query(query: ApicalypseQuery): String = client.query(endpoint, query)

    suspend fun query(block: ApicalypseBuilder.() -> Unit): String = query(apicalypse(block))

    suspend fun queryJson(query: ApicalypseQuery): JsonArray = client.queryJson(endpoint, query)

    suspend fun queryJson(block: ApicalypseBuilder.() -> Unit): JsonArray = queryJson(apicalypse(block))

    suspend fun <T> query(deserializer: DeserializationStrategy<T>, query: ApicalypseQuery): T =
        client.query(endpoint, query, deserializer)

    /** First matching item (forces `limit 1`), or null. */
    suspend fun first(block: ApicalypseBuilder.() -> Unit = {}): JsonObject? {
        val base = apicalypse(block).withDefaultFields()
        return queryJson(base.withPaging(limit = 1, offset = base.offset)).firstOrNull() as? JsonObject
    }

    // ---------------------------------------------------------------- by id

    /**
     * Single item by id. Defaults to `fields *` when the block selects no fields.
     * ```
     * client.games.byId(1942) { fields("name", "cover.image_id") }
     * ```
     */
    suspend fun byId(id: Long, block: ApicalypseBuilder.() -> Unit = {}): JsonObject? {
        val q = apicalypse(block).withDefaultFields()
            .withFilter(Filter.Condition("id", "=", id.toString()))
            .withPaging(limit = 1, offset = null)
        return queryJson(q).firstOrNull() as? JsonObject
    }

    /**
     * Many items by id, transparently chunked into requests of 500.
     * Results are returned **in the order of [ids]**; unknown ids are skipped.
     */
    suspend fun byIds(ids: Collection<Long>, block: ApicalypseBuilder.() -> Unit = {}): JsonArray {
        if (ids.isEmpty()) return JsonArray(emptyList())
        val distinct = ids.distinct()
        val base = apicalypse(block).withDefaultFields()

        val results = distinct.chunked(ApicalypseQuery.MAX_LIMIT).flatMap { chunk ->
            val q = base
                .withFilter(Filter.Condition("id", "=", ApicalypseValues.list(chunk, '(', ')')))
                .withPaging(limit = chunk.size, offset = null)
            queryJson(q)
        }
        val byId = results.associateBy { (it as? JsonObject)?.get("id")?.jsonPrimitive?.longOrNull }
        return JsonArray(distinct.mapNotNull { byId[it] })
    }

    // ---------------------------------------------------------------- search / count

    /**
     * Full-text search on this endpoint (`search "term";`).
     * Not every endpoint supports `search` — games, platforms, characters, collections, themes do.
     * For cross-entity search use `client.search`.
     */
    suspend fun search(term: String, block: ApicalypseBuilder.() -> Unit = {}): JsonArray =
        queryJson(apicalypse { block(); search(term) })

    suspend fun count(query: ApicalypseQuery = ApicalypseQuery.Empty): Long = client.count(endpoint, query)

    /** `count { where { "platforms" eq 167 } }` */
    suspend fun count(block: ApicalypseBuilder.() -> Unit): Long = count(apicalypse(block))

    suspend fun protobuf(query: ApicalypseQuery): ByteArray = client.protobuf(endpoint, query)

    suspend fun protobuf(block: ApicalypseBuilder.() -> Unit): ByteArray = protobuf(apicalypse(block))

    // ---------------------------------------------------------------- paging

    /**
     * Cold [Flow] of pages using limit/offset. Starts at the block's `offset` (or 0) and stops
     * on the first short page. Add a stable `sort` (e.g. `sortAsc("id")`) for consistent paging.
     */
    fun paginate(
        pageSize: Int = ApicalypseQuery.MAX_LIMIT,
        block: ApicalypseBuilder.() -> Unit = {},
    ): Flow<JsonArray> {
        require(pageSize in 1..ApicalypseQuery.MAX_LIMIT) { "pageSize must be in 1..${ApicalypseQuery.MAX_LIMIT}" }
        val base = apicalypse(block)
        return flow {
            var offset = base.offset ?: 0
            while (true) {
                val page = queryJson(base.withPaging(limit = pageSize, offset = offset))
                if (page.isNotEmpty()) emit(page)
                if (page.size < pageSize) break
                offset += pageSize
            }
        }
    }

    /** Collects up to [maxItems] items via [paginate]. Mind the 4 req/s limit for large sets. */
    suspend fun fetchAll(
        maxItems: Int = Int.MAX_VALUE,
        pageSize: Int = ApicalypseQuery.MAX_LIMIT,
        block: ApicalypseBuilder.() -> Unit = {},
    ): JsonArray {
        val items = ArrayList<JsonElement>()
        paginateUntil(maxItems, pageSize, block) { items.addAll(it) }
        return JsonArray(items.take(maxItems))
    }

    private suspend fun paginateUntil(
        maxItems: Int,
        pageSize: Int,
        block: ApicalypseBuilder.() -> Unit,
        onPage: (JsonArray) -> Unit,
    ) {
        var collected = 0
        val base = apicalypse(block)
        var offset = base.offset ?: 0
        while (collected < maxItems) {
            val size = minOf(pageSize, maxItems - collected)
            val page = queryJson(base.withPaging(limit = size, offset = offset))
            onPage(page)
            collected += page.size
            if (page.size < size) break
            offset += size
        }
    }
}

// ========================================================================== typed helpers
// T is YOUR @Serializable class. These are inline/reified, so they're Kotlin-only (not exported to Swift).

/** Decodes the whole response as [T] (e.g. `List<MyGame>` or a custom wrapper). */
suspend inline fun <reified T> IgdbEndpointApi.queryAs(block: ApicalypseBuilder.() -> Unit): T =
    query(serializer<T>(), apicalypse(block))

/** Decodes the response as `List<T>`. */
suspend inline fun <reified T> IgdbEndpointApi.listAs(block: ApicalypseBuilder.() -> Unit): List<T> =
    query(ListSerializer(serializer<T>()), apicalypse(block))

suspend inline fun <reified T> IgdbEndpointApi.firstAs(noinline block: ApicalypseBuilder.() -> Unit = {}): T? =
    first(block)?.let { client.decode(serializer<T>(), it) }

suspend inline fun <reified T> IgdbEndpointApi.byIdAs(id: Long, noinline block: ApicalypseBuilder.() -> Unit = {}): T? =
    byId(id, block)?.let { client.decode(serializer<T>(), it) }

suspend inline fun <reified T> IgdbEndpointApi.byIdsAs(
    ids: Collection<Long>,
    noinline block: ApicalypseBuilder.() -> Unit = {},
): List<T> = client.decode(ListSerializer(serializer<T>()), byIds(ids, block))

suspend inline fun <reified T> IgdbEndpointApi.searchAs(
    term: String,
    noinline block: ApicalypseBuilder.() -> Unit = {},
): List<T> = client.decode(ListSerializer(serializer<T>()), search(term, block))

inline fun <reified T> IgdbEndpointApi.paginateAs(
    pageSize: Int = ApicalypseQuery.MAX_LIMIT,
    noinline block: ApicalypseBuilder.() -> Unit = {},
): Flow<List<T>> {
    val listSerializer = ListSerializer(serializer<T>())
    return paginate(pageSize, block).map { client.decode(listSerializer, it) }
}

suspend inline fun <reified T> IgdbEndpointApi.fetchAllAs(
    maxItems: Int = Int.MAX_VALUE,
    pageSize: Int = ApicalypseQuery.MAX_LIMIT,
    noinline block: ApicalypseBuilder.() -> Unit = {},
): List<T> = client.decode(ListSerializer(serializer<T>()), fetchAll(maxItems, pageSize, block))
