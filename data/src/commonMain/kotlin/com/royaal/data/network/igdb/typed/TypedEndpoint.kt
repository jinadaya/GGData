package com.royaal.data.network.igdb.typed

import com.royaal.data.network.igdb.IgdbEndpointApi
import com.royaal.data.network.igdb.query.ApicalypseQuery
import com.royaal.data.network.igdb.query.ApicalypseValues
import com.royaal.data.network.igdb.query.Filter
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.JsonArray

/** Typed facade over an existing [IgdbEndpointApi]. */
class IgdbTypedEndpoint<T> internal constructor(
    private val api: IgdbEndpointApi,
    private val serializer: KSerializer<T>,
) {
    private val listSerializer = ListSerializer(serializer)

    suspend fun list(block: TypedQueryBuilder.() -> Unit = {}): List<T> =
        api.query(listSerializer, typedQuery(block))

    suspend fun first(block: TypedQueryBuilder.() -> Unit = {}): T? {
        val q = typedQuery(block).withDefaultFields().withPaging(1, null)
        return api.queryJson(q).firstOrNull()?.let { api.client.decode(serializer, it) }
    }

    suspend fun byId(id: Long, block: TypedQueryBuilder.() -> Unit = {}): T? {
        val q = typedQuery(block).withDefaultFields()
            .withFilter(Filter.Condition("id", "=", id.toString()))
            .withPaging(1, null)
        return api.queryJson(q).firstOrNull()?.let { api.client.decode(serializer, it) }
    }

    suspend fun byIds(ids: Collection<Long>, block: TypedQueryBuilder.() -> Unit = {}): List<T> {
        if (ids.isEmpty()) return emptyList()
        val base = typedQuery(block).withDefaultFields()
        return ids.distinct().chunked(ApicalypseQuery.MAX_LIMIT).flatMap { chunk ->
            val q = base
                .withFilter(Filter.Condition("id", "=", ApicalypseValues.list(chunk, '(', ')')))
                .withPaging(chunk.size, null)
            api.query(listSerializer, q)
        }
    }

    suspend fun search(term: String, block: TypedQueryBuilder.() -> Unit = {}): List<T> {
        require(term.isNotBlank()) { "Search term must not be blank" }
        val base = typedQuery(block).withDefaultFields()
        val q = ApicalypseQuery(
            fields = base.fields,
            excludes = base.excludes,
            filter = base.filter,
            search = term,
            sort = base.sort,
            limit = base.limit,
            offset = base.offset,
            extraClauses = base.extraClauses,
        )
        return api.query(listSerializer, q)
    }

    suspend fun count(block: TypedQueryBuilder.() -> Unit = {}): Long =
        api.count(typedQuery(block))

    fun paginate(
        pageSize: Int = ApicalypseQuery.MAX_LIMIT,
        block: TypedQueryBuilder.() -> Unit = {},
    ): Flow<List<T>> = flow {
        require(pageSize in 1..ApicalypseQuery.MAX_LIMIT)
        val base = typedQuery(block)
        var offset = base.offset ?: 0
        while (true) {
            val page = api.queryJson(base.withPaging(pageSize, offset))
            if (page.isNotEmpty()) emit(api.client.decode(listSerializer, page))
            if (page.size < pageSize) break
            offset += pageSize
        }
    }

    suspend fun fetchAll(
        maxItems: Int = Int.MAX_VALUE,
        pageSize: Int = ApicalypseQuery.MAX_LIMIT,
        block: TypedQueryBuilder.() -> Unit = {},
    ): List<T> {
        require(maxItems >= 0)
        require(pageSize in 1..ApicalypseQuery.MAX_LIMIT)
        val result = ArrayList<T>()
        val base = typedQuery(block)
        var offset = base.offset ?: 0
        while (result.size < maxItems) {
            val size = minOf(pageSize, maxItems - result.size)
            val page = api.queryJson(base.withPaging(size, offset))
            if (page.isEmpty()) break
            result += api.client.decode(listSerializer, page)
            if (page.size < size) break
            offset += size
        }
        return result
    }

    suspend fun json(block: TypedQueryBuilder.() -> Unit = {}): JsonArray =
        api.queryJson(typedQuery(block))
}
