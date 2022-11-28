package blockchain

import cafe.cryptography.ed25519.Ed25519PublicKey
import cafe.cryptography.ed25519.Ed25519Signature
import model.Transaction
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import storage.ObjectStorage
import utils.fromHex

class TransactionValidator : KoinComponent{

    val db by inject<ObjectStorage>()

    fun validateFormat(tx: Transaction) : Boolean{

        return VerifyChain("verifyTxFormat", TxValidity.VALID)
            .step(TxValidity.INVALID_FORMAT) {
                tx.inputs != null
            }
            .step(TxValidity.NO_OUTPUTS){
                tx.outputs.isNotEmpty()
            }
            .step(TxValidity.INVALID_FORMAT) {
                tx.inputs!!.all { it.outpoint.index >= 0 && it.sig?.fromHex() != null && it.outpoint.txid.fromHex() != null }
            }.step(TxValidity.INVALID_FORMAT) {
                tx.outputs.all { it.pubkey.fromHex() != null }
            }
            .verify()

    }

    fun validateCoinbase(tx: Transaction) : Boolean{
        return VerifyChain("verifyCoinbase", TxValidity.VALID)
            .step(TxValidity.COINBASE_MULTIPLE_OUTPUTS) {
                tx.outputs.size == 1
            }.step(TxValidity.COINBASE_MULTIPLE_OUTPUTS) {
                tx.inputs == null || tx.inputs.isEmpty()
            }.step(TxValidity.COINBASE_INDEX_NEGATIVE) {
                tx.height != null && tx.height >= 0
            }
            .step(TxValidity.INVALID_FORMAT){
                tx.outputs[0].pubkey.fromHex() != null
            }
            .verify()
    }

    fun validate(tx: Transaction) : Boolean {

        //Coinbase tx
        if((tx.inputs == null || tx.inputs.isEmpty()) && tx.outputs.isNotEmpty()){
            return validateCoinbase(tx)
        }

        if(!validateFormat(tx)) return false

        return VerifyChain<TxValidity>("verifyTxContent", TxValidity.VALID).step(TxValidity.OUTPOINT_INDEX_TOO_BIG) {
            tx.inputs!!.all {
                val outpoint = db.get<Transaction>(it.outpoint.txid)
                if(outpoint != null){
                    outpoint.outputs.size > it.outpoint.index
                }else{
                    false
                }
            }
        }.step(TxValidity.INVALID_SIGNATURE) {
            tx.inputs!!.all { input ->
                val output = db.get<Transaction>(input.outpoint.txid)!!.outputs[input.outpoint.index]
                val msg = tx.jsonWithoutSig()
                input.sig?.fromHex()?.let {

                    val pub = Ed25519PublicKey.fromByteArray(output.pubkey.fromHex()!!)
                    val sig = Ed25519Signature.fromByteArray(it)
                    pub.verify(msg.toByteArray(), sig)

                } ?: false
            }
        }.step(TxValidity.INVALID_FORMAT) {
            tx.outputs.all { output ->
                output.pubkey.fromHex() != null &&
                    output.value >= 0
            }
        }.step(TxValidity.DOUBLE_INPUT){

            val l = tx.inputs!!.map { it.outpoint.txid + it.outpoint.index }
            l.size == l.distinct().size

        }.step(TxValidity.OUTPUTS_GREATER_THAN_INPUTS) {
            val inputSum = tx.inputs!!.sumOf {
                val outpoint = db.get<Transaction>(it.outpoint.txid)
                outpoint!!.outputs[it.outpoint.index].value
            }
            val outputSum = tx.outputs.sumOf { it.value }
            outputSum <= inputSum
        }
        .verify()

    }

}

enum class TxValidity {
    VALID,
    OUTPOINT_INDEX_TOO_BIG,
    INVALID_SIGNATURE,
    INVALID_FORMAT,
    OUTPUTS_GREATER_THAN_INPUTS,
    NO_OUTPUTS,
    DOUBLE_INPUT,

    COINBASE_MULTIPLE_OUTPUTS,
    COINBASE_INDEX_NEGATIVE
    ;

}

class VerifyChain<T: Enum<T>>(val name: String, val validValue: T){

    val steps = mutableListOf<() -> T>()

    fun step(f: () -> T) : VerifyChain<T> {
        steps += f
        return this
    }

    fun step(errorCode: T, f: () -> Boolean) : VerifyChain<T> {
        steps += { if(f()) validValue else errorCode }
        return this
    }

    fun verify() : Boolean {
        for((index, step) in steps.withIndex()){
            try {
                val res = step()
                if (res != validValue) {
                    utils.error { "Validation failed at step $index of verification $name: ${res.name}" }
                    return false
                }
            }catch (e: Exception){
                utils.error { (e.javaClass.simpleName + ": " + e.message) }
//                e.printStackTrace()
                return false
            }
        }
        return true
    }

}