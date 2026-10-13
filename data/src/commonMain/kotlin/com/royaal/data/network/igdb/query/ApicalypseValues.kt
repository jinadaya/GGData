package com.royaal.data.network.igdb.query

import kotlin.math.abs
import kotlin.math.floor
import kotlin.text.iterator

/**
 * Turns Kotlin values into Apicalypse literals.
 *
 * Supported: `null`, [String] (quoted + escaped), [Boolean], any [Number].
 * IGDB timestamps are **Unix seconds** — pass `Long`s, not millis.
 */
object ApicalypseValues {

    fun string(value: String): String = buildString(value.length + 2) {
        append('"')
        for (c in value) {
            when (c) {
                '"' -> append("\\\"")
                '\\' -> append("\\\\")
                '\n' -> append(' ')
                else -> append(c)
            }
        }
        append('"')
    }

    fun number(value: Number): String = when (value) {
        is Double -> double(value)
        is Float -> double(value.toDouble())
        else -> value.toString()
    }

    fun value(value: Any?): String = when (value) {
        null -> "null"
        is String -> string(value)
        is Boolean -> value.toString()
        is Number -> number(value)
        else -> throw IllegalArgumentException(
            "Unsupported Apicalypse value type: ${value::class.simpleName}. " +
                "Use String, Boolean, Number or null (map enums to their IGDB ids)."
        )
    }

    /** Renders a collection with the given brackets: `(1,2)`, `[1,2]`, `{1,2}`. */
    fun list(values: Collection<Any?>, open: Char, close: Char): String {
        require(values.isNotEmpty()) { "Collection filter values must not be empty" }
        return values.joinToString(separator = ",", prefix = open.toString(), postfix = close.toString()) { value(it) }
    }

    private fun double(value: Double): String {
        require(value.isFinite()) { "Apicalypse does not support NaN/Infinity" }
        return if (value == floor(value) && abs(value) < 1e15) value.toLong().toString() else value.toString()
    }
}
