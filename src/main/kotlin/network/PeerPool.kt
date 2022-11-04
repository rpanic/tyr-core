package network

import Config
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import storage.PeerPoolLoader
import java.net.InetAddress
import java.net.Socket
import java.time.temporal.ChronoUnit
import java.util.concurrent.atomic.AtomicInteger

class PeerPool(val bootstrapPeers: List<Peer>) : KoinComponent {

    private val peers: MutableList<Peer> = mutableListOf()
    private val config: Config by inject()
    private val poolStorage: PeerPoolLoader by inject()

    val livenessTimeout = 3 * ChronoUnit.MINUTES.duration.toMillis()

    companion object {
        val PEER_BLACKLIST = listOf(
            "0.0.0.0",
            "1.1.1.1",
            "1.0.0.1",
            "8.8.8.8",
            "127.0.0.0",
            "127.0.0.1",
            "localhost"
        )
    }

    val peerQueue = bootstrapPeers.toMutableList()
    val activeAttempts = AtomicInteger(0)

    private fun addPeer(peer: Peer) : Boolean {
        if(peer.ip.hostAddress !in PEER_BLACKLIST){
            peerQueue += peer

            if(activeAttempts.get() < 5) {

                attemptToConnect(peer)

            }
            return true
        }
        return false
    }

    fun attemptToConnect(peer: Peer) {

        println("Attempting to connect to ${peer.getAddress()}")

        activeAttempts.incrementAndGet()

        peerQueue.remove(peer)

        Thread {

            peer.getOrOpenPeerConnection { res ->
                if (res != null && res.isOk()) {
                    peers += peer
                    println("Added peer ${peer.getAddress()}")
                } else {
                    println("Dropped peer ${peer.getAddress()} from queue")
                }
                activeAttempts.decrementAndGet()
            }


        }.start()
    }

    fun getPeers() : List<Peer>{
        return peers.filter(livenessFilter())
    }

    fun livenessFilter() : (Peer) -> Boolean{
        val liveThreshold = System.currentTimeMillis() - livenessTimeout
        return { it.lastSeen > liveThreshold }
    }

    fun newConnection(s: Socket){
        val address = s.inetAddress!!
        val peer = peers.toList().find { it.getPeerConnection()?.conn?.socket?.inetAddress == address }
        if(peer != null){
            //TODO Don´t know what to do then
            s.close()
        }else{
            val newPeer = Peer(address, s.port)
            newPeer.initWithConnection(s)
            addPeer(newPeer)
            println("Added receiving peer connection $newPeer")
        }
    }

    fun startRoutine(){
        Thread{

            while(true){

                try{

                    if(peers.isEmpty() && peerQueue.isEmpty()){
                        peerQueue.addAll(bootstrapPeers)
                    }

                    while(activeAttempts.get() < 5 && peerQueue.isNotEmpty() && peers.size < config.network.maxPeers){
                        attemptToConnect(peerQueue[0])
                    }

                    val active = peers.toList().count { it.isActive() }
                    println("Peers: ${peers.size} ($active active)")

                    //Add peers if necessary
                    val peers = this.getPeers()
                    if(peers.size < config.network.maxPeers){

                        val peer = peers.minByOrNull { it.lastAskedForPeers }
                        if(peer != null) {
                            val newPeers = peer.getOrOpenPeerConnection().getPeers()

                            if (newPeers.isOk()) {

                                newPeers.get().peers.filter { p ->
                                    peers.all { it.getAddress() !== p }
                                }.take(15).forEach {
                                    try {
                                        val newPeer = Peer.fromString(it)
                                        if (newPeer != null) {
                                            if(addPeer(newPeer))
                                                println("Added peer at ${newPeer.getAddress()} to queue")
                                            else
                                                println("Peer declined: ${newPeer.getAddress()}")
                                        } else {
                                            println("Couldn´t add peer $it to queue")
                                        }
                                    } catch (e: Exception) {
                                        System.err.println("Error while adding new peer: ")
                                        e.printStackTrace()
                                    }
                                }
                                peer.lastAskedForPeers = System.currentTimeMillis()
                            }
                        }
                    }

                    //Check peers for timeouts
    //                this.peers.sortedBy { it.lastSeen }.filterNot(livenessFilter()).take(1).forEach {
    //                    Thread {
    //                        try {
    //                            println("Hello check ${it.getAddress()}")
    //                            val hello = it.getPeerConnection().hello()
    //                            println("Hello check succeeded ${it.getAddress()}")
    //                        } catch (e: Exception) {
    //                            this.peers.remove(it)
    //                            println("Peer ${it.getAddress()} removed from pool")
    //                        }
    //                    }.start()
    //                }

                    poolStorage.storePeerList(this)
                }catch(e: Exception){
                    println("Peerlist exception, caught")
                    e.printStackTrace()
                }

                Thread.sleep(5000L)
            }
        }.start()
    }

}

data class Peer(
    val ip: InetAddress,
    val port: Int
){

    var lastSeen: Long = 0//System.currentTimeMillis()
    var lastAskedForPeers: Long = 0

    private var peerConnection: PeerConnection? = null

    override fun toString(): String {
        return "Peer(ip=$ip, port=$port, lastSeen=$lastSeen)"
    }

    fun initWithConnection(s: Socket) {
        if(peerConnection == null){
            val newCon = PeerConnection(this)
            newCon.initWithSocket(s)
            peerConnection = newCon
        }else{
            println("Error 36")
        }
    }

    fun getOrOpenPeerConnection(helloCallback: (PromiseResult<Hello>?) -> Unit = {}) : PeerConnection {
        if(peerConnection != null){
            helloCallback(null)
        }
        val con = peerConnection ?: run {
            val newCon = PeerConnection(this)
            peerConnection = newCon
            val helloRes = newCon.initOpening()
            helloCallback(helloRes)
            newCon
        }
        return con
    }

    fun getPeerConnection() : PeerConnection?{
        return peerConnection
    }

    fun isActive() : Boolean{
        return if(this.peerConnection != null){
            this.peerConnection!!.hello.isResolvedOk() && this.peerConnection!!.conn.isConnected()
        }else{
            false
        }
    }

    fun getAddress() : String = "${ip.hostAddress}:$port"

    fun seen(){
        this.lastSeen = System.currentTimeMillis()
    }

    companion object {
        fun fromString(address: String) : Peer? {
            val split = address.split(":")
            return if(split.size == 2){
                Peer(InetAddress.getByName(split[0]), split[1].trim().toInt())
            }else{
                null
            }
        }
    }

}