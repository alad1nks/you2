package com.alad1nks.you2.cloud

import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Duration
import java.time.Instant
import java.util.logging.Logger

internal class DesktopFirestoreFailure(val status: String) : Exception()

internal data class DesktopDocumentRead(
    val fields: Map<String, Any?>?,
    val updateTime: String?,
    val readTime: String,
)

/** User-token REST transport: never uses Admin credentials or includes server bodies in errors. */
internal class DesktopFirestoreApi(
    projectId: String = System.getenv("YOU2_FIREBASE_PROJECT_ID")?.takeIf { it.isNotBlank() } ?: "you2-ae09a",
    private val endpoint: String = "https://firestore.googleapis.com/v1",
) {
    val root = "projects/$projectId/databases/(default)/documents"
    private val client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(15)).build()
    private val gson = Gson()
    private val logger = Logger.getLogger("you2.firestore")

    init {
        require(Regex("[a-zA-Z0-9-]+").matches(projectId))
    }

    fun request(token: String, suffix: String, body: Any? = null): JsonElement {
        val builder =
            HttpRequest.newBuilder(URI("$endpoint/$root$suffix"))
                .timeout(Duration.ofSeconds(30)).header("Authorization", "Bearer $token")
        if (body != null) {
            builder.header(
                "Content-Type",
                "application/json",
            ).POST(HttpRequest.BodyPublishers.ofString(gson.toJson(body)))
        }
        val response = client.send(builder.build(), HttpResponse.BodyHandlers.ofString())
        if (response.statusCode() !in 200..299) {
            val error =
                runCatching {
                    JsonParser.parseString(response.body()).asJsonObject.getAsJsonObject("error")
                }.getOrNull()
            val status =
                runCatching {
                    error?.get("status")?.asString
                }.getOrNull() ?: when (response.statusCode()) {
                    401 -> "UNAUTHENTICATED"
                    403 -> "PERMISSION_DENIED"
                    429 -> "RESOURCE_EXHAUSTED"
                    in 500..599 -> "UNAVAILABLE"
                    else -> "UNKNOWN"
                }
            // Report only fixed labels, never document paths, response bodies, or credentials.
            val operation =
                suffix.substringAfterLast(':').takeIf {
                    it in setOf("beginTransaction", "batchGet", "runQuery", "commit", "rollback")
                } ?: "request"
            val safeStatus = status.takeIf { Regex("[A-Z_]{1,40}").matches(it) } ?: "UNKNOWN"
            logger.warning(
                "Firestore $operation failed: HTTP ${response.statusCode()}, $safeStatus, " +
                    "hint=${firestoreErrorHint(error)}",
            )
            throw DesktopFirestoreFailure(status)
        }
        return JsonParser.parseString(response.body())
    }

    fun get(token: String, path: String, readTime: String? = null): DesktopDocumentRead =
        request(
            token,
            ":batchGet",
            buildMap {
                put("documents", listOf("$root/$path"))
                if (readTime != null) put("readTime", readTime)
            },
        )
            .asJsonArray.single().asJsonObject.let { result ->
                val found = result.getAsJsonObject("found")
                check(found != null || result.has("missing"))
                DesktopDocumentRead(
                    found?.let(::firestoreFields),
                    found?.get("updateTime")?.asString,
                    result.get("readTime").asString,
                )
            }

    fun wishes(token: String, pair: String, readTime: String): List<NativeCloudDocument> =
        request(
            token,
            "/couples/$pair:runQuery",
            mapOf(
                "readTime" to readTime,
                "structuredQuery" to
                    mapOf(
                        "from" to listOf(mapOf("collectionId" to "wishes")),
                        "orderBy" to
                            listOf(
                                mapOf("field" to mapOf("fieldPath" to "createdAt"), "direction" to "DESCENDING"),
                            ),
                    ),
            ),
        ).asJsonArray.mapNotNull { row ->
            row.asJsonObject.getAsJsonObject("document")?.let {
                NativeCloudDocument(it.get("name").asString.substringAfterLast('/'), firestoreFields(it))
            }
        }

    fun update(
        path: String,
        fields: Map<String, Any?>,
        mask: List<String>? = null,
        timestamps: List<String> = emptyList(),
        exists: Boolean? = null,
    ): Map<String, Any> =
        buildMap {
            put("update", mapOf("name" to "$root/$path", "fields" to fields.mapValues { firestoreValue(it.value) }))
            if (mask != null) put("updateMask", mapOf("fieldPaths" to mask))
            if (exists != null) put("currentDocument", mapOf("exists" to exists))
            if (timestamps.isNotEmpty()) {
                put(
                    "updateTransforms",
                    timestamps.map {
                        mapOf("fieldPath" to it, "setToServerValue" to "REQUEST_TIME")
                    },
                )
            }
        }
}

internal fun firestoreErrorHint(error: JsonObject?): String {
    val message = runCatching { error?.get("message")?.asString?.lowercase() }.getOrNull().orEmpty()
    return when {
        "app check" in message || "appcheck" in message -> "APP_CHECK"
        "has not been used" in message || "service_disabled" in message -> "API_DISABLED"
        "missing or insufficient permissions" in message -> "SECURITY_RULES"
        "audience" in message || "different project" in message -> "TOKEN_PROJECT_MISMATCH"
        "expired" in message -> "TOKEN_EXPIRED"
        else -> "UNSPECIFIED"
    }
}

internal fun firestoreValue(value: Any?): Map<String, Any?> =
    when (value) {
        null -> {
            mapOf("nullValue" to "NULL_VALUE")
        }

        is String -> {
            mapOf("stringValue" to value)
        }

        is Boolean -> {
            mapOf("booleanValue" to value)
        }

        is Instant -> {
            mapOf("timestampValue" to value.toString())
        }

        is Long, is Int -> {
            mapOf("integerValue" to value.toString())
        }

        is Double -> {
            mapOf("doubleValue" to value)
        }

        is Map<*, *> -> {
            mapOf(
                "mapValue" to
                    mapOf(
                        "fields" to
                            value.entries.associate {
                                (key, item) ->
                                key as String to firestoreValue(item)
                            },
                    ),
            )
        }

        is List<*> -> {
            mapOf("arrayValue" to mapOf("values" to value.map(::firestoreValue)))
        }

        else -> {
            error("Unsupported Firestore value")
        }
    }

internal fun firestoreFields(document: JsonObject): Map<String, Any?> =
    document.getAsJsonObject("fields")?.entrySet()?.associate {
        it.key to firestoreDecode(it.value.asJsonObject)
    } ?: emptyMap()

private fun firestoreDecode(value: JsonObject): Any? =
    when {
        value.has("nullValue") -> {
            null
        }

        value.has("stringValue") -> {
            value.get("stringValue").asString
        }

        value.has("timestampValue") -> {
            Instant.parse(value.get("timestampValue").asString)
        }

        value.has("booleanValue") -> {
            value.get("booleanValue").asBoolean
        }

        value.has("integerValue") -> {
            value.get("integerValue").asLong
        }

        value.has("doubleValue") -> {
            value.get("doubleValue").asDouble
        }

        value.has("mapValue") -> {
            firestoreFields(value.getAsJsonObject("mapValue"))
        }

        value.has("arrayValue") -> {
            value.getAsJsonObject("arrayValue").getAsJsonArray("values")?.map {
                firestoreDecode(it.asJsonObject)
            } ?: emptyList<Any?>()
        }

        else -> {
            error("Unsupported Firestore value")
        }
    }
