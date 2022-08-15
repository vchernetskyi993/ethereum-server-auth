package com.example

import com.example.plugins.NonceStorage
import io.ktor.server.application.ApplicationCall
import org.kethereum.model.Address

private val tokenPattern = Regex("Ethereum +([a-fA-F\\d]+)\\.([a-fA-F\\d]+)")

class AuthException(override val message: String) : Exception(message)

fun validateEthereumSignature(call: ApplicationCall, nonces: NonceStorage): Address {
    val match = call.request.headers["Authorization"]?.let { tokenPattern.find(it) }
        ?: throw AuthException("Invalid auth token")
    val (address, signature) = match.destructured
    // TODO: parse ethereum signature
    val nonce = signature
    if (!nonces.remove(address, nonce)) {
        throw AuthException("Invalid nonce")
    }
    return Address(address)
}
