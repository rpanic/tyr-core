package storage

import Config
import blockchain.Utxo
import com.toddway.shelf.FileStorage
import com.toddway.shelf.KotlinxSerializer
import com.toddway.shelf.Shelf
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.*
import model.Transaction
import network.Block
import network.KarmaObject

@OptIn(InternalSerializationApi::class)
class ObjectStorage {

    private val db: Shelf =
        Shelf(FileStorage(Config.config.storage.getStorageDirectory().resolve("db")), KotlinxSerializer())

    fun item(key: String) = db.item(key)

    inline fun <reified T : Any> get(key: String) : T? {
        return item(key).get(T::class)
    }

    fun exists(key: String) = item(key).getRaw() != null

    fun <T : Any> put(key: String, o: T) {
        db.item(key).put(o)
    }

    fun getHighestPossibleKarmaObject(key: String) : JsonElement? {

        val raw = item(key).getRaw()
        return if(raw != null) {
            val jsonElement = Json.parseToJsonElement(raw)
            jsonElement
//            println(raw)
//            println("123 ${jsonElement.jsonObject.get("type")!!.jsonPrimitive.content}")
//            val type = typeMap[jsonElement.jsonObject.get("type")!!.jsonPrimitive.content]!!
//            item(key).get(type)
        }else{
            null
        }

    }

    companion object {
        val typeMap = mapOf(
            "transaction" to Transaction::class,
            "block" to Block::class
        )
    }

}

@Serializable
data class ProcessedBlock(
    val block: Block?,
    val utxoSet: List<Utxo>,
    val height: Long
)