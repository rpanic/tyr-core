package network

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.OutputStreamWriter
import java.net.Socket
import java.util.*

class MultiWayConnection() {

    var out: OutputStreamWriter? = null
    var input: Scanner? = null
    var socket: Socket? = null

    fun init(socket: Socket?) {
        if(socket != null) {
            this.socket = socket
            out = socket.getOutputStream().writer()
            input = Scanner(socket.getInputStream().reader())
            startListening()
        }else{
            reconnect()
        }
    }

    data class ResponsePending(val type: String, val promise: Promise<String>)

    val queue = mutableListOf<ResponsePending>()

    fun <T> connected(f: (Scanner, OutputStreamWriter) -> T) : T{
        if(socket == null || socket!!.isClosed){
            reconnect()
        }
        return f(input!!, out!!)
    }

    fun isConnected() : Boolean {
        return socket !== null && !socket!!.isClosed
    }

    fun sendMessageNoResponse(m: String) {

        return connected { scanner, writer ->
            writer.write(m + "\n")
            writer.flush()
        }
    }

    inline fun <reified T, reified R> sendMessage(m: T, response: String): Promise<R> {
        val s = Json.encodeToString(m)

        val p = sendMessage(s, response)

        return p.map { Json.decodeFromString<R>(it) }

    }

    fun sendMessage(m: String, response: String) : Promise<String> {

        return connected { scanner, writer ->

            writer.write(m + "\n")
            writer.flush()
            val promise = Promise<String>()
            queue.add(ResponsePending(response, promise))
            promise

        }
    }

    private fun reconnect(){
        socket = null
        out = null
        input = null
        val s = connectionLost()
        if(s != null){
            socket = s
            out = socket!!.getOutputStream().writer()
            input = Scanner(socket!!.getInputStream().reader())
            startListening()
        }
    }

    private var connectionLost: () -> Socket? = { null }

    fun setConnectionLost(f: () -> Socket?){
        this.connectionLost = f
    }

    var t: Thread? = null

    private fun startListening(){
        val t = Thread{
            if(input != null){
                val input = input!!
                while(input.hasNext()){

                    val line = input.nextLine()
                    messageReceived(line)

                }
            }
        }
        t.start()
        this.t = t
    }

    private val ignoreJsonFormat = Json { ignoreUnknownKeys = true }

    private fun messageReceived(s: String){

        try {
            val obj = ignoreJsonFormat.decodeFromString<KarmaObject>(s)
            val type = obj.type

            val i = queue.indexOfFirst { it.type == type }
            if (i >= 0) {
                val p = queue.removeAt(i)
                println("Received reply for type ${p.type}: $s")
                p.promise.resolve(s)
            } else {
                handleIncomingMessage(s, type)
            }
        }catch (e: Exception){
            e.printStackTrace()
        }

    }

    var handler: ((String, String) -> String)? = null

    private fun handleIncomingMessage(s: String, type: String) {

        println("Received new request $s")
        if(handler != null && out != null){

            val res = handler!!(s, type)
            out!!.write(res + "\n")
            out!!.flush()
            println("Replied to request with $res")

        }else{
            println("No message handler set!!")
        }

    }

}