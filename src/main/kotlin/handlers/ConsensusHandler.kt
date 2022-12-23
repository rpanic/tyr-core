package handlers

import blockchain.Blockchain
import network.ChainTip
import network.GetChainTip
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class ConsensusHandler : Handler, KoinComponent {

    val blockchain by inject<Blockchain>()

    override fun routes(handler: MainHandler) = handler.apply {

        route<GetChainTip, ChainTip>("ihaveobject") {
            ChainTip(blockchain.getChainTip().hash())
        }

    }


}