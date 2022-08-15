package com.example.plugins

import com.example.ExpirationJob
import io.ktor.server.application.Application
import io.ktor.server.application.ApplicationStarted
import io.ktor.server.application.ApplicationStopping
import kotlin.time.Duration

fun Application.configureJobs(nonces: NonceStorage) {
    val expiration = ExpirationJob(
        nonces,
        Duration.parse(environment.config.property("app.expiration").getString())
    )
    environment.monitor.subscribe(ApplicationStarted) {
        environment.log.info("Starting background jobs...")
        expiration.start()
    }
    environment.monitor.subscribe(ApplicationStopping) {
        environment.log.info("Stopping background jobs...")
        expiration.stop()
    }
}
