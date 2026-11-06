package com.alad1nks.you2.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import org.koin.compose.koinInject

@Composable
internal actual fun OverlayDialog(
    darkTheme: Boolean,
    onDismiss: () -> Unit,
    hideSystemBars: Boolean,
    content: @Composable () -> Unit,
) {
    val systemBars = koinInject<NativeSystemBarsProvider>()
    DisposableEffect(systemBars, hideSystemBars) {
        systemBars.setHidden(hideSystemBars)
        onDispose { systemBars.setHidden(false) }
    }
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false, usePlatformInsets = false),
        content = content,
    )
}
