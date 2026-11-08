package com.alad1nks.you2.auth

import com.sun.net.httpserver.HttpServer
import java.awt.Desktop
import java.net.InetSocketAddress
import java.net.URI
import java.net.URLDecoder
import java.security.MessageDigest
import java.security.SecureRandom
import java.util.Base64
import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.TimeUnit

/** System browser + loopback redirect, with state binding and PKCE. No embedded browser. */
internal class DesktopGoogleSignIn(private val clientId: String, private val clientSecret: String?) {
    fun signIn(): GoogleSignInResult {
        val state = randomToken()
        val verifier = randomToken()
        val challenge =
            Base64.getUrlEncoder().withoutPadding()
                .encodeToString(MessageDigest.getInstance("SHA-256").digest(verifier.toByteArray(Charsets.US_ASCII)))
        val result = ArrayBlockingQueue<Map<String, String>>(1)
        val server = HttpServer.create(InetSocketAddress("127.0.0.1", 0), 0)
        val redirect = "http://127.0.0.1:${server.address.port}/callback"
        try {
            server.createContext("/callback") { exchange ->
                exchange.use {
                    val params = parseOAuthCallback(exchange.requestURI.rawQuery)
                    val valid =
                        exchange.requestMethod == "GET" &&
                            exchange.requestURI.path == "/callback" && params["state"] == state
                    val message = if (valid) "Return to You2 to continue." else "Invalid sign-in callback."
                    val bytes = message.toByteArray()
                    exchange.responseHeaders.add("Content-Type", "text/plain; charset=utf-8")
                    exchange.responseHeaders.add("Cache-Control", "no-store")
                    exchange.sendResponseHeaders(if (valid) 200 else 400, bytes.size.toLong())
                    exchange.responseBody.write(bytes)
                    if (valid) result.offer(params)
                }
            }
            server.start()
            val query =
                formBody(
                    mapOf(
                        "client_id" to clientId,
                        "redirect_uri" to redirect,
                        "response_type" to "code",
                        "scope" to "openid email",
                        "state" to state,
                        "code_challenge" to challenge,
                        "code_challenge_method" to "S256",
                    ),
                )
            Desktop.getDesktop().browse(URI("https://accounts.google.com/o/oauth2/v2/auth?$query"))
            val callback = result.poll(3, TimeUnit.MINUTES) ?: return GoogleSignInResult.Cancelled
            if (callback["error"] == "access_denied") return GoogleSignInResult.Cancelled
            val code = callback["code"] ?: return GoogleSignInResult.Failed
            val values =
                mutableMapOf(
                    "client_id" to clientId,
                    "code" to code,
                    "code_verifier" to verifier,
                    "grant_type" to "authorization_code",
                    "redirect_uri" to redirect,
                )
            clientSecret?.let { values["client_secret"] = it }
            val token =
                DesktopHttp().post(
                    "https://oauth2.googleapis.com/token",
                    formBody(values),
                    "application/x-www-form-urlencoded",
                ).get("id_token")?.asString
            return token?.let(GoogleSignInResult::Token) ?: GoogleSignInResult.Failed
        } finally {
            server.stop(0)
        }
    }

    private fun randomToken(): String =
        Base64.getUrlEncoder().withoutPadding()
            .encodeToString(ByteArray(32).also { SecureRandom().nextBytes(it) })
}

internal fun parseOAuthCallback(query: String?): Map<String, String> =
    try {
        query.orEmpty().split('&').filter { '=' in it }.associate {
            URLDecoder.decode(it.substringBefore('='), "UTF-8") to URLDecoder.decode(it.substringAfter('='), "UTF-8")
        }
    } catch (_: IllegalArgumentException) {
        emptyMap()
    }
