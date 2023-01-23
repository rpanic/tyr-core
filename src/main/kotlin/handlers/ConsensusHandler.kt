package handlers

import blockchain.Blockchain
import blockchain.TransactionValidator
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.jsonObject
import model.MemPoolInstance
import model.Transaction
import network.*
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import storage.ObjectStorage
import kotlin.Error

class ConsensusHandler : Handler, KoinComponent {

    val blockchain by inject<Blockchain>()
    val storage by inject<ObjectStorage>()
    val mempool by inject<MemPoolInstance>()
    val txValidator by inject<TransactionValidator>()

    override fun routes(handler: MainHandler) = handler.apply {

        route<GetChainTip, ChainTip>("getchaintip") {
            ChainTip(blockchain.getChainTip().hash())
        }

        route<GetMempool, Mempool?>("getmempool") {

//            val json = Json.parseToJsonElement(this.rawString)

//            if(json.jsonObject.containsKey("txids")){
//                val mempool = Json.decodeFromString<Mempool>("")
//
//                //TODO
//
//                null
//            }
//            else{
//
//            }
            Mempool(mempool.getTxIds().toList())

        }

        route<Mempool, Error?>("mempool") {

            val ret: Error? = null

            val txids = it.txids.distinct()

            addTxids(storage, txValidator, mempool, txids, this.conn)

            ret

        }

    }

    companion object {

        fun addTxids(storage: ObjectStorage, txValidator: TransactionValidator, mempool: MemPoolInstance, txids: List<String>, conn: MultiWayConnection) {

            val eligible = txids.mapNotNull { txid ->
                if(!storage.exists(txid)){

                    val p = conn.sendMessage(Json.encodeToString(GetObject(txid)), "object")
                    val res = p.await()
                    if(res.isOk()){

                        val el = Json.parseToJsonElement(res.get())
                        val txObj = el.jsonObject.get("object")!!
                        val tx = Json.decodeFromJsonElement<Transaction>(txObj)
                        if(txValidator.validate(tx)){
                            storage.put(tx.hash(), tx)
                            tx
                        }else null

                    } else null

                } else {
                    storage.get<Transaction>(txid)
                }
            }

            println("Adding ${eligible.size} txs to mempool")

            mempool.add(eligible)
        }

    }


}