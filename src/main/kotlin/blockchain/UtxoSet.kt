package blockchain

import kotlinx.serialization.Serializable

class UtxoSet constructor(val list: MutableList<Utxo>) {

    constructor() : this(mutableListOf()){
    }

//    var valid = true

    fun addCoinbase(outputs: Utxo) : Boolean {
        return list.add(outputs)
    }

    fun spend(spends: List<Utxo>, newOutputs: List<Utxo>) : Boolean{

//        if(!valid) return false

        if(!newOutputs.all { it.fromBlock >= 0 }) return false

        if(!list.containsAll(spends)) return false

        list.removeAll(spends)
        list.addAll(newOutputs)

        return true

    }

//    fun invalidOperation() : Boolean {
//        valid = false
//        return false
//    }

    fun totalSupply() : Long{
        return list.sumOf { it.value }
    }

    fun getUtxos() : List<Utxo>{
        return list.toList()
    }

    fun createNew() : UtxoSet{
        return UtxoSet(this.list.toMutableList())
    }

}

@Serializable
data class Utxo(
    val txid: String,
    val index: Int,
    val value: Long,
    val fromBlock: Long
){
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Utxo

        if (txid != other.txid) return false
        if (index != other.index) return false
        if (value != other.value) return false

        return true
    }
}