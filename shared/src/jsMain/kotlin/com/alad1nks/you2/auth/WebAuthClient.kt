package com.alad1nks.you2.auth

import kotlin.js.Promise

/** Public Firebase web configuration. Credentials and session persistence belong to the SDK. */
private val firebaseAuth: WebFirebaseAuth by lazy {
    val options = js("({})")
    options.apiKey = "AIzaSyDxS2_csqcYwEnBERyySF8OZ-tNtUctBEw"
    options.authDomain = "you2-ae09a.firebaseapp.com"
    options.projectId = "you2-ae09a"
    options.storageBucket = "you2-ae09a.firebasestorage.app"
    options.messagingSenderId = "1084716048064"
    options.appId = "1:1084716048064:web:8cc1c5646cd0235f8b9725"
    getAuth(initializeApp(options))
}

internal class WebAuthClient : AuthClient {
    private val auth = firebaseAuth
    override val currentUser: AuthUser?
        get() = auth.currentUser?.let { AuthUser(it.uid, it.email.orEmpty()) }

    override var restoringSession: Boolean = true
        private set

    override fun observeUser(onChange: (AuthUser?) -> Unit): () -> Unit {
        var active = true
        val unsubscribe =
            onAuthStateChanged(auth) {
                if (active) {
                    restoringSession = false
                    onChange(currentUser)
                }
            }
        return {
            if (active) {
                active = false
                unsubscribe()
            }
        }
    }

    override fun signIn(email: String, password: String, complete: (AuthError?) -> Unit) =
        request(complete) { signInWithEmailAndPassword(auth, email, password) }

    override fun register(email: String, password: String, complete: (AuthError?) -> Unit) =
        request(complete) { createUserWithEmailAndPassword(auth, email, password) }

    override fun resetPassword(email: String, complete: (AuthError?) -> Unit) =
        request(complete) { sendPasswordResetEmail(auth, email) }

    override fun signOut(complete: (AuthError?) -> Unit) = request(complete) { signOut(auth) }

    fun launchGoogleSignIn(complete: (GoogleSignInResult) -> Unit) {
        // Invoke synchronously from the click handler so browsers allow opening the popup.
        try {
            signInWithPopup(auth, GoogleAuthProvider()).then<Unit>(
                onFulfilled = { complete(GoogleSignInResult.SignedIn) },
                onRejected = { failure ->
                    complete(
                        if (failure.asDynamic().code == "auth/popup-closed-by-user" ||
                            failure.asDynamic().code == "auth/cancelled-popup-request"
                        ) {
                            GoogleSignInResult.Cancelled
                        } else {
                            GoogleSignInResult.Failed
                        },
                    )
                },
            )
        } catch (_: Throwable) {
            complete(GoogleSignInResult.Failed)
        }
    }

    private fun <T> request(complete: (AuthError?) -> Unit, operation: () -> Promise<T>) {
        try {
            operation().then(
                onFulfilled = { complete(null) },
                onRejected = { complete(webAuthError(it.asDynamic().code as? String)) },
            )
        } catch (failure: Throwable) {
            complete(webAuthError(failure.asDynamic().code as? String))
        }
    }
}

internal fun webAuthError(code: String?): AuthError =
    when (code) {
        "auth/invalid-email" -> {
            AuthError.EMAIL
        }

        "auth/invalid-credential", "auth/user-not-found", "auth/wrong-password", "auth/user-disabled" -> {
            AuthError.CREDENTIALS
        }

        "auth/weak-password", "auth/password-does-not-meet-requirements" -> {
            AuthError.WEAK_PASSWORD
        }

        "auth/email-already-in-use", "auth/account-exists-with-different-credential" -> {
            AuthError.EMAIL_IN_USE
        }

        "auth/network-request-failed" -> {
            AuthError.NETWORK
        }

        "auth/too-many-requests" -> {
            AuthError.TOO_MANY_REQUESTS
        }

        else -> {
            AuthError.UNKNOWN
        }
    }
