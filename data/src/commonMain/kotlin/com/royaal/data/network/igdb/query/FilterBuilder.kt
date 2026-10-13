package com.royaal.data.network.igdb.query

import com.royaal.data.network.igdb.query.ApicalypseValues.list
import com.royaal.data.network.igdb.query.ApicalypseValues.number
import com.royaal.data.network.igdb.query.ApicalypseValues.string

/**
 * DSL for Apicalypse `where` clauses. Field names are plain strings and may be
 * dotted paths into expanded objects (`"involved_companies.company.name"`).
 *
 * Every statement at the same level is joined with `&`. Use [anyOf] for `|`.
 *
 * ```
 * where {
 *     "total_rating" gte 80
 *     "platforms" containsAny listOf(48, 167)       // platforms = (48,167)
 *     "cover".isNotNull()
 *     anyOf {
 *         "genres" eq 12
 *         "themes" eq 17
 *     }
 * }
 * // total_rating >= 80 & platforms = (48,167) & cover != null & (genres = 12 | themes = 17)
 * ```
 */
@ApicalypseDsl
class FilterBuilder @PublishedApi internal constructor() {

    private val filters = mutableListOf<Filter>()

    /** Adds a prebuilt [Filter] node. */
    fun add(filter: Filter) {
        filters += filter
    }

    /** Adds a raw expression, e.g. `raw("platforms.generation = 9")`. */
    fun raw(expression: String) {
        filters += Filter.Raw(expression)
    }

    // ---------------------------------------------------------------- equality

    infix fun String.eq(value: Number) = condition(this, "=", number(value))
    infix fun String.eq(value: String) = condition(this, "=", string(value))
    infix fun String.eq(value: Boolean) = condition(this, "=", value.toString())

    infix fun String.neq(value: Number) = condition(this, "!=", number(value))
    infix fun String.neq(value: String) = condition(this, "!=", string(value))
    infix fun String.neq(value: Boolean) = condition(this, "!=", value.toString())

    fun String.isNull() = condition(this, "=", "null")
    fun String.isNotNull() = condition(this, "!=", "null")

    // ---------------------------------------------------------------- comparison

    infix fun String.gt(value: Number) = condition(this, ">", number(value))
    infix fun String.gte(value: Number) = condition(this, ">=", number(value))
    infix fun String.lt(value: Number) = condition(this, "<", number(value))
    infix fun String.lte(value: Number) = condition(this, "<=", number(value))

    /** Inclusive range: `"first_release_date" between start..end` → `(f >= start & f <= end)`. */
    infix fun <T> String.between(range: ClosedRange<T>) where T : Number, T : Comparable<T> {
        filters += Filter.Group(
            Filter.Logical.And,
            listOf(
                Filter.Condition(this, ">=", number(range.start)),
                Filter.Condition(this, "<=", number(range.endInclusive)),
            ),
        )
    }

    // ---------------------------------------------------------------- arrays / membership

    /** Array field contains **at least one** of [values]: `field = (a,b)`. Also works as "IN" for scalar fields. */
    infix fun String.containsAny(values: Collection<Any>) = condition(this, "=", list(values, '(', ')'))

    /** Array field contains **all** of [values]: `field = [a,b]`. */
    infix fun String.containsAll(values: Collection<Any>) = condition(this, "=", list(values, '[', ']'))

    /** Array field contains **none** of [values]: `field != (a,b)`. Also works as "NOT IN". */
    infix fun String.containsNone(values: Collection<Any>) = condition(this, "!=", list(values, '(', ')'))

    /** Array field does **not contain all** of [values]: `field != [a,b]`. */
    infix fun String.notContainsAll(values: Collection<Any>) = condition(this, "!=", list(values, '[', ']'))

    /** Array field contains **exactly** [values], nothing more: `field = {a,b}`. */
    infix fun String.containsExactly(values: Collection<Any>) = condition(this, "=", list(values, '{', '}'))

    /** Scalar field is one of [values] (alias of [containsAny]). */
    infix fun String.isIn(values: Collection<Any>) = containsAny(values)

    /** Scalar field is none of [values] (alias of [containsNone]). */
    infix fun String.notIn(values: Collection<Any>) = containsNone(values)

    // ---------------------------------------------------------------- text matching

    /** `field ~ "value"` (case-insensitive equality). */
    infix fun String.equalsIgnoringCase(value: String) = condition(this, "~", string(value))

    /** Prefix match, case-insensitive by default: `field ~ "value"*`. */
    infix fun String.startingWith(value: String) = startingWith(value, ignoreCase = true)
    fun String.startingWith(value: String, ignoreCase: Boolean) =
        condition(this, textOperator(ignoreCase), string(value) + "*")

    /** Suffix match, case-insensitive by default: `field ~ *"value"`. */
    infix fun String.endingWith(value: String) = endingWith(value, ignoreCase = true)
    fun String.endingWith(value: String, ignoreCase: Boolean) =
        condition(this, textOperator(ignoreCase), "*" + string(value))

    /** Substring match, case-insensitive by default: `field ~ *"value"*`. */
    infix fun String.containing(value: String) = containing(value, ignoreCase = true)
    fun String.containing(value: String, ignoreCase: Boolean) =
        condition(this, textOperator(ignoreCase), "*" + string(value) + "*")

    // ---------------------------------------------------------------- grouping

    /** Statements inside are joined with `|`. */
    fun anyOf(block: FilterBuilder.() -> Unit) {
        FilterBuilder().apply(block).build(Filter.Logical.Or)?.let(filters::add)
    }

    /** Statements inside are joined with `&` (useful inside [anyOf]). */
    fun allOf(block: FilterBuilder.() -> Unit) {
        FilterBuilder().apply(block).build(Filter.Logical.And)?.let(filters::add)
    }

    // ---------------------------------------------------------------- internals

    @PublishedApi
    internal fun build(operator: Filter.Logical = Filter.Logical.And): Filter? =
        if (filters.isEmpty()) null else Filter.combine(operator, filters.toList())

    private fun condition(field: String, operator: String, value: String) {
        filters += Filter.Condition(field, operator, value)
    }

    private fun textOperator(ignoreCase: Boolean) = if (ignoreCase) "~" else "="
}

/** Builds a standalone [Filter] (all statements joined with `&`), or `null` if empty. */
fun filter(block: FilterBuilder.() -> Unit): Filter? = FilterBuilder().apply(block).build()
