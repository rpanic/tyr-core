import org.koin.core.context.startKoin
import org.koin.dsl.module
import storage.StorageModule

object KoinSetup {

    fun setup(){

        startKoin {
            modules(StorageModule.module)

            modules(module {
                single { Config.config }
            })
        }

    }

}