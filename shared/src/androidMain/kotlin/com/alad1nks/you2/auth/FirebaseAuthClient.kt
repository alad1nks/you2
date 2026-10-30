package com.alad1nks.you2.auth

import com.google.firebase.FirebaseNetworkException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException

internal class FirebaseAuthClient(private val auth: FirebaseAuth = FirebaseAuth.getInstance()) : AuthClient {
    override val currentUser: AuthUser?
        get() = auth.currentUser?.let { AuthUser(it.uid, it.email.orEmpty()) }

    override fun observeUser(onChange: (AuthUser?) -> Unit): () -> Unit {
        val listener = FirebaseAuth.AuthStateListener { onChange(currentUser) }
        auth.addAuthStateListener(listener)
        return { auth.removeAuthStateListener(listener) }
    }

    override fun signIn(email: String, password: String, complete: (AuthError?) -> Unit) {
        auth.signInWithEmailAndPassword(email, password).addOnCompleteListener {
            complete(if (it.isSuccessful) null else it.exception.toAuthError())
        }
    }

    override fun register(email: String, password: String, complete: (AuthError?) -> Unit) {
        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener {
            complete(if (it.isSuccessful) null else it.exception.toAuthError())
        }
    }

    override fun resetPassword(email: String, complete: (AuthError?) -> Unit) {
        auth.sendPasswordResetEmail(email).addOnCompleteListener {
            // Give the same response for unknown addresses, including projects without enumeration protection.
            val error = it.exception
            complete(if (it.isSuccessful || error is FirebaseAuthInvalidUserException) null else error.toAuthError())
        }
    }

    override fun signOut() = auth.signOut()
}

private fun Exception?.toAuthError(): AuthError =
    when (this) {
        is FirebaseAuthWeakPasswordException -> AuthError.WEAK_PASSWORD
        is FirebaseAuthUserCollisionException -> AuthError.EMAIL_IN_USE
        is FirebaseAuthInvalidCredentialsException, is FirebaseAuthInvalidUserException -> AuthError.CREDENTIALS
        is FirebaseNetworkException -> AuthError.NETWORK
        is FirebaseTooManyRequestsException -> AuthError.TOO_MANY_REQUESTS
        else -> AuthError.UNKNOWN
    }
