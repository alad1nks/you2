package com.alad1nks.you2.cloud

import com.alad1nks.you2.auth.AuthError
import com.alad1nks.you2.auth.DesktopAuthFailure
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.sun.net.httpserver.HttpServer
import java.io.IOException
import java.net.InetSocketAddress
import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse

class DesktopFirestoreTest {
    @Test
    fun diagnosticHintsDoNotExposeServerMessages() {
        fun hint(message: String) = firestoreErrorHint(JsonObject().apply { addProperty("message", message) })
        assertEquals("APP_CHECK", hint("Firebase App Check token is invalid"))
        assertEquals("SECURITY_RULES", hint("Missing or insufficient permissions."))
        assertEquals("UNSPECIFIED", hint("sensitive server detail"))
        assertEquals("UNSPECIFIED", firestoreErrorHint(null))
    }

    @Test
    fun wireValuesPreserveNullsTimestampsAndNestedCollections() {
        val fields =
            mapOf(
                "content" to mapOf("cover" to null, "attachments" to emptyList<String>(), "title" to "A walk"),
                "completion" to null,
                "createdAt" to Instant.parse("2026-11-08T01:02:03.123Z"),
                "members" to listOf("alice", "bob"),
            )
        val json = Gson().toJsonTree(mapOf("fields" to fields.mapValues { firestoreValue(it.value) })).asJsonObject
        assertEquals(fields, firestoreFields(json))
    }

    @Test
    fun transportUsesBearerTokenAndRedactsServerErrors() {
        val server = HttpServer.create(InetSocketAddress("127.0.0.1", 0), 0)
        var authorization: String? = null
        var received: JsonObject? = null
        server.createContext("/") { exchange ->
            authorization = exchange.requestHeaders.getFirst("Authorization")
            received =
                com.google.gson.JsonParser.parseString(
                    exchange.requestBody.bufferedReader().readText(),
                ).asJsonObject
            val body = """{"error":{"status":"PERMISSION_DENIED","message":"sensitive server detail"}}""".toByteArray()
            exchange.sendResponseHeaders(403, body.size.toLong())
            exchange.responseBody.use { it.write(body) }
        }
        server.start()
        try {
            val api = DesktopFirestoreApi("demo-you2", "http://127.0.0.1:${server.address.port}/v1")
            val error =
                assertFailsWith<DesktopFirestoreFailure> {
                    api.get("test-token", "users/alice", "2026-11-08T00:00:00Z")
                }
            assertEquals("Bearer test-token", authorization)
            assertEquals("2026-11-08T00:00:00Z", received?.get("readTime")?.asString)
            assertFalse(received!!.has("transaction"))
            assertEquals("PERMISSION_DENIED", error.status)
            assertFalse(error.toString().contains("sensitive"))
        } finally {
            server.stop(0)
        }
    }

    @Test
    fun failuresDistinguishNetworkAccessAndInvalidInvitations() {
        assertEquals(NativeCloudFailure.NETWORK, cloudFailure(IOException()))
        assertEquals(NativeCloudFailure.NETWORK, cloudFailure(DesktopAuthFailure(AuthError.NETWORK)))
        assertEquals(NativeCloudFailure.ACCESS, cloudFailure(DesktopAuthFailure(AuthError.CREDENTIALS)))
        assertEquals(NativeCloudFailure.ACCESS, cloudFailure(DesktopFirestoreFailure("PERMISSION_DENIED")))
        assertEquals(NativeCloudFailure.INVITE, cloudFailure(DesktopFirestoreFailure("PERMISSION_DENIED"), true))
        assertEquals(NativeCloudFailure.NETWORK, cloudFailure(DesktopFirestoreFailure("ABORTED"), true))
    }
}
