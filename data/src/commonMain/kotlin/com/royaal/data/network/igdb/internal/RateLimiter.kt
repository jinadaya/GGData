package com.royaal.data.network.igdb.internal

import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.sync.withPermit
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlin.time.TimeSource

/**
 * Sliding-window limiter: at most [requestsPerSecond] request starts per rolling second,
 * and at most [maxConcurrent] in flight. Shared by every call made through one client.
 */
internal class RateLimiter(
    private val requestsPerSecond: Int,
    maxConcurrent: Int,
) {
    init {
        require(requestsPerSecond > 0) { "requestsPerSecond must be > 0" }
        require(maxConcurrent > 0) { "maxConcurrent must be > 0" }
    }

    private val window = 1.seconds
    private val semaphore = Semaphore(maxConcurrent)
    private val mutex = Mutex()
    private val starts = ArrayDeque<TimeSource.Monotonic.ValueTimeMark>()

    suspend fun <T> withPermit(block: suspend () -> T): T = semaphore.withPermit {
        awaitSlot()
        block()
    }

    private suspend fun awaitSlot() {
        while (true) {
            val wait: Duration = mutex.withLock {
                val now = TimeSource.Monotonic.markNow()
                while (starts.isNotEmpty() && now - starts.first() >= window) starts.removeFirst()
                if (starts.size < requestsPerSecond) {
                    starts.addLast(now)
                    Duration.ZERO
                } else {
                    window - (now - starts.first())
                }
            }
            if (wait <= Duration.ZERO) return
            delay(wait)
        }
    }
}
