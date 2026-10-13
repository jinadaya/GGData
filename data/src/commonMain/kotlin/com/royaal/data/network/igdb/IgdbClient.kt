package com.royaal.data.network.igdb

import com.royaal.data.network.igdb.auth.IgdbAuth
import com.royaal.data.network.igdb.auth.IgdbTokenProvider
import com.royaal.data.network.igdb.internal.AuthHandler
import com.royaal.data.network.igdb.internal.RateLimiter
import com.royaal.data.network.igdb.internal.createDefaultHttpClient
import com.royaal.data.network.igdb.query.ApicalypseBuilder
import com.royaal.data.network.igdb.query.ApicalypseQuery
import com.royaal.data.network.igdb.query.MultiQuery
import com.royaal.data.network.igdb.query.MultiQueryBuilder
import com.royaal.data.network.igdb.query.apicalypse
import com.royaal.data.network.igdb.query.buildMultiQuery
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.expectSuccess
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.header
import io.ktor.client.request.request
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.content.TextContent
import kotlinx.coroutines.delay
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.longOrNull
import kotlin.coroutines.cancellation.CancellationException
import kotlin.random.Random
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds
import kotlin.time.TimeSource

/**
 * IGDB v4 client.
 *
 * Every request comes in three flavours so you can plug in your own models later:
 * - `String`     — raw JSON exactly as IGDB returned it
 * - `JsonArray`  — parsed but untyped
 * - `T`          — decoded with a `DeserializationStrategy<T>` (or the reified `...As<T>()` helpers)
 *
 * Per-endpoint helpers live on [IgdbEndpointApi]: `client.games.listAs<MyGame> { ... }`.
 *
 * Create one instance per app and reuse it (it owns the rate limiter and the token cache).
 *
 * @param httpClient optional custom client (e.g. `MockEngine` in tests). If supplied, you own its lifecycle.
 */
class IgdbClient(
    val config: IgdbConfig,
    httpClient: HttpClient? = null,
) : AutoCloseable {

    constructor(auth: IgdbAuth) : this(IgdbConfig(auth))

    private val ownsHttpClient = httpClient == null
    private val http: HttpClient = httpClient ?: createDefaultHttpClient(config)
    private val baseUrl = config.baseUrl.trimEnd('/') + "/"
    private val auth = AuthHandler.create(config.auth, http, config.json)
    private val rateLimiter = config.rateLimit?.let { RateLimiter(it.requestsPerSecond, it.maxConcurrentRequests) }

    val json: Json get() = config.json

    /** The active token provider (a `TwitchTokenProvider` for [IgdbAuth.ClientCredentials]), or null for proxies. */
    val tokenProvider: IgdbTokenProvider? get() = auth.tokenProvider

    /** Webhook management (normally done from a server, provided for completeness). */
    val webhooks: IgdbWebhooks by lazy { IgdbWebhooks(this) }

    /** Request facade for any endpoint. Predefined shortcuts: `client.games`, `client.platforms`, … */
    fun endpoint(endpoint: IgdbEndpoint): IgdbEndpointApi = IgdbEndpointApi(endpoint, this)

    fun endpoint(path: String): IgdbEndpointApi = endpoint(IgdbEndpoint(path))

    // ====================================================================== queries

    /** POSTs an arbitrary Apicalypse body and returns raw JSON. */
    suspend fun rawQuery(endpoint: IgdbEndpoint, body: String): String =
        postApicalypse(endpoint.path, body).bodyAsText()

    suspend fun query(endpoint: IgdbEndpoint, query: ApicalypseQuery): String =
        rawQuery(endpoint, query.body)

    suspend fun query(endpoint: IgdbEndpoint, block: ApicalypseBuilder.() -> Unit): String =
        query(endpoint, apicalypse(block))

    suspend fun queryJson(endpoint: IgdbEndpoint, query: ApicalypseQuery): JsonArray =
        parseArray(query(endpoint, query))

    suspend fun queryJson(endpoint: IgdbEndpoint, block: ApicalypseBuilder.() -> Unit): JsonArray =
        queryJson(endpoint, apicalypse(block))

    /** Decodes the whole response with [deserializer] — typically `ListSerializer(MyGame.serializer())`. */
    suspend fun <T> query(endpoint: IgdbEndpoint, query: ApicalypseQuery, deserializer: DeserializationStrategy<T>): T =
        decode(deserializer, query(endpoint, query))

    /** `POST /{endpoint}/count` — only the `where` (and `search`) part of [query] matters. */
    suspend fun count(endpoint: IgdbEndpoint, query: ApicalypseQuery = ApicalypseQuery.Empty): Long {
        val text = postApicalypse("${endpoint.path}/count", query.body).bodyAsText()
        return (parseElement(text) as? JsonObject)?.get("count")?.jsonPrimitive?.longOrNull
            ?: throw IgdbException.Decoding(text, IllegalStateException("Response has no numeric 'count'"))
    }

    suspend fun count(endpoint: IgdbEndpoint, block: ApicalypseBuilder.() -> Unit): Long =
        count(endpoint, apicalypse(block))

    /**
     * `POST /{endpoint}.pb` — raw protobuf bytes. Decode with IGDB's published `.proto`
     * (e.g. via Wire / kotlinx-serialization-protobuf) if you prefer protobuf over JSON.
     */
    suspend fun protobuf(endpoint: IgdbEndpoint, query: ApicalypseQuery): ByteArray =
        postApicalypse("${endpoint.path}.pb", query.body).body()

    // ====================================================================== multiquery

    suspend fun multiQuery(query: MultiQuery): String =
        postApicalypse("multiquery", query.body).bodyAsText()

    suspend fun multiQuery(block: MultiQueryBuilder.() -> Unit): String =
        multiQuery(buildMultiQuery(block))

    /** Array of `{"name": ..., "result": [...]}` / `{"name": ..., "count": n}`; see `JsonArray.multiQueryResult`. */
    suspend fun multiQueryJson(query: MultiQuery): JsonArray = parseArray(multiQuery(query))

    suspend fun multiQueryJson(block: MultiQueryBuilder.() -> Unit): JsonArray =
        multiQueryJson(buildMultiQuery(block))

    suspend fun <T> multiQuery(query: MultiQuery, deserializer: DeserializationStrategy<T>): T =
        decode(deserializer, multiQuery(query))

    // ====================================================================== decoding

    /** Decodes [text] with [json], wrapping failures in [IgdbException.Decoding]. */
    fun <T> decode(deserializer: DeserializationStrategy<T>, text: String): T = try {
        json.decodeFromString(deserializer, text)
    } catch (e: IllegalArgumentException) { // SerializationException is a subclass
        throw IgdbException.Decoding(text, e)
    }

    fun <T> decode(deserializer: DeserializationStrategy<T>, element: JsonElement): T = try {
        json.decodeFromJsonElement(deserializer, element)
    } catch (e: IllegalArgumentException) {
        throw IgdbException.Decoding(element.toString(), e)
    }

    override fun close() {
        if (ownsHttpClient) http.close()
    }

    // ====================================================================== transport

    private suspend fun postApicalypse(path: String, body: String): HttpResponse =
        execute(HttpMethod.Post, path, retryable = true, logBody = body) {
            setBody(TextContent(body, ContentType.Text.Plain))
        }

    /**
     * Sends a request with auth headers, client-side throttling, one token refresh on 401,
     * and exponential backoff on 429 (always) and 5xx / network errors (when [retryable]).
     */
    internal suspend fun execute(
        method: HttpMethod,
        path: String,
        retryable: Boolean,
        logBody: String? = null,
        configure: HttpRequestBuilder.() -> Unit = {},
    ): HttpResponse {
        val url = baseUrl + path.trimStart('/')
        var attempt = 0
        var authRetried = false
        var token = auth.currentToken()

        while (true) {
            log { "--> ${method.value} $url" + if (config.logBodies && !logBody.isNullOrBlank()) "\n$logBody" else "" }
            val started = TimeSource.Monotonic.markNow()

            val response = try {
                throttled {
                    http.request(url) {
                        this.method = method
                        expectSuccess = false
                        header(HttpHeaders.Accept, ContentType.Application.Json.toString())
                        auth.apply(this, token)
                        configure()
                    }
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: IgdbException) {
                throw e
            } catch (e: Exception) {
                log { "<-- ${method.value} $url FAILED: ${e.message}" }
                if (retryable && attempt < config.maxRetries) {
                    delay(backoff(attempt++, null))
                    continue
                }
                throw IgdbException.Network(e)
            }

            val status = response.status.value
            log { "<-- $status ${method.value} $url (${started.elapsedNow().inWholeMilliseconds}ms)" }

            when {
                status in 200..299 -> return response

                status == 401 -> {
                    if (!authRetried) {
                        val fresh = auth.onUnauthorized(token)
                        if (fresh != null) {
                            token = fresh
                            authRetried = true
                            continue
                        }
                    }
                    throw IgdbException.Unauthorized(response.bodyAsText())
                }

                status == 429 -> {
                    if (attempt < config.maxRetries) {
                        delay(backoff(attempt++, response))
                        continue
                    }
                    throw IgdbException.RateLimited(response.bodyAsText())
                }

                status >= 500 && retryable && attempt < config.maxRetries -> {
                    delay(backoff(attempt++, response))
                    continue
                }

                else -> throw IgdbException.Api(status, response.bodyAsText())
            }
        }
    }

    private suspend fun <T> throttled(block: suspend () -> T): T {
        val limiter = rateLimiter ?: return block()
        return limiter.withPermit(block)
    }

    private fun backoff(attempt: Int, response: HttpResponse?): Duration {
        response?.headers?.get(HttpHeaders.RetryAfter)?.trim()?.toLongOrNull()?.let { return it.seconds }
        val exponential = 500.milliseconds * (1 shl attempt.coerceAtMost(5))
        return exponential + Random.nextLong(0, 250).milliseconds
    }

    private inline fun log(message: () -> String) {
        config.logger?.log(message())
    }

    private fun parseElement(text: String): JsonElement = try {
        json.parseToJsonElement(text)
    } catch (e: IllegalArgumentException) {
        throw IgdbException.Decoding(text, e)
    }

    private fun parseArray(text: String): JsonArray =
        parseElement(text) as? JsonArray
            ?: throw IgdbException.Decoding(text, IllegalStateException("Expected a JSON array"))
}
