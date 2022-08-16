package com.example.plugins

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.example.LoginRequest
import com.example.SignatureValidator
import com.example.randomNonce
import io.ktor.server.application.Application
import io.ktor.server.application.call
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.principal
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.routing
import java.util.Date
import kotlin.time.Duration.Companion.milliseconds

fun Application.configureRouting(
    nonces: NonceStorage,
    authProps: AuthProps,
    signatureValidator: SignatureValidator,
) {
    routing {
        post("/nonce/{address}") {
            val address = call.parameters["address"]
                ?: throw IllegalArgumentException("`address` path param is required")
            val nonce = randomNonce()
            nonces.add(address, nonce)
            call.respondText("\"$nonce\"")
        }
        // single-time signature auth
        get("/hello") {
            val address = signatureValidator.validate(call.request.headers)
            call.respondText("Hello from Ktor, ${address.hex}!")
        }
        // JWT auth
        post("/login") {
            val address = signatureValidator.validate(call.receive<LoginRequest>())
            val token = JWT.create()
                .withIssuer(authProps.issuer)
                .withClaim("address", address.hex)
                .withExpiresAt(Date(System.currentTimeMillis() + authProps.expiration.inWholeMilliseconds))
                .sign(Algorithm.HMAC256(authProps.secret))
            call.respond(mapOf("accessToken" to token))
        }
        authenticate("jwt-auth") {
            get("/jwt/hello") {
                val principal = call.principal<JWTPrincipal>()
                val address = principal!!.payload.getClaim("address").asString()
                val expires =
                    principal.expiresAt?.time?.minus(System.currentTimeMillis())?.milliseconds
                call.respondText("Hello, $address! Token expires in $expires")
            }
        }
    }
}
