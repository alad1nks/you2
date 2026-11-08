package com.alad1nks.you2.auth

import com.google.gson.JsonParser

/** Not a data class: default toString must never expose credentials in diagnostics. */
internal class DesktopGoogleConfig(val clientId: String, val clientSecret: String?) {
    companion object {
        fun load(): DesktopGoogleConfig? {
            val clientId = System.getenv("YOU2_GOOGLE_DESKTOP_CLIENT_ID")?.takeIf(String::isNotBlank)
            if (clientId != null) {
                return DesktopGoogleConfig(
                    clientId,
                    System.getenv("YOU2_GOOGLE_DESKTOP_CLIENT_SECRET")?.takeIf(String::isNotBlank),
                )
            }
            return DesktopGoogleConfig::class.java.getResourceAsStream("/you2-google-desktop-client.json")
                ?.bufferedReader()?.use { parse(it.readText()) }
        }

        fun parse(json: String): DesktopGoogleConfig? =
            try {
                val installed = JsonParser.parseString(json).asJsonObject.getAsJsonObject("installed")
                val clientId = installed?.get("client_id")?.asString?.takeIf(String::isNotBlank)
                clientId?.let {
                    DesktopGoogleConfig(
                        it,
                        installed.get("client_secret")?.asString?.takeIf(String::isNotBlank),
                    )
                }
            } catch (_: Exception) {
                null
            }
    }
}
