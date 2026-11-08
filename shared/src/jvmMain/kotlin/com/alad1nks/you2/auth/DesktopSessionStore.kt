package com.alad1nks.you2.auth

/** Desktop host provides OS storage; the shared module remains compatible with Java 21. */
interface DesktopSessionStore {
    fun read(): String?

    fun write(refreshToken: String)

    fun clear()
}
