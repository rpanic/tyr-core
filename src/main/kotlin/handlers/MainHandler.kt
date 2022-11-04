package handlers

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import network.Error
import network.MultiWayConnection

class MainHandler {

    val routes = mutableMapOf<String, (MultiWayConnection, String) -> String>()

    fun handlers(){

        listOf(

            PeerHandler(),
            ObjectHandler()

        ).forEach {
            it.routes(this)
        }

    }

    init {
        handlers()
    }

    fun handle(conn: MultiWayConnection, s: String, type: String) : String{

        val handler = routes.getOrDefault(type, ::defaultHandler);
        return handler(conn, s)

    }

    fun defaultHandler(c: MultiWayConnection, s: String) : String{

        println("Message not supported: $s")
        return Json.encodeToString(Error("Unsupported message type received"))

    }

    inline fun <reified T, reified R> route(type: String, crossinline f: RouteParams.(T) -> R) {

        routes[type] = { conn, msg ->
            val m = Json.decodeFromString<T>(msg)
            val p = RouteParams(conn, msg)
            val r = f(p, m)

            if(r !== null){
                Json.encodeToString(r)
            }else{
                "" //Will be ignored and no response sent
            }
        }
    }
}

data class RouteParams(
    val conn: MultiWayConnection,
    val rawString: String
)