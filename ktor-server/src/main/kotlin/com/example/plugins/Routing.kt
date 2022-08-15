package com.example.plugins

import com.example.randomNonce
import com.example.validateEthereumSignature
import io.ktor.http.HttpStatusCode
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
            nonces.add(address, randomNonce())
            call.response.status(HttpStatusCode.OK)
        }
        get("/hello") {
            val address = validateEthereumSignature(call, nonces)
            call.respondText("Hello, ${address.hex}!")
        }
    }
}
