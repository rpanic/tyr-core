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

data class GetObject(
    val objectid: String
) : KarmaObject("getobject")

data class IHaveObject(
    val objectid: String
) : KarmaObject("ihaveobject")

data class Object(
    val `object`: KarmaObject
) : KarmaObject("object")

@Serializable
data class Error(
    val error: String
) : KarmaObject("error")