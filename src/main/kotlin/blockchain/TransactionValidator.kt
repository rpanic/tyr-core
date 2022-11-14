package blockchain

import cafe.cryptography.ed25519.Ed25519PublicKey
import cafe.cryptography.ed25519.Ed25519Signature
import model.Transaction
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import storage.ObjectStorage
import utils.fromHex
import java.nio.charset.Charset
import java.security.PublicKey

class TransactionValidator : KoinComponent{

    val db by inject<ObjectStorage>()

    fun validateFormat(tx: Transaction) : Boolean{

        return VerifyChain("verifyTxFormat")
            .step {
                tx.inputs != null
            }
            .step {
                tx.inputs!!.all { it.outpoint.index >= 0 && it.sig?.fromHex() != null && it.outpoint.txid.fromHex() != null }
            }.step {
                tx.outputs.all { it.pubkey.fromHex() != null }
            }
            .verify()

    }

    fun validate(tx: Transaction) : Boolean {

        //Coinbase tx
        if(tx.inputs == null && tx.outputs.isNotEmpty()){
            return true
        }

        if(!validateFormat(tx)) return false

        return VerifyChain("verifyTxContent").step {
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
                val output = db.get<Transaction>(input.outpoint.txid)!!.outputs[input.outpoint.index]
                val msg = tx.jsonWithoutSig()
                input.sig?.fromHex()?.let {

                    println(msg)

                    val pub = Ed25519PublicKey.fromByteArray(output.pubkey.fromHex()!!)
                    val sig = Ed25519Signature.fromByteArray(it)
                    println(pub.verify(msg.toByteArray(), sig))
                    pub.verify(msg.toByteArray(), sig)

                } ?: false
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

class VerifyChain(val name: String){

    val steps = mutableListOf<() -> Boolean>()

    fun step(f: () -> Boolean) : VerifyChain {
        steps += f
        return this
    }

    fun verify() : Boolean {
        for((index, step) in steps.withIndex()){
            val res = step()
            if(!res){
                utils.error { "Validation failed at step $index of verification $name" }
                return false
            }
        }
        return true
    }

}