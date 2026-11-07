package com.alad1nks.you2.auth

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import org.koin.compose.koinInject

@Composable
internal actual fun rememberGoogleSignInLauncher(): GoogleSignInLauncher? {
    val provider = koinInject<NativeAuthProvider>()
    return remember(provider) { nativeGoogleSignInLauncher(provider) }
}
