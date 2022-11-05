import blockchain.TransactionValidator
import model.Transaction
import utils.info

fun main(){

//    println(Json.encodeToString(Hello("0.7.0", "client") as KarmaObject))

    KoinSetup.setup()

    val tx = Transaction(
        inputs = listOf(
            Transaction.TransactionInput(
                Transaction.TransactionInput.TransactionOutpoint("1bb37b637d07100cd26fc063dfd4c39a7931cc88dae3417871219715a5e374af", 0),
                "1d0d7d774042607c69a87ac5f1cdf92bf474c25fafcc089fe667602bfefb0494726c519e92266957429ced875256e6915eb8cea2ea66366e739415efc47a6805"
            )
        ),
        outputs = listOf(
            Transaction.TransactionOutput(
                "8dbcd2401c89c04d6e53c81c90aa0b551cc8fc47c0469217c8f5cfbae1e911f9", 10
            )
        ),
    )

    println(tx.json())

    println(tx.hash())

    val vali = TransactionValidator()

    vali.db.put("1bb37b637d07100cd26fc063dfd4c39a7931cc88dae3417871219715a5e374af", Transaction(inputs = listOf(), outputs = listOf(
        Transaction.TransactionOutput("1bb37b637d07100cd26fc063dfd4c39a7931cc88dae3417871219715a5e374af", 10))))

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

