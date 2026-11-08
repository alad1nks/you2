package com.alad1nks.you2

import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.alad1nks.you2.ui.App

fun main() =
    application {
        Window(
            state = rememberWindowState(width = 1280.dp, height = 900.dp),
            onCloseRequest = ::exitApplication,
            title = "you2",
            icon = painterResource("app-icon.png"),
        ) {
            App()
        }
    }
