package model

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

}