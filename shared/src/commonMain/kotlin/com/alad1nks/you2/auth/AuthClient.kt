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
    GOOGLE,
}

internal interface AuthClient {
    val currentUser: AuthUser?
    val restoringSession: Boolean get() = false

    fun observeUser(onChange: (AuthUser?) -> Unit): () -> Unit

    fun signIn(email: String, password: String, complete: (AuthError?) -> Unit)

    fun signInWithGoogle(idToken: String, complete: (AuthError?) -> Unit) = complete(AuthError.GOOGLE)

    fun register(email: String, password: String, complete: (AuthError?) -> Unit)

    fun resetPassword(email: String, complete: (AuthError?) -> Unit)

    fun signOut(): AuthError? = AuthError.UNKNOWN

    fun signOut(complete: (AuthError?) -> Unit) = complete(signOut())
}
