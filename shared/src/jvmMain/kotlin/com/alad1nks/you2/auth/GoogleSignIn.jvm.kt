package com.alad1nks.you2.auth

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runInterruptible

@Composable
internal actual fun rememberGoogleSignInLauncher(): GoogleSignInLauncher? {
    val config = remember { DesktopGoogleConfig.load() } ?: return null
    val scope = rememberCoroutineScope()
    return remember(config, scope) {
        val google =
            DesktopGoogleSignIn(config.clientId, config.clientSecret)
        val launcher: GoogleSignInLauncher = { complete ->
            scope.launch {
                val result =
                    try {
                        runInterruptible(Dispatchers.IO) { google.signIn() }
                    } catch (failure: CancellationException) {
                        throw failure
                    } catch (_: Exception) {
                        GoogleSignInResult.Failed
                    }
                complete(result)
            }
        }
        launcher
    }
}
