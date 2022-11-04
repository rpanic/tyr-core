package storage

import org.koin.dsl.module

object StorageModule {

    val module = module {
        single {
            PeerPoolLoader()
        }
        single {
            get<PeerPoolLoader>().loadPeerList()
        }
        single {
            ObjectStorage()
        }

    }

}