package handlers

import blockchain.TransactionValidator
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.*
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

            val t = storage.exists(it.objectid)
            if(t){

                GetObject(it.objectid)

            }else{
                null
            }
        }

        route<GetObject, JsonObject?>("getobject"){

            val o = storage.getHighestPossibleKarmaObject(it.objectid)
            if(o != null){
                val e = JsonObject(mapOf(
                    "type" to JsonPrimitive("object"),
                    "object" to o
                ))
                e
            }else{
                null
            }

        }

        route<Object, Error?>("object"){

            val obj = it.`object`

            var ret: Error? = null

            if(obj.type == "transaction"){

                val el = Json.parseToJsonElement(this.rawString) //TODO In storage class auslagern
                val txObj = el.jsonObject.get("object")!!
                val tx = Json.decodeFromJsonElement<Transaction>(txObj)
//                Json.decodeFromString<Transaction>(this.rawString)

                if(txValidator.validate(tx)){

                    if(!storage.exists(tx.hash())) {

                        storage.put(tx.hash(), tx)
                        debug("object" to tx.hash()) { "Stored new tx ${tx.hash()}, gossiping" }

                        Thread {
                            Node.broadcast(
                                Json.encodeToString(IHaveObject(tx.hash())),
                                listOf(this.conn.underlyingConnection.peer)
                            )
                        }.start()
                    }else{
                        debug("object" to tx.hash()) { "Object already in storage" }
                    }

                }else{
                    ret = Error("Tx Validation failed")
                }
            }
            ret

        }

    }

}