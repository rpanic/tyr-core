import blockchain.TransactionValidator
import model.Transaction
import utils.info

fun main(){

//    println(Json.encodeToString(Hello("0.7.0", "client") as KarmaObject))

    KoinSetup.setup()

//    val tx = Transaction(
//        inputs = listOf(
//            Transaction.TransactionInput(
//                Transaction.TransactionInput.TransactionOutpoint("1bb37b637d07100cd26fc063dfd4c39a7931cc88dae3417871219715a5e374af", 0),
//                "1d0d7d774042607c69a87ac5f1cdf92bf474c25fafcc089fe667602bfefb0494726c519e92266957429ced875256e6915eb8cea2ea66366e739415efc47a6805"
//            )
//        ),
//        outputs = listOf(
//            Transaction.TransactionOutput(
//                "8dbcd2401c89c04d6e53c81c90aa0b551cc8fc47c0469217c8f5cfbae1e911f9", 10
//            )
//        ),
//    )

    val tx = Transaction(
        inputs = listOf(
            Transaction.TransactionInput(
                outpoint = Transaction.TransactionInput.TransactionOutpoint(txid = "48c2ae2fbb4dead4bcc5801f6eaa9a350123a43750d22d05c53802b69c7cd9fb", index = 0),
                sig = "d51e82d5c121c5db21c83404aaa3f591f2099bccf731208c4b0b676308be1f994882f9d991c0ebfd8fdecc90a4aec6165fc3440ade9c83b043cba95b2bba1d0a"
            )
        ),
        outputs = listOf(
            Transaction.TransactionOutput(
                pubkey = "228ee807767047682e9a556ad1ed78dff8d7edf4bc2a5f4fa02e4634cfcad7e0",
                value = 49000000000000
            )
        )
    )

    println(tx.json())

    println(tx.hash())

    val vali = TransactionValidator()

    vali.db.put("48c2ae2fbb4dead4bcc5801f6eaa9a350123a43750d22d05c53802b69c7cd9fb", Transaction(inputs = listOf(), outputs = listOf(
        Transaction.TransactionOutput("62b7c521cd9211579cf70fd4099315643767b96711febaa5c76dc3daf27c281c", 49000000000000))))

    println(vali.validate(tx))

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

