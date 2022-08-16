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
import java.util.Base64

private val tokenPattern = Regex("Ethereum +(0x[a-fA-F\\d]+)\\.0x([a-fA-F\\d]+)\\.?(.+)?$")

@Serializable
data class LoginRequest(
    val address: String,
    val signature: String,
    val message: String = "",
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
        val message = if (match.groupValues.size == 4) {
            match.groupValues[3]
        } else {
            ""
        }
        return validate(address, signature, message)
    }

    fun validate(body: LoginRequest): Address =
        validate(
            body.address,
            HexString(body.signature).clean0xPrefix().string,
            body.message,
        )

    private fun validate(address: String, signature: String, messageBase64: String): Address {
        if (!Address(address).isValid()) {
            throw AuthException("Address is not valid")
        }
        val nonce = nonces.byAddress(address).find {
            // http://eips.ethereum.org/EIPS/eip-191
            val message = Base64.getDecoder().decode(messageBase64)
            val messageWithNonce = message + it.nonce.toByteArray()
            val fullMessage =
                "\u0019Ethereum Signed Message:\n${messageWithNonce.size}".toByteArray() +
                        messageWithNonce
            val key = signedMessageToKey(fullMessage, signature.toSignatureData())
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
