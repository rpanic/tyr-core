import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import model.Transaction
import network.Hello
import network.KarmaObject
import network.Promise
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

fun main(){

//    println(Json.encodeToString(Hello("0.7.0", "client") as KarmaObject))

    val tx = Transaction(
        inputs = listOf(
            Transaction.TransactionInput(
                Transaction.TransactionInput.TransactionOutpoint("f71408bf847d7dd15824574a7cd4afdfaaa2866286910675cd3fc371507aa196", 0),
                "3869a9ea9e7ed926a7c8b30fb71f6ed151a132b03fd5dae764f015c98271000e7da322dbcfc97af7931c23c0fae060e102446ccff0f54ec00f9978f3a69a6f0f"
            )
        ),
        outputs = listOf(
            Transaction.TransactionOutput(
                "077a2683d776a71139fd4db4d00c16703ba0753fc8bdc4bd6fc56614e659cde3", 5100000000
            )
        ),
    )

    println(tx.json())


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

