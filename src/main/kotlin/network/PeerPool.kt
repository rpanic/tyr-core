package network

import Config
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import storage.PeerPoolLoader
import java.net.InetAddress
import java.net.Socket
import java.time.temporal.ChronoUnit

class PeerPool(bootstrapPeers: List<Peer>) : KoinComponent {

    private val peers: MutableList<Peer> = bootstrapPeers.toMutableList()
    private val config: Config by inject()
    private val poolStorage: PeerPoolLoader by inject()

    val livenessTimeout = 3 * ChronoUnit.MINUTES.duration.toMillis()

    private fun addPeer(peer: Peer) {
        peers += peer
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
        val peer = peers.toList().find { it.getPeerConnection(openIfNecessary = false).conn.socket?.inetAddress == address }
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

                    peers.toList().forEach {
                        it.getPeerConnection() //Initializes peer if not yet
                    }

                    val active = peers.toList().count { it.isActive() }
                    println("Peers: ${peers.size} ($active active)")

                    //Add peers if necessary
                    val peers = this.getPeers()
                    if(peers.size < config.network.maxPeers){

                        val peer = peers.minBy { it.lastAskedForPeers }
                        val newPeers = peer.getPeerConnection().getPeers()

                        if(newPeers.isOk()) {

                            newPeers.get().peers.filter { p ->
                                peers.all { it.getAddress() !== p }
                            }.forEach {
                                try {
                                    val newPeer = Peer.fromString(it)
                                    if (newPeer != null) {
                                        addPeer(newPeer)
                                        println("Added peer at ${newPeer.getAddress()}")
                                    } else {
                                        println("Couldn´t add peer $it")
                                    }
                                }catch (e: Exception){
                                    System.err.println("Error while adding new peer: ")
                                    e.printStackTrace()
                                }
                            }
                            peer.lastAskedForPeers = System.currentTimeMillis()
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

    fun getPeerConnection(openIfNecessary: Boolean = true) : PeerConnection {
        val con = peerConnection ?: run {
            val newCon = PeerConnection(this)
            peerConnection = newCon
            newCon.initOpening()
            newCon
        }
        return con
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