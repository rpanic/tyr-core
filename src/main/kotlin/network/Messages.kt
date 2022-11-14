package network

import kotlinx.serialization.Serializable

@Serializable
open class KarmaObject(
    val type: String
)

@Serializable
data class Hello(
    val version: String,
    val agent: String
) : KarmaObject("hello")

@Serializable
class GetPeers : KarmaObject("getpeers")

@Serializable
data class Peers(
    val peers: List<String>
) : KarmaObject("peers")

@Serializable
data class GetObject(
    val objectid: String
) : KarmaObject("getobject")

@Serializable
data class IHaveObject(
    val objectid: String
) : KarmaObject("ihaveobject")

@Serializable
data class Object(
    val `object`: KarmaObject
) : KarmaObject("object")

@Serializable
data class TypedObject<T>(
    val `object`: T
) : KarmaObject("object")

@Serializable
data class Error(
    val error: String
) : KarmaObject("error")


@Serializable
data class Block(

    val txids: List<String>,
    val nonce: String,
    val previd: String?,
    val created: Long,
    val T: String,
    val miner: String,
    val note: String

) : KarmaObject("block"){

}






















