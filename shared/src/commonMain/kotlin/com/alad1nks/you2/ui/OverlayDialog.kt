package com.alad1nks.you2.ui

import androidx.compose.runtime.Composable

@Composable
internal expect fun OverlayDialog(darkTheme: Boolean, onDismiss: () -> Unit, content: @Composable () -> Unit)
