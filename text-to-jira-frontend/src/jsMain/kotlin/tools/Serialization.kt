package tools

import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer


inline fun <reified T> serializeJson(value: T): String {
    return Json.encodeToString(serializer<T>(), value)
}

inline fun <reified T> readJson(value: String): T {
    return Json.decodeFromString(serializer<T>(), value)
}