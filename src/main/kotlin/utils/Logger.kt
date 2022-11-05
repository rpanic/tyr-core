package utils

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import java.text.SimpleDateFormat
import java.util.*

object Logger {

    val json = Config.config.logging.json

    val dateFormatter = SimpleDateFormat("dd/MM/yyyy:hh:mm:ss Z")

    fun log(level: String, msg: () -> String, attr: List<Pair<String, String>> = listOf()){
        if(!json){
            println(msg())
        }else{
            val map = mutableMapOf<String, String>()
            map["level"] = level
            val className = msg::javaClass.get().name.split("$")[0]
            map["class"] = className
            map["message"] = msg()
            map["datetime"] = dateFormatter.format(Date())
            attr.forEach { map[it.first] = it.second }
            println(Json.encodeToString(JsonObject(map.mapValues { JsonPrimitive(it.value) })))
        }
    }
}

fun info(msg: () -> String){
    Logger.log("info", msg)
}

fun info(vararg args: Pair<String, String>, f: () -> String){
    Logger.log("info", f, args.toList())
}

fun error(msg: () -> String){
    Logger.log("error", msg)
}

fun error(vararg args: Pair<String, String>, f: () -> String){
    Logger.log("error", f, args.toList())
}

fun debug(msg: () -> String){
    Logger.log("debug", msg)
}

fun debug(vararg args: Pair<String, String>, f: () -> String){
    Logger.log("debug", f, args.toList())
}