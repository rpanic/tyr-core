package blockchain

import model.Transaction
import net.i2p.crypto.eddsa.EdDSAEngine
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import storage.ObjectStorage
import utils.fromHex

class TransactionValidator : KoinComponent{

    val db by inject<ObjectStorage>()

    fun validateFormat(tx: Transaction) : Boolean{

        return VerifyChain()
            .step {
                tx.inputs != null
            }
            .step {
                tx.inputs!!.all { it.outpoint.index > 0 && it.sig.fromHex() != null && it.outpoint.txid.fromHex() != null }
            }.step {
                tx.outputs.all { it.pubkey.fromHex() != null }
            }
            .verify()

    }

    fun validate(tx: Transaction) : Boolean {

        val ecdsa = EdDSAEngine.getInstance(EdDSAEngine.SIGNATURE_ALGORITHM)

        //Coinbase tx
        if(tx.inputs == null && tx.outputs.isNotEmpty()){
            return true
        }

        if(!validateFormat(tx)) return false

        return VerifyChain().step {
            tx.inputs!!.all {
                val outpoint = db.get<Transaction>(it.outpoint.txid)
                if(outpoint != null){
                    outpoint.outputs.size > it.outpoint.index
                }else{
                    false
                }
            }
        }.step {
            tx.inputs!!.all { input ->
                input.sig.fromHex()?.let { ecdsa.verify(it) } ?: false
            }
        }.step {
            tx.outputs.all { output ->
                output.pubkey.fromHex() != null &&
                    output.value >= 0
            }
        } .step {
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

class VerifyChain(){

    val steps = mutableListOf<() -> Boolean>()

    fun step(f: () -> Boolean) : VerifyChain {
        steps += f
        return this
    }

    fun verify() : Boolean {
        for((index, step) in steps.withIndex()){
            val res = step()
            if(!res){
                println("Validation failed at step $index")
                return false
            }
        }
        return true
    }

}