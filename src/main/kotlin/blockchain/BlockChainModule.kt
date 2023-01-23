package blockchain

import model.BlockSyncer
import model.MemPoolInstance
import network.Block
import org.koin.dsl.module
import storage.ObjectStorage

object BlockChainModule {

    val module = module {
        single {
            val db = this.get<ObjectStorage>()
            val tip = db.get<LongestChainTip>(Blockchain.DB_KEY)
            return@single if(tip != null){
                val block = db.get<Block>(tip.hash)!!
                Blockchain(block, tip.height)
            }else{
                val genesis = Config.config.consensus.genesisBlock!!
                Blockchain(genesis, 0)
            }
        }
        single {
            TransactionValidator()
        }
        single{
            BlockValidator()
        }

        single{
            BlockSyncer()
        }

        single {
            MemPoolInstance()
        }

    }
}