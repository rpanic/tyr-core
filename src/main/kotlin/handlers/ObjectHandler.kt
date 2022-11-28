package handlers

import blockchain.BlockValidator
import blockchain.TransactionValidator
import blockchain.UtxoSet
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.*
import model.Transaction
import network.*
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import storage.ObjectStorage
import storage.ProcessedBlock
import utils.debug
import java.util.*

class ObjectHandler : Handler, KoinComponent {

    val storage by inject<ObjectStorage>()
    val txValidator by inject<TransactionValidator>()
    val blockValidator by inject<BlockValidator>()

    override fun routes(handler: MainHandler) = handler.apply {

        route<IHaveObject, GetObject?>("ihaveobject") {

            val t = storage.exists(it.objectid)
            if(t){

                GetObject(it.objectid)

            }else{
                null
            }
        }

        route<GetObject, JsonObject?>("getobject"){

            val o = storage.getHighestPossibleKarmaObject(it.objectid)
            if(o != null){
                val e = JsonObject(mapOf(
                    "type" to JsonPrimitive("object"),
                    "object" to o
                ))
                e
            }else{
                null
            }

        }

        route<Object, Error?>("object"){

            val obj = it.`object`

            var ret: Error? = null

            if(obj.type == "transaction"){

                val el = Json.parseToJsonElement(this.rawString) //TODO In storage class auslagern
                val txObj = el.jsonObject.get("object")!!
                val tx = Json.decodeFromJsonElement<Transaction>(txObj)
//                Json.decodeFromString<Transaction>(this.rawString)

                if(txValidator.validate(tx)){

                    if(!storage.exists(tx.hash())) {

                        storage.put(tx.hash(), tx)
                        debug("object" to tx.hash()) { "Stored new tx ${tx.hash()}, gossiping" }

                        Thread {
                            Node.broadcast(
                                Json.encodeToString(IHaveObject(tx.hash())),
                                listOf(this.conn.underlyingConnection.peer)
                            )
                        }.start()
                    }else{
                        debug("object" to tx.hash()) { "Object already in storage" }
                    }

                }else{
                    ret = Error("Tx Validation failed")
                }
            }

            else if(obj.type == "block"){

                val el = Json.parseToJsonElement(this.rawString) //TODO In storage class auslagern
                val blockObj = el.jsonObject.get("object")!!
                val block = Json.decodeFromJsonElement<Block>(blockObj)

                val blockQueue = LinkedList<Block>()

                blockQueue += block
                var currBlock = block

                while(currBlock.previd != null){

                    if(storage.exists(currBlock.previd!!)){
                        break
                    }else{
                        val b = Node.retrieveObjectFromPeers<Block>(currBlock.previd!!) ?: break
                        currBlock = b
                        blockQueue += b
                    }

                }
                blockQueue.reverse()

                val initialset = if(currBlock.previd == null){
                    ProcessedBlock(null, listOf(), 0)
                }else{
                    storage.get<ProcessedBlock>(blockQueue.first().previd + "_processed")!!
                }

                val finishedset = blockQueue.fold(initialset!! as ProcessedBlock?) { acc, b ->

                    if(acc == null) return@fold null

                    val set = UtxoSet(acc.utxoSet.toMutableList()).createNew()
                    if(blockValidator.validateBlock(b, set, acc.block)){
                        storage.put(b.hash(), b)
                        val pb = ProcessedBlock(b, set.getUtxos(), acc.height + 1)
                        storage.put(b.hash() + "_processed", pb)

                        debug("object" to b.hash()) { "Stored new block ${b.hash()} in storage" }

                        pb
                    }else{
                        ret = Error("Block Validation failed")
                        return@fold null
                    }
                }

                if(finishedset != null){

                    Thread {
                        Node.broadcast(
                            Json.encodeToString(IHaveObject(block.hash())),
                            listOf(this.conn.underlyingConnection.peer)
                        )
                    }.start()
                }

            }

            ret

        }

    }

}