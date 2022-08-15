package com.example

import com.example.plugins.NonceStorage
import io.ktor.server.application.ApplicationCall
import org.kethereum.crypto.signedMessageToKey
import org.kethereum.crypto.toAddress
import org.kethereum.extensions.hexToBigInteger
import org.kethereum.model.Address
import org.kethereum.model.SignatureData
import org.komputing.khex.model.HexString

private val tokenPattern = Regex("Ethereum +(0x[a-fA-F\\d]+)\\.0x([a-fA-F\\d]+)")

class AuthException(override val message: String) : Exception(message)

fun validateEthereumSignature(call: ApplicationCall, nonces: NonceStorage): Address {
    val match = call.request.headers["Authorization"]?.let { tokenPattern.find(it) }
        ?: throw AuthException("Invalid auth token")
    val (address, signature) = match.destructured
    val nonce = nonces.byAddress(address).find {
        // http://eips.ethereum.org/EIPS/eip-191
        val message = "\u0019Ethereum Signed Message:\n${it.nonce.length}${it.nonce}"
        val key = signedMessageToKey(message.toByteArray(), signature.toSignatureData())
        key.toAddress() == Address(address)
    } ?: throw AuthException("Invalid nonce")
    nonce.delete()
    return Address(address)
}

private fun String.toSignatureData(): SignatureData {
    val rI = HexString(substring(0, 64)).hexToBigInteger()
    val sI = HexString(substring(64, 128)).hexToBigInteger()
    val vI = HexString(substring(128)).hexToBigInteger()
    return SignatureData(rI, sI, vI)
}
