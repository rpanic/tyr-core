import com.sksamuel.hoplite.ConfigLoader
import java.io.File

data class Config (
    val network: NetworkConfig,
    val storage: StorageConfig,
    val logging: LoggingConfig
){
    companion object{

        var config: Config = loadFromFile("config.yml")

        val CLIENT_VERSION = "Tyr-Core 0.2"

        fun loadFromFile(name: String) : Config {
            val c = ConfigLoader().loadConfigOrThrow<Config>(File(System.getProperty("user.dir") + File.separator + name))
            this.config = c
            return c
        }

    }
}

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