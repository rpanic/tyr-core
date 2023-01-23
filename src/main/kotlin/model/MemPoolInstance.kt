package model

import blockchain.Blockchain
import blockchain.UtxoSet
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import storage.ObjectStorage
import storage.ProcessedBlock
import utils.info
import java.lang.Error

class MemPoolInstance : KoinComponent {

    val blockchain by inject<Blockchain>()
    val storage by inject<ObjectStorage>()

    private val txs = mutableSetOf<Transaction>()

    var state = UtxoSet()

    init {
        blockchain.longestTipListener += { b, length ->

            val pb = storage.get<ProcessedBlock>(b.hash() + "_processed")!!
            this.state = UtxoSet(pb.utxoSet.toMutableList())

            val newTxs = reapplyTransactions(txs, length)

            txs.clear()
            txs.addAll(newTxs)
        }
    }

    fun reapplyTransactions(tx: Set<Transaction>, height: Long) : List<Transaction>{

        val mapped = tx.filter {
            applyTransaction(it)
        }

        info("height" to height.toString()) { "Mempool: Threw out ${tx.size - mapped.size} transaction at height $height" }

        return mapped

    }

    fun applyTransaction(tx: Transaction) : Boolean{

        if(tx.isCoinbase()){
            throw Error("Cant add Coinbase to Mempool")
        }else{
            val (inputs, outputs) = tx.toUtxos(state) ?: return false
            return state.spend(inputs, outputs)
        }

    }

    fun add(tx: List<Transaction>) {

        tx.forEach {
            val (inputs, outputs) = it.toUtxos(state) ?: return@forEach
            if(state.spend(inputs, outputs)){
                txs.add(it)
            }
        }

    }

    fun getTxIds() = txs.map { it.hash() }

}