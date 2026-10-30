package com.alad1nks.you2.auth

import androidx.compose.runtime.Composable

internal sealed interface GoogleSignInResult {
    data class Token(val value: String) : GoogleSignInResult

    data object Cancelled : GoogleSignInResult

    data object Failed : GoogleSignInResult
}

internal typealias GoogleSignInLauncher = ((GoogleSignInResult) -> Unit) -> Unit

@Composable
internal expect fun rememberGoogleSignInLauncher(): GoogleSignInLauncher?
