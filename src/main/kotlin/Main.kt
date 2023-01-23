import network.PeerPool
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

fun main(){

    KoinSetup.setup()

    Main().start()

}

class Main : KoinComponent {

    val peerPool: PeerPool by inject()

    fun start(){

        Thread {
            Node.start()
        }.start()

//        val conn = peerPool.getPeers()[0].getPeerConnection()
//        val h = conn.hello()
//        println(h)

    }

}