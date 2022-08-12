package com.example.plugins

import com.example.AuthException
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.response.respondText

fun Application.configureErrorHandling() {
    install(StatusPages) {
        exception<AuthException> { call, cause ->
            call.respondText(
                cause.message,
                status = HttpStatusCode.Unauthorized
            )
        }
        exception<IllegalArgumentException> { call, cause ->
            call.respondText(
                cause.message ?: "",
                status = HttpStatusCode.BadRequest
            )
        }
    }
}
