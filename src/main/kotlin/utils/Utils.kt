package utils

fun <T> List<T>.skip(n: Int) : List<T>{
    return this.subList(n, this.size)
}