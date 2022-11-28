package network

import java.util.concurrent.TimeUnit
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

data class PromiseResult<T>(val res: T?, val err: Boolean){
    fun get(): T {
        return res!!
    }
    fun isOk() : Boolean = !err
}

class Promise<T> {

    val lock = ReentrantLock()
    val cond = lock.newCondition()
    var t: T? = null
    var err = false

    fun resolve(t: T){
        this.t = t
        lock.withLock {
            cond.signalAll()
        }
        listeners.forEach { it(t) }
    }

    fun resolveError(){
        this.err = true
        lock.withLock {
            cond.signalAll()
        }
    }

    fun await() : PromiseResult<T>{
        if(t == null){
            lock.withLock {
                val awaited = cond.await(5, TimeUnit.SECONDS)

                if(!awaited){
                    err = true
                }
            }
        }
        return PromiseResult(t, err)
    }

    private val listeners: MutableList<(T) -> Unit> = mutableListOf()

    fun onResolve(f: (T) -> Unit) {
        listeners += f
    }

    fun isResolved() : Boolean = t != null

    fun isResolvedOk() : Boolean = isResolved() && !err

    companion object {
        fun <T> awaitFirst(list: List<Promise<T>>) : T? {
            val lock = ReentrantLock()
            val cond = lock.newCondition()

            var t: T? = null

            list.forEach { p ->
                p.onResolve {
                    if(it != null){
                        t = it
                        lock.withLock {
                            cond.signalAll()
                        }
                    }
                }
            }
            lock.withLock {
                cond.await(10, TimeUnit.SECONDS)
            }

            return t

        }
    }

    fun <R> map(f: (T) -> R) : Promise<R> {

        val p = Promise<R>()
        Thread{
            val res = this.await()
            if(res.isOk()){
                p.resolve(f(res.get()))
            }else{
                p.resolveError()
            }
        }.start()
        return p

    }

}