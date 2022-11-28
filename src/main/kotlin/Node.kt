import handlers.MainHandler
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import network.MultiWayConnection
import network.Peer
import network.PeerPool
import network.Promise
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import utils.info
import java.net.ServerSocket
import java.net.Socket
import kotlin.reflect.KClass

object Node : KoinComponent {

    val peerPool: PeerPool by inject()

    val mainHandler = MainHandler()

    fun start(port: Int = Config.config.network.bind){

        info { "Starting Tyr-Core v${Config.CLIENT_VERSION}" }

        Thread {
            Thread.sleep(1000)

            peerPool.startRoutine()
        }.start()

        val socket = ServerSocket(port)
        info { "Listening on port $port..." }
        while(!socket.isClosed){
            val conn = socket.accept()

            handleConnectionAsync(conn);

        }

    }

    fun handleConnectionAsync(conn: Socket){
        Thread {

            peerPool.newConnection(conn)

        }.start()
    }

    fun incomingHandler() = { conn: MultiWayConnection, s: String, type: String ->
        mainHandler.handle(conn, s, type)
    }

    fun broadcast(msg: String, excludes: List<Peer>){
        peerPool.getPeers().filter { it !in excludes }.forEach {
            it.getPeerConnection()?.broadcast(msg)
        }
    }

    inline fun <reified T : Any> retrieveObjectFromPeers(objectId: String) : T?{

        val all = peerPool.getPeers().mapNotNull {
            it.getPeerConnection()?.getObject(objectId)
        }

        val s = Promise.awaitFirst(all)
        return if(s != null){

            Json.decodeFromString<T>(s)

        }else{
            null
        }

    }


}