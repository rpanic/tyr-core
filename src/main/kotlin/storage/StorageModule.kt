package storage

import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

object StorageModule {

    val module = module {
        single {
            PeerPoolLoader()
        }
        single {
            get<PeerPoolLoader>().loadPeerList()
        }
    }

}