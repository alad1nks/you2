package com.alad1nks.you2

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.alad1nks.you2.ui.App

fun main() =
    application {
        Window(
            onCloseRequest = ::exitApplication,
            title = "you2",
        ) {
            App()
        }
    }
