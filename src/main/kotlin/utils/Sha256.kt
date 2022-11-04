package utils

import java.security.MessageDigest

object Sha256 {

    val digest = MessageDigest.getInstance("SHA-256");

    fun hash(s: String) : String {
        return digest.digest(s.fromHex()).toHex()
    }

}