package com.royaal.data.network.igdb

import com.royaal.data.network.igdb.query.ApicalypseBuilder
import com.royaal.data.network.igdb.query.apicalypse
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.longOrNull
import kotlinx.serialization.serializer

// ========================================================================== multiquery results

/** The `result` array of the multiquery entry called [name], or null. */
fun JsonArray.multiQueryResult(name: String): JsonArray? = multiQueryEntry(name)?.get("result") as? JsonArray

/** The `count` of a multiquery `count(...)` entry called [name], or null. */
fun JsonArray.multiQueryCount(name: String): Long? = multiQueryEntry(name)?.get("count")?.jsonPrimitive?.longOrNull

private fun JsonArray.multiQueryEntry(name: String): JsonObject? =
    asSequence().filterIsInstance<JsonObject>().firstOrNull { it["name"]?.jsonPrimitive?.contentOrNull == name }

// ========================================================================== typed decoding (Kotlin-only)

/** Decode any element into your own type: `client.decodeAs<MyGame>(obj)`. */
inline fun <reified T> IgdbClient.decodeAs(element: JsonElement): T = decode(serializer<T>(), element)

inline fun <reified T> IgdbClient.decodeListAs(array: JsonArray): List<T> = decode(ListSerializer(serializer<T>()), array)

/** Typed query on any endpoint: `client.queryAs<List<MyGame>>(IgdbEndpoint.Games) { ... }`. */
suspend inline fun <reified T> IgdbClient.queryAs(endpoint: IgdbEndpoint, block: ApicalypseBuilder.() -> Unit): T =
    query(endpoint, apicalypse(block), serializer<T>())

/** Typed result of one multiquery entry. */
inline fun <reified T> IgdbClient.multiQueryResultAs(results: JsonArray, name: String): List<T>? =
    results.multiQueryResult(name)?.let { decodeListAs<T>(it) }

