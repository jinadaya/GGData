package com.royaal.data.network.igdb.query

import com.royaal.data.network.igdb.IgdbEndpoint

/**
 * A `/multiquery` body: up to 10 named sub-queries executed in one request.
 *
 * ```
 * buildMultiQuery {
 *     query(IgdbEndpoint.Games, "top") {
 *         fields("name", "total_rating")
 *         sortDesc("total_rating")
 *         limit(10)
 *     }
 *     count(IgdbEndpoint.Games, "ps5Count") {
 *         where { "platforms" eq 167 }
 *     }
 * }
 * ```
 *
 * The response is a JSON array of `{"name": ..., "result": [...]}` or `{"name": ..., "count": n}`.
 */
class MultiQuery internal constructor(val entries: List<Entry>) {

    class Entry(
        val endpoint: IgdbEndpoint,
        val name: String,
        val query: ApicalypseQuery,
        val isCount: Boolean,
    ) {
        fun render(): String {
            val path = if (isCount) "${endpoint.path}/count" else endpoint.path
            return "query $path ${ApicalypseValues.string(name)} { ${query.body} };"
        }
    }

    val body: String = entries.joinToString("\n") { it.render() }

    override fun toString(): String = body

    companion object {
        const val MAX_QUERIES = 10
    }
}

@ApicalypseDsl
class MultiQueryBuilder @PublishedApi internal constructor() {
    private val entries = mutableListOf<MultiQuery.Entry>()

    fun query(endpoint: IgdbEndpoint, name: String, query: ApicalypseQuery) {
        add(MultiQuery.Entry(endpoint, name, query, isCount = false))
    }

    fun query(endpoint: IgdbEndpoint, name: String, block: ApicalypseBuilder.() -> Unit) =
        query(endpoint, name, ApicalypseBuilder().apply(block).build())

    fun count(endpoint: IgdbEndpoint, name: String, query: ApicalypseQuery) {
        add(MultiQuery.Entry(endpoint, name, query, isCount = true))
    }

    fun count(endpoint: IgdbEndpoint, name: String, block: ApicalypseBuilder.() -> Unit = {}) =
        count(endpoint, name, ApicalypseBuilder().apply(block).build())

    @PublishedApi
    internal fun build(): MultiQuery {
        require(entries.isNotEmpty()) { "Multiquery needs at least one query" }
        return MultiQuery(entries.toList())
    }

    private fun add(entry: MultiQuery.Entry) {
        require(entry.name.isNotBlank()) { "Multiquery name must not be blank" }
        require(entries.none { it.name == entry.name }) { "Duplicate multiquery name '${entry.name}'" }
        require(entries.size < MultiQuery.MAX_QUERIES) { "IGDB allows at most ${MultiQuery.MAX_QUERIES} queries per multiquery" }
        entries += entry
    }
}

inline fun buildMultiQuery(block: MultiQueryBuilder.() -> Unit): MultiQuery =
    MultiQueryBuilder().apply(block).build()
