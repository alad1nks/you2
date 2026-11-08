package com.alad1nks.you2.auth

import com.sun.net.httpserver.HttpServer
import java.net.InetSocketAddress
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull

class DesktopHttpTest {
    @Test
    fun sendsJsonAndParsesFirebaseResponsesWithoutLeakingErrors() {
        val server = HttpServer.create(InetSocketAddress("127.0.0.1", 0), 0)
        var status = 200
        var response = """{"localId":"user"}"""
        var body: String? = null
        var contentType: String? = null
        server.createContext("/") { exchange ->
            exchange.use {
                body = exchange.requestBody.bufferedReader().readText()
                contentType = exchange.requestHeaders.getFirst("Content-Type")
                val bytes = response.toByteArray()
                exchange.sendResponseHeaders(status, bytes.size.toLong())
                exchange.responseBody.write(bytes)
            }
        }
        server.start()
        try {
            val url = "http://127.0.0.1:${server.address.port}/"
            val http = DesktopHttp()
            assertEquals("user", http.post(url, "{}").get("localId").asString)
            assertEquals("{}", body)
            assertEquals("application/json", contentType)
            status = 400
            response = """{"error":{"message":"INVALID_LOGIN_CREDENTIALS"}}"""
            val failure = assertFailsWith<DesktopAuthFailure> { http.post(url, "{}") }
            assertEquals(AuthError.CREDENTIALS, failure.error)
            assertNull(failure.message)
            status = 503
            response = "not JSON"
            assertEquals(AuthError.NETWORK, assertFailsWith<DesktopAuthFailure> { http.post(url, "{}") }.error)
            status = 429
            assertEquals(
                AuthError.TOO_MANY_REQUESTS,
                assertFailsWith<DesktopAuthFailure> { http.post(url, "{}") }.error,
            )
        } finally {
            server.stop(0)
        }
    }
}
