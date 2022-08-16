package com.example

import com.example.plugins.NonceStorage
import com.example.plugins.configureAuthentication
import com.example.plugins.configureCors
import com.example.plugins.configureDatabase
import com.example.plugins.configureErrorHandling
import com.example.plugins.configureJobs
import com.example.plugins.configureRouting
import com.example.plugins.configureSerialization
import io.ktor.server.application.Application

fun main(args: Array<String>): Unit =
    io.ktor.server.netty.EngineMain.main(args)

@Suppress("unused") // application.conf references the main function. This annotation prevents the IDE from marking it as unused.
fun Application.module() {
    configureSerialization()
    val nonces = NonceStorage(configureDatabase())
    val authProps = configureAuthentication()
    val signatureValidator = SignatureValidator(nonces)
    configureRouting(nonces, authProps, signatureValidator)
    configureErrorHandling()
    configureCors()
    configureJobs(nonces)
}
