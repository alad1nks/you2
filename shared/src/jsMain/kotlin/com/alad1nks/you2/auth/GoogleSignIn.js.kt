package com.alad1nks.you2.auth

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import org.koin.compose.koinInject

@Composable
internal actual fun rememberGoogleSignInLauncher(): GoogleSignInLauncher? {
    val client = koinInject<AuthClient>() as? WebAuthClient ?: return null
    return remember(client) { client::launchGoogleSignIn }
}
