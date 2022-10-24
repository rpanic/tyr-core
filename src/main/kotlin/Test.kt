import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import network.Hello
import network.KarmaObject
import network.Promise
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

fun main(){

    println(Json.encodeToString(Hello("0.7.0", "client") as KarmaObject))

//    val promise = Promise<String>()
//
//    Thread {
//        println("Listening")
//        val r = promise.await()
//        println("REsult: $r")
//    }.start()
//
//    Thread {
//        Thread.sleep(4000)
//        promise.resolve("Hello")
//    }.start()

}

