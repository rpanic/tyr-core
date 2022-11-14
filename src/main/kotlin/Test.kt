import blockchain.TransactionValidator
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import model.Transaction
import utils.info

fun main(){

//    println(Json.encodeToString(Hello("0.7.0", "client") as KarmaObject))

    KoinSetup.setup()

    val json = listOf(
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
        "{\"inputs\":[{\"outpoint\":{\"index\":0,\"txid\":\"8a6fdba6541db169d9bb3249e91bfde5692048818031aa55062a41c673bd01ea\"},\"sig\":\"6f3d4522bf29e55becb3ce707830f62954cb109464737ea37bd0213f14ea52b530fe0c13d56c9496fa72466cbd94fb0a927e53a57786bca2dfa7214c89580403\"}],\"outputs\":[{\"pubkey\":\"5e3f197d8b63a853b79330d96a82d2ad51ff06605879f5f4b664c1d9a6b9c02e\",\"value\":50}],\"type\":\"transaction\"}"
    )

//    val tx = Transaction(
//        inputs = listOf(
//            Transaction.TransactionInput(
//                outpoint = Transaction.TransactionInput.TransactionOutpoint(txid = "48c2ae2fbb4dead4bcc5801f6eaa9a350123a43750d22d05c53802b69c7cd9fb", index = 0),
//                sig = "d51e82d5c121c5db21c83404aaa3f591f2099bccf731208c4b0b676308be1f994882f9d991c0ebfd8fdecc90a4aec6165fc3440ade9c83b043cba95b2bba1d0a"
//            )
//        ),
//        outputs = listOf(
//            Transaction.TransactionOutput(
//                pubkey = "228ee807767047682e9a556ad1ed78dff8d7edf4bc2a5f4fa02e4634cfcad7e0",
//                value = 49000000000000
//            )
//        )
//    )

    val validator = TransactionValidator()

    for(s in json){

        val tx = Json.decodeFromString<Transaction>(s)

        println("Hash: ${tx.hash()}")

        val valid = validator.validate(tx)

        println("Valid: $valid")
        if(valid){
            validator.db.put(tx.hash(), tx)
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

