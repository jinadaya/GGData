package com.royaal.data.network.igdb.typed

import com.royaal.data.network.igdb.query.Filter
import com.royaal.data.network.igdb.query.SortOrder

/** A compile-time typed IGDB field. The [path] is the APICalypse field path. */
sealed class IgdbField<T>(val path: String) {
    init { require(path.isNotBlank()) { "IGDB field path must not be blank" } }
}

open class StringField(path: String) : IgdbField<String>(path)
open class NumberField<N : Number>(path: String) : IgdbField<N>(path)
open class BooleanField(path: String) : IgdbField<Boolean>(path)
open class LongField(path: String) : IgdbField<Long>(path)
open class StringListField(path: String) : IgdbField<List<String>>(path)
open class LongListField(path: String) : IgdbField<List<Long>>(path)
open class AnyField(path: String) : IgdbField<Any?>(path)

class TypedCondition internal constructor(internal val filter: Filter)

data class TypedOrder(val field: IgdbField<*>, val order: SortOrder)

fun IgdbField<*>.asc() = TypedOrder(this, SortOrder.Asc)
fun IgdbField<*>.desc() = TypedOrder(this, SortOrder.Desc)
