package com.example.plugins

import com.example.randomNonce
import com.example.validateEthereumSignature
import io.ktor.server.application.Application
import io.ktor.server.application.call
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.routing

fun Application.configureRouting(nonces: NonceStorage) {
    routing {
        post("/nonce/{address}") {
            val address = call.parameters["address"]
                ?: throw IllegalArgumentException("`address` path param is required")
            val nonce = randomNonce()
            nonces.add(address, nonce)
            call.respondText("\"$nonce\"")
        }
        get("/hello") {
            val address = validateEthereumSignature(call, nonces)
            call.respondText("Hello from Ktor, ${address.hex}!")
        }
    }
}
