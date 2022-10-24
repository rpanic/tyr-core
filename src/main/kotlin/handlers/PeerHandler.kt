package handlers

import network.GetPeers
import network.Peer
import network.PeerPool
import network.Peers
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class PeerHandler : Handler, KoinComponent {

    val peerPool: PeerPool by inject()

    override fun routes(handler: MainHandler) = handler.apply {

        route<GetPeers, Peers>("getpeers"){

            Peers(
                peerPool.getPeers().take(10).map(Peer::getAddress)
            )

        }

    }
}