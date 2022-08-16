package com.example.plugins

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.auth.Authentication
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.jwt.jwt
import io.ktor.server.response.respond
import org.kethereum.erc55.isValid
import org.kethereum.model.Address
import kotlin.time.Duration

data class AuthProps(
    val secret: String,
    val issuer: String,
    val expiration: Duration,
)

fun Application.configureAuthentication(): AuthProps {
    val props = AuthProps(
        environment.config.property("app.jwt.secret").getString(),
        environment.config.property("app.jwt.issuer").getString(),
        Duration.parse(environment.config.property("app.jwt.expiration").getString()),
    )
    install(Authentication) {
        jwt("jwt-auth") {
            realm = "JWT access"
            verifier(
                JWT.require(Algorithm.HMAC256(props.secret))
                    .withIssuer(props.issuer)
                    .build()
            )
            validate { credential ->
                val address = credential.payload.getClaim("address")?.asString()
                when (address?.let { Address(it).isValid() }) {
                    true -> JWTPrincipal(credential.payload)
                    else -> null
                }
            }
            challenge { _, _ ->
                call.respond(HttpStatusCode.Unauthorized, "Token is not valid or has expired")
            }
        }
    }
    return props
}