import com.sksamuel.hoplite.ConfigLoader
import network.Block
import java.io.File

data class Config (
    val network: NetworkConfig,
    val storage: StorageConfig,
    val logging: LoggingConfig,
    val consensus: ConsensusConfig,
){
    companion object{

        var config: Config = loadFromFile("config.yml")

        val CLIENT_VERSION = "Tyr-Core 0.5"

        fun loadFromFile(name: String) : Config {
            val c = ConfigLoader().loadConfigOrThrow<Config>(File(System.getProperty("user.dir") + File.separator + name))
            this.config = c
            c.consensus.genesisBlock = Block(
                T = "00000002af000000000000000000000000000000000000000000000000000000",
                created = 1624219079,
                miner = "dionyziz",
                nonce = "0000000000000000000000000000000000000000000000000000002634878840",
                note = "The Economist 2021-06-20: Crypto-miners are probably to blame for the graphics-chip shortage",
                previd = null,
                txids = listOf()
            )
            return c
        }

    }
}

data class ConsensusConfig(
    val blockTarget: String,
    var genesisBlock: Block? = null
)

data class NetworkConfig (
    val bootstrapNode: String,
    val maxPeers: Int,
    val bind: Int = 18018,
    val enableLocalhost: Boolean = false
)

data class StorageConfig (
    val storageDir: String,
    val persistPeers: Boolean = true
){
    fun getStorageDirectory() : File{
        val file = File(System.getProperty("user.dir") + "/$storageDir")
        if(!file.exists()){
            file.mkdir()
        }
        return file
    }
}

data class LoggingConfig(
    val json: Boolean = true
)