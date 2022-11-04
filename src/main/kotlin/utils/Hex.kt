package utils

import java.lang.NumberFormatException
import java.math.BigInteger

fun String.fromHex() : ByteArray? {
    return try {
        BigInteger(this, 16).toByteArray()
    }catch(e: NumberFormatException){
        e.printStackTrace()
        null
    }
}

fun ByteArray.toHex() : String{
    return BigInteger(this).toString(16)
}