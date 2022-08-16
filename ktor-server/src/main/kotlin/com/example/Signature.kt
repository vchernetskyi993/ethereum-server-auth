package com.example

import com.example.plugins.NonceStorage
import io.ktor.http.Headers
import kotlinx.serialization.Serializable
import org.kethereum.crypto.signedMessageToKey
import org.kethereum.crypto.toAddress
import org.kethereum.erc55.isValid
import org.kethereum.extensions.hexToBigInteger
import org.kethereum.model.Address
import org.kethereum.model.SignatureData
import org.komputing.khex.extensions.clean0xPrefix
import org.komputing.khex.model.HexString
import java.math.BigInteger
import java.security.SecureRandom

private val tokenPattern = Regex("Ethereum +(0x[a-fA-F\\d]+)\\.0x([a-fA-F\\d]+)")

@Serializable
data class LoginRequest(
    val address: String,
    val signature: String,
)

class AuthException(override val message: String) : Exception(message)

fun randomNonce(): String {
    val rnd = SecureRandom()
    val bytes = ByteArray(16)
    rnd.nextBytes(bytes)
    return BigInteger(1, bytes).toString()
}

class SignatureValidator(
    private val nonces: NonceStorage,
) {
    fun validate(headers: Headers): Address {
        val match = headers["Authorization"]?.let { tokenPattern.find(it) }
            ?: throw AuthException("Invalid auth token")
        val (address, signature) = match.destructured
        return validate(address, signature)
    }

    fun validate(body: LoginRequest): Address =
        validate(body.address, HexString(body.signature).clean0xPrefix().string)

    private fun validate(address: String, signature: String): Address {
        if (!Address(address).isValid()) {
            throw AuthException("Address is not valid")
        }
        val nonce = nonces.byAddress(address).find {
            // http://eips.ethereum.org/EIPS/eip-191
            val message = "\u0019Ethereum Signed Message:\n${it.nonce.length}${it.nonce}"
            val key = signedMessageToKey(message.toByteArray(), signature.toSignatureData())
            key.toAddress() == Address(address)
        } ?: throw AuthException("Invalid nonce")
        nonce.delete()
        return Address(address)
    }
}

private fun String.toSignatureData(): SignatureData {
    val rI = HexString(substring(0, 64)).hexToBigInteger()
    val sI = HexString(substring(64, 128)).hexToBigInteger()
    val vI = HexString(substring(128)).hexToBigInteger()
    return SignatureData(rI, sI, vI)
}
