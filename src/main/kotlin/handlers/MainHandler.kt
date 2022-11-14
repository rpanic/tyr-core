package handlers

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import network.Error
import network.MultiWayConnection
import utils.debug

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
        try {
            val ret = handler(conn, s)
            return ret
        } catch (e: Exception){
            e.printStackTrace()
            return defaultHandler(conn, s)
        }

    }

    fun defaultHandler(c: MultiWayConnection, s: String) : String{

        if(s.contains("\"type\":\"error\"")){ //to prevent error loops
            return ""
        }
        debug("peer" to c.underlyingConnection.peer.getAddress()) {"Message not supported: $s"}
        return Json.encodeToString(Error("Unsupported message type received"))

    }

    val JsonIgnoreFormat = Json { ignoreUnknownKeys = true }

    inline fun <reified T, reified R> route(type: String, crossinline f: RouteParams.(T) -> R) {

        routes[type] = { conn, msg ->
            println(msg)
            val m = JsonIgnoreFormat.decodeFromString<T>(msg)
            val p = RouteParams(conn, msg)
            val r = f(p, m)

            if(r !== null){
//                if(r is JsonObject){
//                    Json.
//                }else{
                    Json.encodeToString(r)
//                }
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