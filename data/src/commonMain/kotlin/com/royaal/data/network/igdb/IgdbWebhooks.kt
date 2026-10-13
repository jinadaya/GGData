package com.royaal.data.network.igdb

import io.ktor.client.request.forms.FormDataContent
import io.ktor.client.request.parameter
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpMethod
import io.ktor.http.Parameters

/**
 * IGDB webhooks: IGDB POSTs the changed entity to your [url][register] on create/update/delete.
 *
 * A phone can't receive webhooks, so these are normally called from a backend; they're
 * here so a shared KMP/JVM module can cover the whole API. All methods return raw JSON.
 */
class IgdbWebhooks internal constructor(private val client: IgdbClient) {

    enum class Method(val value: String) { Create("create"), Update("update"), Delete("delete") }

    /**
     * `POST /{endpoint}/webhooks/` (form-encoded `url`, `secret`, `method`).
     * IGDB sends [secret] back in the `X-Secret` header so you can verify calls.
     */
    suspend fun register(endpoint: IgdbEndpoint, url: String, secret: String, method: Method): String =
        client.execute(
            method = HttpMethod.Post,
            path = "${endpoint.path}/webhooks/",
            retryable = false,
            logBody = "url=$url&method=${method.value}",
        ) {
            setBody(
                FormDataContent(
                    Parameters.build {
                        append("url", url)
                        append("secret", secret)
                        append("method", method.value)
                    },
                ),
            )
        }.bodyAsText()

    /** `GET /webhooks/` — all webhooks registered for your client id. */
    suspend fun list(): String =
        client.execute(HttpMethod.Get, "webhooks/", retryable = true).bodyAsText()

    /** `GET /webhooks/{id}` */
    suspend fun get(webhookId: Long): String =
        client.execute(HttpMethod.Get, "webhooks/$webhookId", retryable = true).bodyAsText()

    /** `DELETE /webhooks/{id}` */
    suspend fun delete(webhookId: Long): String =
        client.execute(HttpMethod.Delete, "webhooks/$webhookId", retryable = false).bodyAsText()

    /** `POST /{endpoint}/webhooks/test/{webhookId}?entityId={entityId}` — makes IGDB fire a test call. */
    suspend fun test(endpoint: IgdbEndpoint, webhookId: Long, entityId: Long): String =
        client.execute(HttpMethod.Post, "${endpoint.path}/webhooks/test/$webhookId", retryable = false) {
            parameter("entityId", entityId)
        }.bodyAsText()
}
