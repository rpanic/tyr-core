package utils

import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import java.util.*

fun canonicalize(src: JsonElement): JsonElement {
    return if (src is JsonArray) {
        // Canonicalize each element of the array
        val srcArray: JsonArray = src as JsonArray
        val result = mutableListOf<JsonElement>()
        for (i in 0 until srcArray.size) {
            result.add(canonicalize(srcArray[i]))
        }
        JsonArray(result)
    } else if (src is JsonObject) {
        // Sort the attributes by name, and the canonicalize each element of the object
        val srcObject: JsonObject = src as JsonObject
        val result = mutableMapOf<String, JsonElement>()
        val attributes = TreeSet<String>()
        for ((key, value) in srcObject.entries) {
            attributes.add(key)
        }
        for (attribute in attributes) {
            result.put(attribute, canonicalize(srcObject.get(attribute)!!))
        }
        JsonObject(result)
    } else {
        src
    }
}