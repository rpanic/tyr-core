package handlers

import blockchain.TransactionValidator
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import model.Transaction
import network.Error
import network.GetObject
import network.IHaveObject
import network.KarmaObject
import network.Object
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import storage.ObjectStorage
import utils.debug

class ObjectHandler : Handler, KoinComponent {

    val storage by inject<ObjectStorage>()
    val txValidator by inject<TransactionValidator>()

    override fun routes(handler: MainHandler) = handler.apply {

        route<IHaveObject, GetObject?>("ihaveobject") {

            val t = storage.get<KarmaObject>(it.objectid)
            if(t == null){

                GetObject(it.objectid)

            }else{
                null
            }
        }

        route<GetObject, Object?>("getobject"){

            val o = storage.getHighestPossibleKarmaObject(it.objectid)
            if(o != null){
                Object(o)
            }else{
                null
            }

        }

        route<Object, KarmaObject?>("object"){

            val obj = it.`object`

            var ret: KarmaObject? = null

            if(obj.type == "transaction"){
                val tx = Json.decodeFromString<Transaction>(this.rawString)

                if(txValidator.validate(tx)){

                    storage.put(tx.hash(), tx)
                    debug("object" to tx.hash()) { "Stored new tx ${tx.hash()}, gossiping" }

                    Thread {
                        Node.broadcast(Json.encodeToString(IHaveObject(tx.hash())), listOf(this.conn.underlyingConnection.peer))
                    }.start()

                }else{
                    ret = Error("Tx Validation failed")
                }
            }
            ret

        }

    }

}