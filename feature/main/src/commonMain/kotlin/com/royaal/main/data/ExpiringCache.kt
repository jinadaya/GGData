package com.royaal.main.data

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.Instant

/** In-memory values that are forgotten [ttl] after they were put. Safe to use from any thread. */
internal class ExpiringCache<K, V : Any>(
    private val ttl: Duration,
    private val clock: Clock = Clock.System,
) {
    private class Entry<V>(val value: V, val expiresAt: Instant)

    // A state flow only for its atomic update: nobody collects it.
    private val entries = MutableStateFlow(emptyMap<K, Entry<V>>())

    operator fun get(key: K): V? {
        val entry = entries.value[key] ?: return null
        return entry.value.takeIf { clock.now() < entry.expiresAt }
    }

    operator fun set(key: K, value: V) {
        val now = clock.now()
        // Expired entries go on the next write, so the map doesn't keep growing.
        entries.update { current ->
            current.filterValues { now < it.expiresAt } + (key to Entry(value, now + ttl))
        }
    }

    /** The cached value, or the [load]ed one, which is then cached. */
    suspend fun getOrLoad(key: K, load: suspend () -> V): V =
        get(key) ?: load().also { set(key, it) }
}
