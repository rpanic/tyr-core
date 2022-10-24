package handlers

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import network.Error

class MainHandler {

    val routes = mutableMapOf<String, (String) -> String>()

    fun handlers(){

        listOf(

            PeerHandler()

        ).forEach {
            it.routes(this)
        }

    }

    init {
        handlers()
    }

    fun handle(s: String, type: String) : String{

        val handler = routes.getOrDefault(type, ::defaultHandler)
        return handler(s)

    }

    fun defaultHandler(s: String) : String{

        println("Message not supported: $s")
        return Json.encodeToString(Error("Unsupported message type received"))

    }

    inline fun <reified T, reified R> route(type: String, crossinline f: (T) -> R) {

        routes[type] = {
            val m = Json.decodeFromString<T>(it)
            val r = f(m)

            Json.encodeToString(r)
        }

    }

}