import blockchain.TransactionValidator
import handlers.ConsensusHandler
import handlers.MainHandler
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.jsonObject
import model.BlockSyncer
import model.MemPoolInstance
import network.*
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import storage.ObjectStorage
import utils.debug
import utils.info
import java.net.ServerSocket
import java.net.Socket
import kotlin.reflect.KClass

object Node : KoinComponent {

    val peerPool: PeerPool by inject()
    val syncer by inject<BlockSyncer>()
    val storage by inject<ObjectStorage>()
    val txValidator by inject<TransactionValidator>()
    val memPool by inject<MemPoolInstance>()

    val mainHandler = MainHandler()

    fun start(port: Int = Config.config.network.bind){

        info { "Starting Tyr-Core v${Config.CLIENT_VERSION}" }

        init()

        Thread {
            Thread.sleep(1000)

            peerPool.startRoutine()
        }.start()

        val socket = ServerSocket(port)
        info { "Listening on port $port..." }
        while(!socket.isClosed){
            val conn = socket.accept()

            handleConnectionAsync(conn);

        }

    }

    fun init(){

        //Syncing Chaintip
        peerPool.newPeerListener += {

            val p = it.getPeerConnection()!!.conn.sendMessage<GetChainTip, ChainTip>(GetChainTip(), "chaintip")
            val res = p.await()
            if(res.isOk()){
                val chainTip = res.get()

                if(!storage.exists(chainTip.blockid)){

                    //Get Block data from peer
                    val p2 = it.getPeerConnection()!!.conn.sendMessage(Json.encodeToString(GetObject(chainTip.blockid)), "object")
                    val res2 = p2.await()
                    if(res2.isOk()){

                        val el = Json.parseToJsonElement(res2.get())
                        val blockObj = el.jsonObject.get("object")!!
                        val block = Json.decodeFromJsonElement<Block>(blockObj)

                        syncer.downloadBlockRecursive(block, it)

                    }

                }
            }
        }

        //Retrieving mempool
        peerPool.newPeerListener += {

            debug { "Retrieving mempool from ${it.getAddress()}" }

            val p = it.getPeerConnection()!!.conn.sendMessage<GetMempool, Mempool>(GetMempool(), "mempool")
            val res = p.await()
            if(res.isOk()){

                ConsensusHandler.addTxids(
                    storage, txValidator, memPool,
                    res.get().txids,
                    it.getPeerConnection()!!.conn
                )
            }
        }

    }

    fun handleConnectionAsync(conn: Socket){
        Thread {

            peerPool.newConnection(conn)

        }.start()
    }

    fun incomingHandler() = { conn: MultiWayConnection, s: String, type: String ->
        mainHandler.handle(conn, s, type)
    }

    fun broadcast(msg: String, excludes: List<Peer>){
        peerPool.getPeers().filter { it !in excludes }.forEach {
            it.getPeerConnection()?.broadcast(msg)
        }
    }

    inline fun <reified T : Any> retrieveObjectFromPeers(objectId: String) : T?{

        debug { "Retrieving $objectId" }

        val all = peerPool.getPeers().mapNotNull {
            it.getPeerConnection()?.getObject(objectId)
        }

        val s = Promise.awaitFirst(all)
        return if(s != null){

            val jsonobject = Json.parseToJsonElement(s)
            val o = jsonobject.jsonObject.get("object")!!

            Json.decodeFromJsonElement<T>(o)

//            Json.decodeFromString<T>(s)

        }else{
            null
        }

    }


}