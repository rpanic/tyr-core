package storage

import network.Peer
import network.PeerPool
import java.lang.IllegalArgumentException
import java.net.InetAddress

class PeerPoolLoader {

    fun loadPeerList() : PeerPool {

        val file = Config.config.storage.getStorageDirectory().resolve("peers.txt")
        var peers = if(file.exists()){
            file.readLines().mapNotNull(Peer.Companion::fromString)
        }else{
            listOf()
        }
        if(peers.isEmpty()){
            val bootstrapPeer = Peer.fromString(Config.config.network.bootstrapNode)
                ?: throw IllegalArgumentException("Bootstrap peer could not be initialized")
            peers = listOf(bootstrapPeer)
        }

        return PeerPool(
            peers.filter { Config.config.network.enableLocalhost || it.ip.hostAddress !in PeerPool.PEER_BLACKLIST }
        )

    }

    fun storePeerList(pool: PeerPool){

        if(Config.config.storage.persistPeers) {
            val file = Config.config.storage.getStorageDirectory().resolve("peers.txt")
            file.writeText(pool.getPeers().joinToString("\n") { it.getAddress() })
        }

    }

}