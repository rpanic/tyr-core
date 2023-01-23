package model

import blockchain.BlockValidator
import blockchain.Blockchain
import blockchain.TransactionValidator
import blockchain.UtxoSet
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import network.Block
import network.Error
import network.IHaveObject
import network.Peer
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import storage.ObjectStorage
import storage.ProcessedBlock
import utils.debug
import java.util.*

class BlockSyncer : KoinComponent {

    val storage by inject<ObjectStorage>()
    val blockValidator by inject<BlockValidator>()
    val blockchain by inject<Blockchain>()

    fun downloadBlockRecursive(block: Block, peer: Peer): Error? {

        var ret: Error? = null

        val blockQueue = LinkedList<Block>()

        blockQueue += block
        var currBlock = block

        while (currBlock.previd != null) {

            if (storage.exists(currBlock.previd!!)) {
                break
            } else {
                val b = Node.retrieveObjectFromPeers<Block>(currBlock.previd!!) ?: break
                currBlock = b
                blockQueue += b
            }

        }
        blockQueue.reverse()

        val initialset = if (currBlock.previd == null) {
            ProcessedBlock(null, listOf(), -1)
        } else {
            storage.get<ProcessedBlock>(blockQueue.first().previd + "_processed")!!
        }

        val finishedset = blockQueue.fold(initialset as ProcessedBlock?) { acc, b ->

            if (acc == null) return@fold null

            val set = UtxoSet(acc.utxoSet.toMutableList()).createNew()
            if (blockValidator.validateBlock(b, set, acc.block, acc.height)) {
                storage.put(b.hash(), b)
                val pb = ProcessedBlock(b, set.getUtxos(), acc.height + 1)
                storage.put(b.hash() + "_processed", pb)
                blockchain.addBlock(b, acc.height + 1)

                debug("object" to b.hash()) { "Stored new block ${b.hash()} in storage" }

                pb
            } else {
                ret = Error("Block Validation failed")
                debug { "Block Validation failed" }
                return@fold null
            }
        }

        if (finishedset != null) {

            Thread {
                Node.broadcast(
                    Json.encodeToString(IHaveObject(block.hash())),
                    listOf(peer)
                )
            }.start()
        }

        return ret

    }

}