package storage

import Config
import com.toddway.shelf.FileStorage
import com.toddway.shelf.KotlinxSerializer
import com.toddway.shelf.Shelf
import kotlinx.serialization.InternalSerializationApi
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

    fun <T : Any> put(key: String, o: T) {
        db.item(key).put(o)
    }

    fun getHighestPossibleKarmaObject(key: String) : KarmaObject? {

        val base = get<KarmaObject>(key)
        return if(base != null){
            val type = typeMap[base.type]!!
            item(key).get(type)
        }else{
            null
        }

    }

    companion object {
        val typeMap = mapOf(
            "transaction" to Transaction::class, //TODO
            "block" to Block::class
        )
    }

}