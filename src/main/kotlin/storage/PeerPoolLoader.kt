package storage

import network.Peer
import network.PeerPool
import java.lang.IllegalArgumentException
import java.net.InetAddress

class PeerPoolLoader {

    fun loadPeerList() : PeerPool {

        val file = Config.config.storage.getStorageDirectory().resolve("peers.txt")
        val peers = if(file.exists()){
            file.readLines().mapNotNull(Peer.Companion::fromString)
        }else{
            val bootstrapPeer = Peer.fromString(Config.config.network.bootstrapNode)
                ?: throw IllegalArgumentException("Bootstrap peer could not be initialized")
            listOf(bootstrapPeer)
        }
        return PeerPool(peers)

    }

    fun storePeerList(pool: PeerPool){

        val file = Config.config.storage.getStorageDirectory().resolve("peers.txt")
        file.writeText(pool.getPeers().joinToString("\n") { it.getAddress() })

    }

}