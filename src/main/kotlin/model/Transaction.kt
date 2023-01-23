package model

import blockchain.Utxo
import blockchain.UtxoSet
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToJsonElement
import network.KarmaObject
import utils.Sha256
import utils.canonicalize

@Serializable
data class Transaction(

    val inputs: List<TransactionInput>? = null,
    val outputs: List<TransactionOutput>,
    val height: Long? = null //only for coinbase tx

) : KarmaObject("transaction"){

    @Serializable
    data class TransactionInput(
        val outpoint: TransactionOutpoint,
        var sig: String?
    ){
        @Serializable
        data class TransactionOutpoint(
            val txid: String,
            val index: Int
        )
    }

    @Serializable
    data class TransactionOutput(
        val pubkey: String,
        val value: Long
    )

    fun json() : String {
        val element = Json.encodeToJsonElement(this)
        val canonicalized = canonicalize(element)
        return Json.encodeToString(canonicalized)
    }

    fun hash() : String {

        return Sha256.hashText(json())

    }

    fun jsonWithoutSig() : String {
        val tx2 = this.copy(inputs = inputs?.map { TransactionInput(it.outpoint, null) })
        return tx2.json()
    }

    fun isCoinbase() : Boolean {
        return (inputs == null || inputs.isEmpty()) && (height != null && height >= 0)
    }


    fun toUtxos(utxoSet: UtxoSet) : Pair<List<Utxo>, List<Utxo>>? {

        val inputs = this.inputs!!.map { input ->
            val utxo = utxoSet.getUtxos().find {
                it.txid == input.outpoint.txid &&
                        it.index == input.outpoint.index
            } ?: return null
            utxo
        }
        val outputs = this.outputs.mapIndexed { index, output ->
            Utxo(
                this.hash(),
                index,
                output.value,
                1
            )
        }

        return inputs to outputs

    }

    override fun equals(other: Any?): Boolean {
        return if(other is Transaction)
            this.hash() == other.hash()
        else
            super.equals(other)
    }

}