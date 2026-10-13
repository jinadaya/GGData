package com.royaal.data.network.igdb.query

/**
 * A node of an Apicalypse `where` expression.
 *
 * You normally build these through [FilterBuilder] (`where { ... }`), but the
 * types are public so filters can be composed, stored and reused.
 */
sealed interface Filter {

    /** Renders the expression exactly as it goes into the request body (without `where` / `;`). */
    fun render(): String

    /** Arbitrary expression, inserted verbatim. Wrapped in parentheses when combined with others. */
    class Raw(val expression: String) : Filter {
        init {
            require(expression.isNotBlank()) { "Raw filter expression must not be blank" }
        }

        override fun render(): String = expression.trim().removeSuffix(";").trim()
    }

    /** `field operator value`, where [value] is already rendered (quoted / bracketed). */
    class Condition(val field: String, val operator: String, val value: String) : Filter {
        init {
            require(field.isNotBlank()) { "Filter field must not be blank" }
        }

        override fun render(): String = "${field.trim()} $operator $value"
    }

    /** Several filters joined with `&` or `|`. */
    class Group(val operator: Logical, val children: List<Filter>) : Filter {
        init {
            require(children.isNotEmpty()) { "Filter group must contain at least one filter" }
        }

        override fun render(): String {
            if (children.size == 1) return children.single().render()
            return children.joinToString(" ${operator.symbol} ") { child ->
                when {
                    child is Group && child.children.size > 1 -> "(${child.render()})"
                    child is Raw -> "(${child.render()})"
                    else -> child.render()
                }
            }
        }
    }

    enum class Logical(val symbol: String) { And("&"), Or("|") }

    companion object {
        fun allOf(vararg filters: Filter): Filter = combine(Logical.And, filters.toList())
        fun anyOf(vararg filters: Filter): Filter = combine(Logical.Or, filters.toList())

        internal fun combine(operator: Logical, filters: List<Filter>): Filter =
            if (filters.size == 1) filters.single() else Group(operator, filters)
    }
}
