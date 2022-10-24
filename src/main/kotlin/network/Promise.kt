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

    fun isResolved() : Boolean = t != null

    fun isResolvedOk() : Boolean = isResolved() && !err

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