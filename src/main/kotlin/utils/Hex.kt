package utils

fun String.fromHex() : ByteArray? {
    return try {
        Numeric.hexStringToByteArray(this)
    }catch(e: Exception){
        e.printStackTrace()
        null
    }
}

fun ByteArray.toHex() : String{
    return Numeric.toHexString(this).removePrefix("0x")
}