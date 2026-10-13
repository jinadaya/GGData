package com.royaal.data.network.igdb.query

import kotlin.collections.plusAssign

@DslMarker
annotation class ApicalypseDsl

enum class SortOrder(val value: String) { Asc("asc"), Desc("desc") }

/**
 * An immutable Apicalypse request body (`fields ...; where ...; sort ...; limit ...;`).
 *
 * Build with [apicalypse]. [body] is what gets POSTed to IGDB.
 */
class ApicalypseQuery internal constructor(
    val fields: List<String>,
    val excludes: List<String>,
    val filter: Filter?,
    val search: String?,
    val sort: Sort?,
    val limit: Int?,
    val offset: Int?,
    val extraClauses: List<String>,
) {
    class Sort(val field: String, val order: SortOrder)

    /** The rendered request body. */
    val body: String = render()

    /** Copy with different paging (pass `null` to drop a clause). */
    fun withPaging(limit: Int?, offset: Int?): ApicalypseQuery = copy(limit = limit, offset = offset)

    /** Copy whose filter is `(existing) & additional`. */
    fun withFilter(additional: Filter): ApicalypseQuery =
        copy(filter = filter?.let { Filter.Group(Filter.Logical.And, listOf(it, additional)) } ?: additional)

    /** Copy with [fields] replaced. */
    fun withFields(fields: List<String>): ApicalypseQuery = copy(fields = fields)

    /** Copy that selects `*` when no fields were chosen (IGDB otherwise returns ids only). */
    fun withDefaultFields(): ApicalypseQuery = if (fields.isEmpty()) copy(fields = listOf("*")) else this

    /** Copy with sort replaced (or removed with `null`). */
    fun withSort(sort: Sort?): ApicalypseQuery = copy(sort = sort)

    override fun toString(): String = body
    override fun equals(other: Any?): Boolean = other is ApicalypseQuery && other.body == body
    override fun hashCode(): Int = body.hashCode()

    private fun copy(
        fields: List<String> = this.fields,
        filter: Filter? = this.filter,
        sort: Sort? = this.sort,
        limit: Int? = this.limit,
        offset: Int? = this.offset,
    ) = ApicalypseQuery(fields, excludes, filter, search, sort, limit, offset, extraClauses)

    private fun render(): String = buildString {
        search?.let { append("search ").append(ApicalypseValues.string(it)).append("; ") }
        if (fields.isNotEmpty()) append("fields ").append(fields.joinToString(",")).append("; ")
        if (excludes.isNotEmpty()) append("exclude ").append(excludes.joinToString(",")).append("; ")
        filter?.let { append("where ").append(it.render()).append("; ") }
        sort?.let { append("sort ").append(it.field).append(' ').append(it.order.value).append("; ") }
        limit?.let { append("limit ").append(it).append("; ") }
        offset?.let { append("offset ").append(it).append("; ") }
        extraClauses.forEach { append(it).append("; ") }
    }.trim()

    companion object {
        /** IGDB's maximum page size. */
        const val MAX_LIMIT = 500

        /** Empty body (useful for `count`). */
        val Empty: ApicalypseQuery = ApicalypseBuilder().build()
    }
}

/**
 * Builder for [ApicalypseQuery].
 *
 * ```
 * apicalypse {
 *     fields("name", "total_rating", "first_release_date")
 *     fields {
 *         "cover" { +"image_id" }
 *         "platforms" { +"name"; +"abbreviation" }
 *     }
 *     exclude("storyline")
 *     where { "total_rating_count" gt 100 }
 *     sort("total_rating", SortOrder.Desc)
 *     limit(20)
 * }
 * ```
 */
@ApicalypseDsl
class ApicalypseBuilder {
    private val selectedFields = linkedSetOf<String>()
    private val excludedFields = linkedSetOf<String>()
    private val filters = mutableListOf<Filter>()
    private val extraClauses = mutableListOf<String>()
    private var searchTerm: String? = null
    private var sortClause: ApicalypseQuery.Sort? = null
    private var limitValue: Int? = null
    private var offsetValue: Int? = null

    // ---------------------------------------------------------------- fields

    /** `fields a,b,c;` — accepts dotted paths and comma separated strings (`"name,cover.image_id"`). */
    fun fields(vararg names: String) = names.forEach { addFields(selectedFields, it) }

    fun fields(names: Iterable<String>) = names.forEach { addFields(selectedFields, it) }

    /** Nested fields DSL — see [FieldsBuilder]. */
    fun fields(block: FieldsBuilder.() -> Unit) {
        FieldsBuilder(prefix = null, sink = selectedFields).block()
    }

    /** `fields *;` */
    fun allFields() = fields("*")

    /** `exclude a,b;` — useful with `fields *`. */
    fun exclude(vararg names: String) = names.forEach { addFields(excludedFields, it) }

    // ---------------------------------------------------------------- search / filter

    /** `search "term";` — full-text search. Results come back relevance-ordered. */
    fun search(term: String) {
        require(term.isNotBlank()) { "Search term must not be blank" }
        searchTerm = term
    }

    /** Adds a where-expression. Multiple `where` calls are joined with `&`. */
    fun where(block: FilterBuilder.() -> Unit) {
        FilterBuilder().apply(block).build()?.let(filters::add)
    }

    fun where(filter: Filter) {
        filters += filter
    }

    /** Raw where-expression, e.g. `where("rating > 75 & genres = (5,12)")`. */
    fun where(rawExpression: String) {
        filters += Filter.Raw(rawExpression)
    }

    // ---------------------------------------------------------------- sort / paging

    /** IGDB supports a single sort field; the last call wins. */
    fun sort(field: String, order: SortOrder = SortOrder.Asc) {
        require(field.isNotBlank()) { "Sort field must not be blank" }
        sortClause = ApicalypseQuery.Sort(field.trim(), order)
    }

    fun sortAsc(field: String) = sort(field, SortOrder.Asc)
    fun sortDesc(field: String) = sort(field, SortOrder.Desc)

    /** 1..500 (IGDB default is 10). */
    fun limit(value: Int) {
        require(value in 1..ApicalypseQuery.MAX_LIMIT) { "limit must be in 1..${ApicalypseQuery.MAX_LIMIT}, was $value" }
        limitValue = value
    }

    fun offset(value: Int) {
        require(value >= 0) { "offset must be >= 0, was $value" }
        offsetValue = value
    }

    /** Zero-based page helper: sets `limit = size`, `offset = index * size`. */
    fun page(index: Int, size: Int) {
        require(index >= 0) { "page index must be >= 0" }
        limit(size)
        offset(index * size)
    }

    /** Escape hatch: appends an arbitrary clause verbatim (trailing `;` optional). */
    fun clause(raw: String) {
        require(raw.isNotBlank()) { "Clause must not be blank" }
        extraClauses += raw.trim().removeSuffix(";").trim()
    }

    fun build(): ApicalypseQuery = ApicalypseQuery(
        fields = selectedFields.toList(),
        excludes = excludedFields.toList(),
        filter = if (filters.isEmpty()) null else Filter.combine(Filter.Logical.And, filters.toList()),
        search = searchTerm,
        sort = sortClause,
        limit = limitValue,
        offset = offsetValue,
        extraClauses = extraClauses.toList(),
    )

    private fun addFields(target: MutableSet<String>, raw: String) {
        raw.split(',').map { it.trim() }.filter { it.isNotEmpty() }.forEach { target += it }
    }
}

/**
 * Nested field selection.
 *
 * ```
 * fields {
 *     +"name"
 *     "cover" { +"image_id"; +"width" }               // cover.image_id, cover.width
 *     "involved_companies" {
 *         +"developer"
 *         "company" { +"name"; "logo" { +"image_id" } } // involved_companies.company.logo.image_id
 *     }
 *     "screenshots" { all() }                          // screenshots.*
 * }
 * ```
 */
@ApicalypseDsl
class FieldsBuilder internal constructor(
    private val prefix: String?,
    private val sink: MutableSet<String>,
) {
    operator fun String.unaryPlus() {
        sink += path(this)
    }

    fun field(vararg names: String) = names.forEach { sink += path(it) }

    /** `prefix.*` (or `*` at the top level). */
    fun all() {
        sink += path("*")
    }

    operator fun String.invoke(block: FieldsBuilder.() -> Unit) {
        FieldsBuilder(path(this), sink).block()
    }

    private fun path(name: String): String {
        val clean = name.trim()
        require(clean.isNotEmpty()) { "Field name must not be blank" }
        return if (prefix == null) clean else "$prefix.$clean"
    }
}

/** Entry point of the query DSL. */
inline fun apicalypse(block: ApicalypseBuilder.() -> Unit): ApicalypseQuery =
    ApicalypseBuilder().apply(block).build()
