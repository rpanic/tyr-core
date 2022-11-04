import blockchain.BlockChainModule
import org.koin.core.context.startKoin
import org.koin.dsl.module
import storage.StorageModule
import utils.Sha256

object KoinSetup {

    fun setup(){

        startKoin {
            modules(StorageModule.module)

            modules(BlockChainModule.module)

            modules(module {
                single { Config.config }
            })
        }

    }

}