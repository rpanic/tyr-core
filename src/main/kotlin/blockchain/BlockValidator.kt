package blockchain

import model.Transaction
import network.Block
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import storage.ObjectStorage
import utils.Sha256
import utils.fromHex
import utils.skip
import java.lang.Math.pow
import java.math.BigInteger
import kotlin.math.pow

class BlockValidator : KoinComponent {

    val db by inject<ObjectStorage>()
    val txvalidator by inject<TransactionValidator>()

    fun validateBlock(block: Block, utxoSet: UtxoSet, previous: Block?) : Boolean{

        //Validate Format
        val formatValid = VerifyChain("verifyBlock: Format", BlockValidation.VALID)
            .step(BlockValidation.INVALID_TIMESTAMP) {
                block.created > 0 && block.created < System.currentTimeMillis() / 1000
            }.step(BlockValidation.INVALID_FORMAT){
                block.nonce.length < 100 &&
                        block.nonce.fromHex() != null
            }.step(BlockValidation.INVALID_FORMAT){
                block.miner.length <= 128
            }.step(BlockValidation.INVALID_FORMAT){
                block.note.length <= 128
            }
            .verify()

        if(!formatValid) return false

        val txcache = mutableListOf<Transaction>()
        var coinbaseCache: Transaction? = null

        val valid = VerifyChain("verifyBlock: verify", BlockValidation.VALID)
            .step(BlockValidation.INVALID_TARGET) {
                block.T == Config.config.consensus.blockTarget
            }.step(BlockValidation.INVALID_POW){
                BigInteger(block.hash().fromHex()!!).compareTo(
                    BigInteger(block.T.fromHex())
                ) == -1
            }.step(BlockValidation.INVALID_PARENT){
                (previous != null && previous.hash() == block.previd)
                        || block.previd == null
            }.step(BlockValidation.TIMESTAMP_BEFORE_PREVIOUS) {
                block.created > (previous?.created ?: 0)
            }
            .step(BlockValidation.TX_DOWNLOAD_FAILED){

                val txs = block.txids.map {
                    var tx = db.get<Transaction>(it)
                    if(tx == null){

                        throw IllegalStateException("123")

                        //Get tx from peers
                        tx = Node.retrieveObjectFromPeers<Transaction>(it)

                        if(tx != null){
                            db.put(tx.hash(), tx)
                        }

                        tx
                    }else{
                        tx
                    }
                }
                if(txs.any { it == null }){
                    utils.error { "Block validation failed, one or more txs could not be retrieved" }
                    false
                }else{

                    txcache.addAll(txs.filterNotNull())

                    txs.all {
                        txvalidator.validate(it!!)
                    }
                }
            }.step(BlockValidation.INVALID_COINBASE) {

                val coinbases = txcache.filter { it.isCoinbase() }

                if(coinbases.size <= 1){   // only max. 1 coinbase
                    val coinbase = coinbases.firstOrNull()
                    if(coinbase != null){
                        coinbaseCache = coinbase

                        var valid = coinbase.hash() == block.txids[0] //Coinbase has to be at index 0

                        valid = valid && txcache.none { it.inputs?.any { i -> i.outpoint.txid == block.txids[0] } ?: false } //check that coinbase is not spent

                        valid && txvalidator.validateCoinbase(coinbase)
                    }else{
                        true
                    }
                }else false

            }.step(BlockValidation.INVALID_COINBASE2) {
                //Validate coinbase value
                if(coinbaseCache != null){

                    val tx = coinbaseCache!!

                    val txleftover = txcache.skip(if(coinbaseCache != null) 1 else 0).sumOf {
                        val inputs = it.inputs!!.sumOf { input ->
                            val utxo = utxoSet.getUtxos().find { it.txid == input.outpoint.txid && it.index == input.outpoint.index }
                            utxo?.value ?: return@step false
                        }
                        val outputs = it.outputs.sumOf { it.value }
                        inputs - outputs
                    }

                    tx.outputs[0].value <= txleftover + (50 * 10.0.pow(12.0).toLong())

                }else{
                    true
                }

            }.step(BlockValidation.TX_SIMULATION_FAILED) {

                if(coinbaseCache != null){
                    val o = coinbaseCache!!.outputs[0]
                    utxoSet.addCoinbase(
                        Utxo(
                            coinbaseCache!!.hash(),
                            0,
                            o.value,
                            1
                        )
                    )
                }

                txcache.skip(if(coinbaseCache != null) 1 else 0).all { tx ->
                    val inputs = tx.inputs!!.map { input ->
                        val utxo = utxoSet.getUtxos().find {
                            it.txid == input.outpoint.txid &&
                            it.index == input.outpoint.index
                        } ?: return@step false
                        utxo
                    }
                    val outputs = tx.outputs.mapIndexed { index, output ->
                        Utxo(
                            tx.hash(),
                            index,
                            output.value,
                            1
                        )
                    }
                    utxoSet.spend(inputs, outputs)
                }
            }
            .verify()

        return valid

    }

}

enum class BlockValidation {
    VALID,
    INVALID_FORMAT,
    INVALID_TIMESTAMP,
    INVALID_SIGNATURE,
    INVALID_TARGET,
    INVALID_POW,
    INVALID_UTXO,
    INVALID_COINBASE,
    INVALID_COINBASE2,
    TX_DOWNLOAD_FAILED,
    TX_SIMULATION_FAILED,
    TIMESTAMP_BEFORE_PREVIOUS,
    INVALID_PARENT
}