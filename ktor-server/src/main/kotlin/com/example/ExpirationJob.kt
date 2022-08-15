package com.example

import com.example.plugins.NonceStorage
import kotlin.time.Duration
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class ExpirationJob(
    private val nonces: NonceStorage,
    private val interval: Duration,
) {
    private val scheduler = Executors.newScheduledThreadPool(1)

    fun start() {
        val delay = interval.inWholeMilliseconds
        scheduler.scheduleWithFixedDelay(
            { nonces.expire(interval) },
            delay,
            delay,
            TimeUnit.MILLISECONDS
        )
    }

    fun stop() {
        scheduler.shutdown()
    }
}
