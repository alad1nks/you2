package com.alad1nks.you2.auth

/** Bridge implemented by the Apple host. Calls and callbacks must run on the main thread. */
interface NativeAuthProvider {
    val currentUser: NativeAuthUser?

    /** Returns an idempotent unsubscribe function; removed listeners must not deliver queued events. */
    fun observeUser(onChange: (NativeAuthUser?) -> Unit): () -> Unit

    fun signIn(email: String, password: String, complete: (NativeAuthFailure?) -> Unit)

    fun register(email: String, password: String, complete: (NativeAuthFailure?) -> Unit)

    fun resetPassword(email: String, complete: (NativeAuthFailure?) -> Unit)

    fun signOut(): NativeAuthFailure?
}

data class NativeAuthUser(val id: String, val email: String)

enum class NativeAuthFailure { EMAIL, CREDENTIALS, WEAK_PASSWORD, EMAIL_IN_USE, NETWORK, TOO_MANY_REQUESTS, UNKNOWN }

internal class NativeAuthClient(private val provider: NativeAuthProvider) : AuthClient {
    override val currentUser: AuthUser? get() = provider.currentUser?.toAuthUser()

    override fun observeUser(onChange: (AuthUser?) -> Unit): () -> Unit =
        provider.observeUser { onChange(it?.toAuthUser()) }

    override fun signIn(email: String, password: String, complete: (AuthError?) -> Unit) =
        provider.signIn(email, password) { complete(it?.toAuthError()) }

    override fun register(email: String, password: String, complete: (AuthError?) -> Unit) =
        provider.register(email, password) { complete(it?.toAuthError()) }

    override fun resetPassword(email: String, complete: (AuthError?) -> Unit) =
        provider.resetPassword(email) { complete(it?.toAuthError()) }

    override fun signOut(): AuthError? = provider.signOut()?.toAuthError()
}

private fun NativeAuthUser.toAuthUser() = AuthUser(id, email)

private fun NativeAuthFailure.toAuthError(): AuthError =
    when (this) {
        NativeAuthFailure.EMAIL -> AuthError.EMAIL
        NativeAuthFailure.CREDENTIALS -> AuthError.CREDENTIALS
        NativeAuthFailure.WEAK_PASSWORD -> AuthError.WEAK_PASSWORD
        NativeAuthFailure.EMAIL_IN_USE -> AuthError.EMAIL_IN_USE
        NativeAuthFailure.NETWORK -> AuthError.NETWORK
        NativeAuthFailure.TOO_MANY_REQUESTS -> AuthError.TOO_MANY_REQUESTS
        NativeAuthFailure.UNKNOWN -> AuthError.UNKNOWN
    }
