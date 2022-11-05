import handlers.MainHandler
import network.MultiWayConnection
import network.Peer
import network.PeerPool
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import utils.info
import java.net.ServerSocket
import java.net.Socket

object Node : KoinComponent {

    val peerPool: PeerPool by inject()

    val mainHandler = MainHandler()

    fun start(){

        info { "Starting Tyr-Core v${Config.CLIENT_VERSION}" }

        Thread {
            Thread.sleep(1000)

            peerPool.startRoutine()
        }.start()

        val socket = ServerSocket(18018)
        info { "Listening on port 18018..." }
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


}