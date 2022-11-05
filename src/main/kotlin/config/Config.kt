import com.sksamuel.hoplite.ConfigLoader
import java.io.File

data class Config (
    val network: NetworkConfig,
    val storage: StorageConfig,
    val logging: LoggingConfig
){
    companion object{

        val config: Config = ConfigLoader().loadConfigOrThrow<Config>(File(System.getProperty("user.dir") + File.separator + "config.yml"))

        val CLIENT_VERSION = "Tyr-Core 0.2"

    }
}

data class NetworkConfig (
    val bootstrapNode: String,
    val maxPeers: Int
)

data class StorageConfig (
    val storageDir: String
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