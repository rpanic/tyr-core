import handlers.MainHandler
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import network.Hello
import network.PeerPool
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.net.ServerSocket
import java.net.Socket

object Node : KoinComponent {

    val peerPool: PeerPool by inject()

    val mainHandler = MainHandler()

    fun start(){

        Thread {
            Thread.sleep(1000)

            peerPool.startRoutine()
        }.start()

        val socket = ServerSocket(18018)
        println("Listing on port 18018...")
        while(!socket.isClosed){
            val conn = socket.accept()

            handleConnectionAsync(conn);

        }

    }

    fun handleConnectionAsync(conn: Socket){
        Thread {

            //Send Hello message -> Done in PeerConnection
//            val writer = conn.getOutputStream().writer()
//            writer.write(Json.encodeToString(Hello("0.8.0", Config.CLIENT_VERSION)) + "\n")
//            writer.flush()

            peerPool.newConnection(conn)

//            while(conn.isConnected){
//                val t = conn.getInputStream().reader().readText()
//                println("Received ss: $t")
//            }

        }.start()
    }

    fun incomingHandler() = { s: String, type: String ->
        mainHandler.handle(s, type)
    }


}