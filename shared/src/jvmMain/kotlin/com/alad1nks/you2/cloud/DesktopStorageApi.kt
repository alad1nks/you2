package com.alad1nks.you2.cloud

import java.net.URI
import java.net.URLEncoder
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Duration

internal class DesktopStorageFailure(val code: Int) : Exception()

/** Firebase client endpoint: all access is evaluated against Storage rules using the user's ID token. */
internal class DesktopStorageApi(
    bucket: String =
        System.getenv("YOU2_FIREBASE_STORAGE_BUCKET")?.takeIf { it.isNotBlank() }
            ?: "you2-ae09a.firebasestorage.app",
    endpoint: String = "https://firebasestorage.googleapis.com/v0",
) {
    private val root = "$endpoint/b/${encode(bucket)}/o"
    private val client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(15)).build()

    fun upload(token: String, path: String, bytes: ByteArray) {
        require(bytes.size in 1..MAX_IMAGE_BYTES)
        val boundary = java.util.UUID.randomUUID().toString()
        val metadata =
            com.google.gson.Gson().toJson(
                mapOf(
                    "name" to path,
                    "contentType" to "image/jpeg",
                    "contentEncoding" to "identity",
                    "contentDisposition" to "inline",
                ),
            )
        val prefix =
            "--$boundary\r\nContent-Type: application/json; charset=utf-8\r\n\r\n" +
                "$metadata\r\n--$boundary\r\nContent-Type: image/jpeg\r\n\r\n"
        val body = prefix.toByteArray() + bytes + "\r\n--$boundary--".toByteArray()
        request(token, "?name=${encode(path)}", "POST", body, "multipart/related; boundary=$boundary")
    }

    fun download(token: String, path: String): ByteArray = request(token, "/${encode(path)}?alt=media", "GET")

    fun delete(token: String, path: String) {
        try {
            request(token, "/${encode(path)}", "DELETE")
        } catch (failure: DesktopStorageFailure) {
            if (failure.code != 404) throw failure
        }
    }

    private fun request(
        token: String,
        suffix: String,
        method: String,
        bytes: ByteArray? = null,
        contentType: String? = null,
    ): ByteArray {
        val builder =
            HttpRequest.newBuilder(URI(root + suffix)).timeout(Duration.ofSeconds(30))
                .header("Authorization", "Firebase $token")
        if (bytes != null) {
            builder.header("Content-Type", requireNotNull(contentType))
                .header("X-Goog-Upload-Protocol", "multipart")
        }
        builder.method(
            method,
            bytes?.let(HttpRequest.BodyPublishers::ofByteArray) ?: HttpRequest.BodyPublishers.noBody(),
        )
        // Bound the subscriber itself, so a malicious/oversized response cannot exhaust memory.
        val response =
            client.send(
                builder.build(),
                HttpResponse.BodyHandler { BoundedImageSubscriber() },
            )
        if (response.statusCode() !in 200..299) throw DesktopStorageFailure(response.statusCode())
        return response.body()
    }
}

internal const val MAX_IMAGE_BYTES = 5_242_880

private fun encode(value: String): String = URLEncoder.encode(value, Charsets.UTF_8).replace("+", "%20")

private class BoundedImageSubscriber : HttpResponse.BodySubscriber<ByteArray> {
    private val delegate = HttpResponse.BodySubscribers.ofByteArray()
    private var subscription: java.util.concurrent.Flow.Subscription? = null
    private var received = 0L

    override fun getBody() = delegate.body

    override fun onSubscribe(value: java.util.concurrent.Flow.Subscription) {
        subscription = value
        delegate.onSubscribe(value)
    }

    override fun onNext(items: List<java.nio.ByteBuffer>) {
        received += items.sumOf { it.remaining().toLong() }
        if (received > MAX_IMAGE_BYTES) {
            subscription?.cancel()
            delegate.onError(java.io.IOException("Image exceeds size limit"))
        } else {
            delegate.onNext(items)
        }
    }

    override fun onError(error: Throwable) = delegate.onError(error)

    override fun onComplete() = delegate.onComplete()
}
