@file:JsModule("firebase/auth")
@file:JsNonModule

package com.alad1nks.you2.auth

import kotlin.js.Promise

internal external interface WebFirebaseUser {
    val uid: String
    val email: String?
}

internal external interface WebFirebaseAuth {
    val currentUser: WebFirebaseUser?
}

internal external class GoogleAuthProvider()

internal external fun getAuth(app: dynamic): WebFirebaseAuth

internal external fun onAuthStateChanged(auth: WebFirebaseAuth, next: (WebFirebaseUser?) -> Unit): () -> Unit

internal external fun signInWithEmailAndPassword(
    auth: WebFirebaseAuth,
    email: String,
    password: String,
): Promise<dynamic>

internal external fun createUserWithEmailAndPassword(
    auth: WebFirebaseAuth,
    email: String,
    password: String,
): Promise<dynamic>

internal external fun sendPasswordResetEmail(auth: WebFirebaseAuth, email: String): Promise<Unit>

internal external fun signInWithPopup(auth: WebFirebaseAuth, provider: GoogleAuthProvider): Promise<dynamic>

internal external fun signOut(auth: WebFirebaseAuth): Promise<Unit>
