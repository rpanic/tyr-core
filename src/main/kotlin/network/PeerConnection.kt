package network

import java.net.Socket

class PeerConnection(val peer: Peer) {

    var conn: MultiWayConnection = MultiWayConnection(this)

    var hello: Promise<Hello> = Promise()

    fun initOpening() : PromiseResult<Hello>?{
        return try{
            conn.init(openSocket())
            conn.setConnectionLost(::openSocket)
            conn.handler = Node.incomingHandler()
            println("Init")
            hello()
        }catch(e: Exception){
            println("Connection to peer $peer failed")
            null
        }
    }

    fun initWithSocket(s: Socket){
        conn.init(s)
        conn.setConnectionLost(::openSocket)
        conn.handler = Node.incomingHandler()
        hello()
        println("Init with incoming connection successful")
    }

    private fun openSocket() : Socket?{

        val socket = Socket(peer.ip, peer.port)
        if (socket.isConnected) {
            return socket
        } else {
            println("Failed to establish socket to $peer")
        }
        return null
    }

    private fun <T> peerConnectionBody(f: () -> PromiseResult<T>) : PromiseResult<T>{
        val res = f()
        if(res.isOk()){
            this.peer.seen()
        }
        return res
    }

    fun hello() : PromiseResult<Hello> = peerConnectionBody {

        val res = this.conn.sendMessage<Hello, Hello>(Hello("0.8.0", Config.CLIENT_VERSION), "hello")
        val hello = res.await()
        if(!this.hello.isResolved() && hello.isOk()){
            this.hello.resolve(hello.get())
        }

        hello

    }

    fun broadcast(msg: String) {

        this.conn.sendMessageNoResponse(msg)

    }

    fun getPeers() : PromiseResult<Peers> = peerConnectionBody{

        hello.await()

        val peers = this.conn.sendMessage<GetPeers, Peers>(GetPeers(), "peers")

        peers.await()

    }

}
