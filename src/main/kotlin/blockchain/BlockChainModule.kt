package blockchain

import org.koin.dsl.module

object BlockChainModule {

    val module = module {
        single {
            TransactionValidator()
        }

    }
}