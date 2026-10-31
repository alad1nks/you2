package com.alad1nks.you2.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

@Composable
internal actual fun OverlayDialog(
    darkTheme: Boolean,
    onDismiss: () -> Unit,
    hideSystemBars: Boolean,
    content: @Composable () -> Unit,
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
        content = content,
    )
}
