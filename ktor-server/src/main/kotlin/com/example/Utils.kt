package com.example

import java.math.BigInteger
import java.security.SecureRandom

fun randomNonce(): String {
    val rnd = SecureRandom()
    val bytes = ByteArray(16)
    rnd.nextBytes(bytes)
    return BigInteger(1, bytes).toString()
}
