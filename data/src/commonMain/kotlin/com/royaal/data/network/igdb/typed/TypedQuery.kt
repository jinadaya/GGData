package com.royaal.data.network.igdb.typed

import com.royaal.data.network.igdb.query.ApicalypseBuilder
import com.royaal.data.network.igdb.query.ApicalypseQuery
import com.royaal.data.network.igdb.query.ApicalypseValues
import com.royaal.data.network.igdb.query.Filter
import com.royaal.data.network.igdb.query.SortOrder

@DslMarker
annotation class IgdbTypedDsl

@IgdbTypedDsl
class TypedFilterBuilder internal constructor() {
    private val filters = mutableListOf<Filter>()

    // ------------------------------ conditions: the receiver is the typed field
    infix fun StringField.eq(value: String) = add(this, "=", ApicalypseValues.string(value))
    infix fun StringField.neq(value: String) = add(this, "!=", ApicalypseValues.string(value))
    infix fun StringField.equalsIgnoringCase(value: String) = add(this, "~", ApicalypseValues.string(value))
    fun StringField.isNull() = add(this, "=", "null")
    fun StringField.isNotNull() = add(this, "!=", "null")
    fun StringField.startingWith(value: String, ignoreCase: Boolean = true) = add(this, if (ignoreCase) "~" else "=", ApicalypseValues.string(value) + "*")
    fun StringField.endingWith(value: String, ignoreCase: Boolean = true) = add(this, if (ignoreCase) "~" else "=", "*" + ApicalypseValues.string(value))
    fun StringField.containing(value: String, ignoreCase: Boolean = true) = add(this, if (ignoreCase) "~" else "=", "*" + ApicalypseValues.string(value) + "*")

    infix fun <N : Number> NumberField<N>.eq(value: N) = add(this, "=", ApicalypseValues.number(value))
    infix fun <N : Number> NumberField<N>.neq(value: N) = add(this, "!=", ApicalypseValues.number(value))
    infix fun <N : Number> NumberField<N>.gt(value: N) = add(this, ">", ApicalypseValues.number(value))
    infix fun <N : Number> NumberField<N>.gte(value: N) = add(this, ">=", ApicalypseValues.number(value))
    infix fun <N : Number> NumberField<N>.lt(value: N) = add(this, "<", ApicalypseValues.number(value))
    infix fun <N : Number> NumberField<N>.lte(value: N) = add(this, "<=", ApicalypseValues.number(value))
    infix fun <N> NumberField<N>.between(range: ClosedRange<N>): TypedCondition where N : Number, N : Comparable<N> = addGroup(
        listOf(
            Filter.Condition(path, ">=", ApicalypseValues.number(range.start)),
            Filter.Condition(path, "<=", ApicalypseValues.number(range.endInclusive)),
        ),
    )
    fun <N : Number> NumberField<N>.isNull() = add(this, "=", "null")
    fun <N : Number> NumberField<N>.isNotNull() = add(this, "!=", "null")

    infix fun LongField.eq(value: Long) = add(this, "=", value.toString())
    infix fun LongField.neq(value: Long) = add(this, "!=", value.toString())
    infix fun LongField.gt(value: Long) = add(this, ">", value.toString())
    infix fun LongField.gte(value: Long) = add(this, ">=", value.toString())
    infix fun LongField.lt(value: Long) = add(this, "<", value.toString())
    infix fun LongField.lte(value: Long) = add(this, "<=", value.toString())
    fun LongField.isNull() = add(this, "=", "null")
    fun LongField.isNotNull() = add(this, "!=", "null")

    infix fun BooleanField.eq(value: Boolean) = add(this, "=", value.toString())
    infix fun BooleanField.neq(value: Boolean) = add(this, "!=", value.toString())
    fun BooleanField.isNull() = add(this, "=", "null")
    fun BooleanField.isNotNull() = add(this, "!=", "null")

    infix fun LongListField.containsAny(values: Collection<Long>) = add(this, "=", ApicalypseValues.list(values, '(', ')'))
    infix fun LongListField.containsAll(values: Collection<Long>) = add(this, "=", ApicalypseValues.list(values, '[', ']'))
    infix fun LongListField.containsNone(values: Collection<Long>) = add(this, "!=", ApicalypseValues.list(values, '(', ')'))
    infix fun LongListField.notContainsAll(values: Collection<Long>) = add(this, "!=", ApicalypseValues.list(values, '[', ']'))
    infix fun LongListField.containsExactly(values: Collection<Long>) = add(this, "=", ApicalypseValues.list(values, '{', '}'))
    infix fun LongListField.isIn(values: Collection<Long>) = containsAny(values)
    infix fun LongListField.notIn(values: Collection<Long>) = containsNone(values)

    infix fun StringListField.containsAny(values: Collection<String>) = add(this, "=", ApicalypseValues.list(values, '(', ')'))
    infix fun StringListField.containsAll(values: Collection<String>) = add(this, "=", ApicalypseValues.list(values, '[', ']'))
    infix fun StringListField.containsNone(values: Collection<String>) = add(this, "!=", ApicalypseValues.list(values, '(', ')'))

    fun condition(condition: TypedCondition) { filters += condition.filter }
    operator fun TypedCondition.unaryPlus() { filters += filter }
    fun raw(expression: String) { filters += Filter.Raw(expression) }

    fun anyOf(block: TypedFilterBuilder.() -> Unit) {
        TypedFilterBuilder().apply(block).build(Filter.Logical.Or)?.let(filters::add)
    }

    fun allOf(block: TypedFilterBuilder.() -> Unit) {
        TypedFilterBuilder().apply(block).build(Filter.Logical.And)?.let(filters::add)
    }

    internal fun build(operator: Filter.Logical = Filter.Logical.And): Filter? =
        if (filters.isEmpty()) null else Filter.combine(operator, filters.toList())

    private fun add(field: IgdbField<*>, operator: String, value: String): TypedCondition {
        val condition = TypedCondition(Filter.Condition(field.path, operator, value))
        filters += condition.filter
        return condition
    }

    private fun addGroup(children: List<Filter>, operator: Filter.Logical = Filter.Logical.And): TypedCondition {
        val condition = TypedCondition(Filter.Group(operator, children))
        filters += condition.filter
        return condition
    }
}

@IgdbTypedDsl
class TypedQueryBuilder internal constructor() {
    private val delegate = ApicalypseBuilder()

    fun fields(vararg fields: IgdbField<*>) = delegate.fields(fields.map { it.path })
    fun exclude(vararg fields: IgdbField<*>) = delegate.exclude(*fields.map { it.path }.toTypedArray())
    fun allFields() = delegate.allFields()

    fun where(block: TypedFilterBuilder.() -> Unit) {
        TypedFilterBuilder().apply(block).build()?.let(delegate::where)
    }

    fun where(condition: TypedCondition) = delegate.where(condition.filter)
    fun search(term: String) = delegate.search(term)
    fun orderBy(field: IgdbField<*>, order: SortOrder = SortOrder.Asc) = delegate.sort(field.path, order)
    fun orderBy(order: TypedOrder) = delegate.sort(order.field.path, order.order)
    fun asc(field: IgdbField<*>) = orderBy(field, SortOrder.Asc)
    fun desc(field: IgdbField<*>) = orderBy(field, SortOrder.Desc)
    fun limit(value: Int) = delegate.limit(value)
    fun offset(value: Int) = delegate.offset(value)
    fun page(index: Int, size: Int) = delegate.page(index, size)
    fun clause(raw: String) = delegate.clause(raw)

    internal fun build(): ApicalypseQuery = delegate.build()
}

fun typedQuery(block: TypedQueryBuilder.() -> Unit): ApicalypseQuery =
    TypedQueryBuilder().apply(block).build()
