package com.alad1nks.you2.auth

internal data class AuthUser(val id: String, val email: String)

internal enum class AuthError {
    EMAIL,
    PASSWORD,
    PASSWORD_MISMATCH,
    CREDENTIALS,
    WEAK_PASSWORD,
    EMAIL_IN_USE,
    NETWORK,
    TOO_MANY_REQUESTS,
    UNKNOWN,
}

internal interface AuthClient {
    val currentUser: AuthUser?

    fun observeUser(onChange: (AuthUser?) -> Unit): () -> Unit

    fun signIn(email: String, password: String, complete: (AuthError?) -> Unit)

    fun register(email: String, password: String, complete: (AuthError?) -> Unit)

    fun resetPassword(email: String, complete: (AuthError?) -> Unit)

    fun signOut()
}
