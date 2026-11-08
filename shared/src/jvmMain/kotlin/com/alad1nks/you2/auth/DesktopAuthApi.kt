package com.alad1nks.you2.auth

import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import java.net.URI
import java.net.URLEncoder
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Duration

internal const val DESKTOP_FIREBASE_API_KEY = "AIzaSyDxS2_csqcYwEnBERyySF8OZ-tNtUctBEw"

internal class DesktopAuthFailure(val error: AuthError) : Exception()

// Never include response bodies in exceptions: successful responses contain credentials.
internal class DesktopHttp {
    private val client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(15)).build()

    fun post(url: String, body: String, contentType: String = "application/json"): JsonObject {
        val request =
            HttpRequest.newBuilder(URI(url)).timeout(Duration.ofSeconds(30))
                .header("Content-Type", contentType).POST(HttpRequest.BodyPublishers.ofString(body)).build()
        val response = client.send(request, HttpResponse.BodyHandlers.ofString())
        if (response.statusCode() == 429) throw DesktopAuthFailure(AuthError.TOO_MANY_REQUESTS)
        if (response.statusCode() >= 500) throw DesktopAuthFailure(AuthError.NETWORK)
        val json =
            try {
                JsonParser.parseString(response.body()).asJsonObject
            } catch (_: Exception) {
                throw DesktopAuthFailure(AuthError.UNKNOWN)
            }
        if (response.statusCode() !in 200..299) {
            val error = json.get("error")
            val message = if (error?.isJsonObject == true) error.asJsonObject.get("message")?.asString else null
            throw DesktopAuthFailure(desktopAuthError(message))
        }
        return json
    }
}

internal fun formBody(values: Map<String, String>): String =
    values.entries.joinToString("&") { (key, value) -> "${encodeForm(key)}=${encodeForm(value)}" }

private fun encodeForm(value: String): String = URLEncoder.encode(value, "UTF-8")

internal open class DesktopAuthApi(
    private val apiKey: String =
        System.getenv("YOU2_FIREBASE_API_KEY")?.takeIf {
            it.isNotBlank()
        } ?: DESKTOP_FIREBASE_API_KEY,
    private val http: DesktopHttp = DesktopHttp(),
) {
    open fun request(method: String, values: Map<String, Any>): JsonObject =
        http.post("https://identitytoolkit.googleapis.com/v1/accounts:$method?key=$apiKey", Gson().toJson(values))

    open fun refresh(token: String): JsonObject =
        http.post(
            "https://securetoken.googleapis.com/v1/token?key=$apiKey",
            formBody(mapOf("grant_type" to "refresh_token", "refresh_token" to token)),
            "application/x-www-form-urlencoded",
        )
}

internal fun desktopAuthError(message: String?): AuthError =
    when (message?.substringBefore(" :")) {
        "INVALID_EMAIL" -> AuthError.EMAIL

        "INVALID_LOGIN_CREDENTIALS", "EMAIL_NOT_FOUND", "INVALID_PASSWORD", "USER_DISABLED",
        "USER_NOT_FOUND", "INVALID_REFRESH_TOKEN", "TOKEN_EXPIRED", "INVALID_ID_TOKEN",
        -> AuthError.CREDENTIALS

        "WEAK_PASSWORD", "PASSWORD_DOES_NOT_MEET_REQUIREMENTS" -> AuthError.WEAK_PASSWORD

        "EMAIL_EXISTS", "FEDERATED_USER_ID_ALREADY_LINKED" -> AuthError.EMAIL_IN_USE

        "TOO_MANY_ATTEMPTS_TRY_LATER" -> AuthError.TOO_MANY_REQUESTS

        else -> AuthError.UNKNOWN
    }
