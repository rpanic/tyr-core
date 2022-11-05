package utils

import java.nio.charset.Charset
import java.security.MessageDigest

object Sha256 {

    val digest = MessageDigest.getInstance("SHA-256");

    fun hash(s: String) : String {
        return digest.digest(s.fromHex()).toHex()
    }

    fun hashText(s: String) : String {
        return digest.digest(s.toByteArray(Charset.forName("UTF-8"))).toHex()
    }

}