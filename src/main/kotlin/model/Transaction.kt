package model

import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import net.i2p.crypto.eddsa.EdDSAEngine
import network.KarmaObject
import utils.Sha256

@Serializable
data class Transaction(

    val inputs: List<TransactionInput>? = null,
    val outputs: List<TransactionOutput>,
    val height: Long? = null //only for coinbase tx

) : KarmaObject("transaction"){

    @Serializable
    data class TransactionInput(
        val outpoint: TransactionOutpoint,
        val sig: String
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

    fun json() = Json.encodeToString(this)

    fun hash() : String {

        return Sha256.hash(json())

    }

}