package blockchain

import kotlinx.serialization.Serializable
import network.Block
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import storage.ObjectStorage

class Blockchain(
    var longestTip: Block,
    var longestTipHeight: Long
) : KoinComponent {

    val db by inject<ObjectStorage>()

    fun addBlock(block: Block, height: Long){
        if(height > longestTipHeight){
            longestTip = block
            longestTipHeight = height
            changed()
        }
    }

    fun changed(){
        db.put(DB_KEY, LongestChainTip(longestTipHeight, longestTip.hash()))
    }

    fun getChainTip() = longestTip

    companion object {

        const val DB_KEY = "longestChainTip"

    }

}

@Serializable
data class LongestChainTip(val height: Long, val hash: String)