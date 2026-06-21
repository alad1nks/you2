package com.alad1nks.startkmp

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application

fun main() =
    application {
        Window(
            onCloseRequest = ::exitApplication,
            title = "startkmp",
        ) {
            KoinApp()
        }
    }
