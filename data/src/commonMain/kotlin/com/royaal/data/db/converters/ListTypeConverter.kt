package com.royaal.data.db.converters

import androidx.room.TypeConverter
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json

abstract class BaseListConverter<T>(serializer: KSerializer<T>) {
    private val listSerializer = ListSerializer(serializer)

    @TypeConverter
    fun fromList(list: List<T>?): String? {
        return list?.let { Json.encodeToString(listSerializer, it) }
    }

    @TypeConverter
    fun toList(json: String?): List<T>? {
        return json?.let { Json.decodeFromString(listSerializer, it) }
    }
}

class StringListConverter : BaseListConverter<String>(String.serializer())