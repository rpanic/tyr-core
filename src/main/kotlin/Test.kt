import blockchain.BlockValidator
import blockchain.TransactionValidator
import blockchain.UtxoSet
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import model.Transaction
import network.Block
import network.KarmaObject
import network.Object
import storage.ProcessedBlock
import utils.info


fun testNodeRunning(){

    Config.loadFromFile("config_local.yml")

    println(Config.config.network.bootstrapNode)

    Thread {
        Node.start(18017)
    }.start()

    Thread.sleep(5000L)


    println("Checkpoint")
    val peer = Node.peerPool.getPeers().find { it.getAddress() == "127.0.0.1:18018" }
    val tx = Transaction(
        height = 1,
//        inputs = listOf(),
        outputs = listOf(
            Transaction.TransactionOutput(
                pubkey = "62b7c521cd9211579cf70fd4099315643767b96711febaa5c76dc3daf27c281d",
                value = 50000000000000
        )
        )
    )

    val o = network.TypedObject<Transaction>(
        `object` = tx
    )

    val p = peer!!.getPeerConnection()!!.conn.sendMessage<network.TypedObject<Transaction>, KarmaObject>(o, "ihaveobject")
    val s = p.await()
    println(s.isOk())
    println(s.get().toString())

}

fun test2(){

    val block = Block(
        listOf("1bb37b637d07100cd26fc063dfd4c39a7931cc88dae3417871219715a5e374af"),
        "200000000000000000000000000000000000000000000000000000000e762cb9",
        null,
        1624229079,
        T = "00000002af000000000000000000000000000000000000000000000000000000",
        miner = "TUWien−Kerma",
        note = " First block . Yayy, I have 50 ker now!!"
    )

    val tx = Transaction(
        height = 0,
        outputs = listOf(
            Transaction.TransactionOutput(
                pubkey = "8dbcd2401c89c04d6e53c81c90aa0b551cc8fc47c0469217c8f5cfbae1e911f9",
                50000000000
            )
        )
    )

    val b = BlockValidator()
    b.db.put(tx.hash(), tx)

    val v = b.validateBlock(block, UtxoSet(), null, -1)
    println(v)


}

fun main(){

//    println(Json.encodeToString(Hello("0.7.0", "client") as KarmaObject))

    KoinSetup.setup()

    println(Config.config.consensus.genesisBlock!!.hash())
//    return

//    testNodeRunning()
//    test2()

    val txsjson = listOf(
        "{\"height\":1,\"outputs\":[{\"pubkey\":\"62b7c521cd9211579cf70fd4099315643767b96711febaa5c76dc3daf27c281c\",\"value\":50000000000000}],\"type\":\"transaction\"}",
        "{\"inputs\":[{\"outpoint\":{\"index\":0,\"txid\":\"48c2ae2fbb4dead4bcc5801f6eaa9a350123a43750d22d05c53802b69c7cd9fb\"},\"sig\":\"d51e82d5c121c5db21c83404aaa3f591f2099bccf731208c4b0b676308be1f994882f9d991c0ebfd8fdecc90a4aec6165fc3440ade9c83b043cba95b2bba1d0a\"}],\"outputs\":[{\"pubkey\":\"228ee807767047682e9a556ad1ed78dff8d7edf4bc2a5f4fa02e4634cfcad7e0\",\"value\":49000000000000}],\"type\":\"transaction\"}",
        "{\"height\":0,\"outputs\":[{\"pubkey\":\"8bd22d5b544887762cd6104b433d93e1f9a5f451fe47d641733e517d9551ab05\",\"value\":50}],\"type\":\"transaction\"}",
        "{\"inputs\":[{\"outpoint\":{\"index\":0,\"txid\":\"29e793963f3933af943d20cb3b8da893488c2e4fd169d88fd47c8081a368794c\"},\"sig\":\"648d0db001fe44edda0493b9635a9747b5f1b7e4b90032c17a3abc2aa874f4e2a0090ad7f43d1ce22614a52f6a13797dc2908e602a1c46c8cf32ec2ad910600b\"}],\"outputs\":[{\"pubkey\":\"8bd22d5b544887762cd6104b433d93e1f9a5f451fe47d641733e517d9551ab05\",\"value\":51}],\"type\":\"transaction\"}",
        "{\"height\":0,\"outputs\":[{\"pubkey\":\"bb2e6868eb399a530c2dbcde6727d8f4d7ccb052c7ed20488322ee5fef4c65e2\",\"value\":50}],\"type\":\"transaction\"}",
        "{\"inputs\":[{\"outpoint\":{\"index\":1,\"txid\":\"f76875e504b72322063a0c5ddff2d1c91a82665b33070eba257737c43a4e428e\"},\"sig\":\"996fb8d366b066d129f6fb5ef14ccd9a882952c47d90e58bf5b7ff397d072a6ff905380afcc541d60f8382dc226de3f3ec09250e6894e1a67fb8317257c68e0f\"}],\"outputs\":[{\"pubkey\":\"bb2e6868eb399a530c2dbcde6727d8f4d7ccb052c7ed20488322ee5fef4c65e2\",\"value\":50}],\"type\":\"transaction\"}",
        "{\"height\":0,\"outputs\":[{\"pubkey\":\"7d1b8f51c35cb8ecddc8c671138499bc8f37421a8906becff5cd318cf088b041\",\"value\":50}],\"type\":\"transaction\"}",
        "{\"inputs\":[{\"outpoint\":{\"index\":0,\"txid\":\"79f9b136a706e2a82a44b18e8ddc7e2e67757ea942bd2851c09314f3ddcdbe19\"},\"sig\":\"44779f8ef39b5e0bfd0a197e17e9a6734c22a512c8d8dc9b4a39b4451b8af5bce08c91d9be849c8cb6c470362fad240b95c6378a875d81559d2291b372742f0f\"}],\"outputs\":[],\"type\":\"transaction\"}",
        "{\"height\":0,\"outputs\":[{\"pubkey\":\"06abdd0a320df08c1e9d00e60c57409b1a6754428806a6d5d7f855885c48d540\",\"value\":50}],\"type\":\"transaction\"}",
        "{\"height\":0,\"outputs\":[{\"pubkey\":\"1d917705041ac7765510872543dee033fe5fe8a652854d7f3e62c65b538ad6aa\",\"value\":50}],\"type\":\"transaction\"}",
        "{\"inputs\":[{\"outpoint\":{\"index\":0,\"txid\":\"ce175f46aeca15cbb80b66d261519f223d992e23dd268721f4135b58f4497a2e\"},\"sig\":\"cbd77bf5cdb2a252aea728d776adab2029a2cc375def7f3196fe423111b9374a71cb6707bbfb3bf6833db4218426f8b9f32a5708cad36476decb6da565209d05\"},{\"outpoint\":{\"index\":0,\"txid\":\"b5770e009b63dc8d9aa3334dfe913ac098738dc02e4b379f96e663ec86aa81eb\"},\"sig\":\"3833f90108952068758a30e287d026e7b74d844026333c904d62dd92351330002996e947c0178f9c2248a1dc1d711101df3a385af15e78afdacd723202f2be0c\"}],\"outputs\":[{\"pubkey\":\"06abdd0a320df08c1e9d00e60c57409b1a6754428806a6d5d7f855885c48d540\",\"value\":20}],\"type\":\"transaction\"}",
        "{\"height\":0,\"outputs\":[{\"pubkey\":\"5e3f197d8b63a853b79330d96a82d2ad51ff06605879f5f4b664c1d9a6b9c02e\",\"value\":50}],\"type\":\"transaction\"}",
        "{\"inputs\":[{\"outpoint\":{\"index\":0,\"txid\":\"8a6fdba6541db169d9bb3249e91bfde5692048818031aa55062a41c673bd01ea\"},\"sig\":\"6f3d4522bf29e55becb3ce707830f62954cb109464737ea37bd0213f14ea52b530fe0c13d56c9496fa72466cbd94fb0a927e53a57786bca2dfa7214c89580403\"}],\"outputs\":[{\"pubkey\":\"5e3f197d8b63a853b79330d96a82d2ad51ff06605879f5f4b664c1d9a6b9c02e\",\"value\":50}],\"type\":\"transaction\"}",
        "{\"height\":1,\"outputs\":[{\"pubkey\":\"f66c7d51551d344b74e071d3b988d2bc09c3ffa82857302620d14f2469cfbf60\",\"value\":50000000000000}],\"type\":\"transaction\"}",
        "{\"inputs\":[{\"outpoint\":{\"index\":0,\"txid\":\"2a9458a2e75ed8bd0341b3cb2ab21015bbc13f21ea06229340a7b2b75720c4df\"},\"sig\":\"49cc4f9a1fb9d600a7debc99150e7909274c8c74edd7ca183626dfe49eb4aa21c6ff0e4c5f0dc2a328ad6b8ba10bf7169d5f42993a94bf67e13afa943b749c0b\"}],\"outputs\":[{\"pubkey\":\"c7c2c13afd02be7986dee0f4630df01abdbc950ea379055f1a423a6090f1b2b3\",\"value\":50}],\"type\":\"transaction\"}",
        "{\"height\":1,\"outputs\":[{\"pubkey\":\"f66c7d51551d344b74e071d3b988d2bc09c3ffa82857302620d14f2469cfbf60\",\"value\":50000000000000}],\"type\":\"transaction\"}",
        "{\"height\":2,\"outputs\":[{\"pubkey\":\"c7c2c13afd02be7986dee0f4630df01abdbc950ea379055f1a423a6090f1b2b3\",\"value\":50000000000000}],\"type\":\"transaction\"}",
        "{\"inputs\":[{\"outpoint\":{\"index\":0,\"txid\":\"2a9458a2e75ed8bd0341b3cb2ab21015bbc13f21ea06229340a7b2b75720c4df\"},\"sig\":\"334939cac007a71e72484ffa5f34fabe3e3aff31297003a7d3d24795ed33d04a72f8b14316bce3e6467b2f6e66d481f8142ccd9933279fdcb3aef7ace145f10b\"},{\"outpoint\":{\"index\":0,\"txid\":\"73231cc901774ddb4196ee7e9e6b857b208eea04aee26ced038ac465e1e706d2\"},\"sig\":\"032c6c0a1074b7a965e58fa5071aa9e518bf5c4db9e2880ca5bb5c55dcea47cfd6e0a9859526a16d2bb0b46da0ca4c6f90be8ddf16b149be66016d7f272e6708\"}],\"outputs\":[{\"pubkey\":\"f66c7d51551d344b74e071d3b988d2bc09c3ffa82857302620d14f2469cfbf60\",\"value\":20}],\"type\":\"transaction\"}",
        "{\"height\":2,\"outputs\":[{\"pubkey\":\"c7c2c13afd02be7986dee0f4630df01abdbc950ea379055f1a423a6090f1b2b3\",\"value\":50000000000000}],\"type\":\"transaction\"}",
        "{\"type\":\"transaction\",\"height\":0,\"outputs\":[{\"pubkey\":\"8dbcd2401c89c04d6e53c81c90aa0b551cc8fc47c0469217c8f5cfbae1e911f9\",\"value\":50000000000}]}"
    )

    val blocksjson = listOf(
        Json.encodeToString(Config.config.consensus.genesisBlock),
        "{\"T\":\"00000002af000000000000000000000000000000000000000000000000000000\",\"created\":1624229079,\"miner\":\"TUWien-Kerma\",\"nonce\":\"200000000000000000000000000000000000000000000000000000000e762cb9\",\"note\":\"First block. Yayy, I have 50 ker now!!\",\"previd\":\"00000000a420b7cefa2b7730243316921ed59ffe836e111ca3801f82a4f5360e\",\"txids\":[\"1bb37b637d07100cd26fc063dfd4c39a7931cc88dae3417871219715a5e374af\"],\"type\":\"block\"}",
        "{\"T\":\"00000002af000000000000000000000000000000000000000000000000000000\",\"created\":1624209000,\"miner\":\"TUWien-Kerma\",\"nonce\":\"100000000000000000000000000000000000000000000000000000000166a7e6\",\"note\":\"This block has timestamp before genesis :/\",\"previd\":\"00000000a420b7cefa2b7730243316921ed59ffe836e111ca3801f82a4f5360e\",\"txids\":[\"7beb66b257df282da23d30d06966ca0902ce0d764fb062b77560b03e187a53d7\"],\"type\":\"block\"}",
        "{\"T\":\"00000002af000000000000000000000000000000000000000000000000000000\",\"created\":1624239080,\"miner\":\"TUWien-Kerma\",\"nonce\":\"300000000000000000000000000000000000000000000000000000001f8e0f20\",\"note\":\"Wow, this is a super block!\",\"previd\":\"00000000a420b7cefa2b7730243316921ed59ffe836e111ca3801f82a4f5360e\",\"txids\":[\"7beb66b257df282da23d30d06966ca0902ce0d764fb062b77560b03e187a53d7\"],\"type\":\"block\"}",
        "{\"T\":\"00000002af000000000000000000000000000000000000000000000000000000\",\"created\":1624220079,\"miner\":\"Snekel testminer\",\"nonce\":\"000000000000000000000000000000000000000000000000000000001beecbf3\",\"note\":\"First block after genesis with CBTX and TX spending it\",\"previd\":\"00000000a420b7cefa2b7730243316921ed59ffe836e111ca3801f82a4f5360e\",\"txids\":[\"2a9458a2e75ed8bd0341b3cb2ab21015bbc13f21ea06229340a7b2b75720c4df\",\"7ef80f2da40b3f681a5aeb7962731beddccea25fa51e6e7ae6fbce8a58dbe799\"],\"type\":\"block\"}",
   /*i*/     "{\"T\":\"00000002af000000000000000000000000000000000000000000000000000000\",\"created\":1624220079,\"miner\":\"Snekel testminer\",\"nonce\":\"000000000000000000000000000000000000000000000000000000009d8b60ea\",\"note\":\"First block after genesis with CBTX\",\"previd\":\"00000000a420b7cefa2b7730243316921ed59ffe836e111ca3801f82a4f5360e\",\"txids\":[\"2a9458a2e75ed8bd0341b3cb2ab21015bbc13f21ea06229340a7b2b75720c4df\"],\"type\":\"block\"}",
        "{\"T\":\"00000002af000000000000000000000000000000000000000000000000000000\",\"created\":1624221079,\"miner\":\"Snekel testminer\",\"nonce\":\"000000000000000000000000000000000000000000000000000000004d82fc68\",\"note\":\"Second block after genesis with CBTX\",\"previd\":\"0000000108bdb42de5993bcf5f7d92557585dd6abfe9fb68e796518fe7f2ed2e\",\"txids\":[\"73231cc901774ddb4196ee7e9e6b857b208eea04aee26ced038ac465e1e706d2\"],\"type\":\"block\"}",
        /*i*/     "{\"T\":\"00000002af000000000000000000000000000000000000000000000000000000\",\"created\":1624222079,\"miner\":\"Snekel testminer\",\"nonce\":\"00000000000000000000000000000000000000000000000000000000062d431b\",\"note\":\"Third block after genesis with double-spending TX\",\"previd\":\"00000002a8986627f379547ed1ec990841e1f1c6ba616a56bfcd4b410280dc6d\",\"txids\":[\"fbb455506e5a7ce628fed88c8429e43810d3e306c4ff0c5a8313a1aeed6da88d\",\"7ef80f2da40b3f681a5aeb7962731beddccea25fa51e6e7ae6fbce8a58dbe799\"],\"type\":\"block\"}",
        "{\"T\":\"00000002af000000000000000000000000000000000000000000000000000000\",\"created\":1624221079,\"miner\":\"Snekel testminer\",\"nonce\":\"00000000000000000000000000000000000000000000000000000000182b95ea\",\"note\":\"Second block after genesis with CBTX and TX\",\"previd\":\"0000000108bdb42de5993bcf5f7d92557585dd6abfe9fb68e796518fe7f2ed2e\",\"txids\":[\"73231cc901774ddb4196ee7e9e6b857b208eea04aee26ced038ac465e1e706d2\",\"7ef80f2da40b3f681a5aeb7962731beddccea25fa51e6e7ae6fbce8a58dbe799\"],\"type\":\"block\"}",
        /*i*/"{\"T\":\"00000002af000000000000000000000000000000000000000000000000000000\",\"created\":1624222079,\"miner\":\"Snekel testminer\",\"nonce\":\"0000000000000000000000000000000000000000000000000000000010fea5cc\",\"note\":\"Third block after genesis with double-spending TX\",\"previd\":\"000000021dc4cfdcd0970084949f94da17f97504e1cc3e354851bb4768842b57\",\"txids\":[\"fbb455506e5a7ce628fed88c8429e43810d3e306c4ff0c5a8313a1aeed6da88d\"],\"type\":\"block\"}",
        "{\"T\":\"00000002af000000000000000000000000000000000000000000000000000000\",\"created\":1624239080,\"miner\":\"TUWien-Kerma\",\"nonce\":\"0000000000000000000000000000000000000000000000000000000019a2c9b0\",\"note\":\"Peew, this was difficult!\",\"previd\":\"00000000a420b7cefa2b7730243316921ed59ffe836e111ca3801f82a4f5360e\",\"txids\":[\"7beb66b257df282da23d30d06966ca0902ce0d764fb062b77560b03e187a53d7\"],\"type\":\"block\"}"
    )

    val validator = TransactionValidator()
    val bvalidator = BlockValidator()

    val txs = txsjson.map { Json.decodeFromString<Transaction>(it) }
        .distinctBy { it.hash() }
        .toMutableList()
    val blocks = blocksjson.map { Json.decodeFromString<Block>(it) }

    var utxoSet = UtxoSet()

    var height = 0L

    for(block in blocks){

        println("\nHash: ${block.hash()}")

        if(block.hash() in listOf("000000021dc4cfdcd0970084949f94da17f97504e1cc3e354851bb4768842b57", "0000000108bdb42de5993bcf5f7d92557585dd6abfe9fb68e796518fe7f2ed2e")){
            println()
        }

        val blocktxs = txs.filter { it.hash() in block.txids }

        blocktxs.forEach {
            validator.db.put(it.hash(), it)
        }

        val prev = block.previd?.let { validator.db.get<Block>(it) }

        val nus = utxoSet.createNew()
        val valid = bvalidator.validateBlock(block, nus, prev, height - 1)

        println("Valid: $valid")
        if(valid){
            validator.db.put(block.hash(), block)
            validator.db.put(block.hash() + "_processed", ProcessedBlock(block, nus.getUtxos(), height))
            utxoSet = nus
            height++
        }

    }

//    println(tx.json())
//
//    println(tx.hash())

//    validator.db.put("48c2ae2fbb4dead4bcc5801f6eaa9a350123a43750d22d05c53802b69c7cd9fb", Transaction(inputs = listOf(), outputs = listOf(
//        Transaction.TransactionOutput("62b7c521cd9211579cf70fd4099315643767b96711febaa5c76dc3daf27c281c", 49000000000000))))
//
//    println(validator.validate(tx))

//    val promise = Promise<String>()
//
//    Thread {
//        println("Listening")
//        val r = promise.await()
//        println("REsult: $r")
//    }.start()
//
//    Thread {
//        Thread.sleep(4000)
//        promise.resolve("Hello")
//    }.start()

}

