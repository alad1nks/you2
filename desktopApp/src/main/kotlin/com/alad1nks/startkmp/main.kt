package com.alad1nks.startkmp

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.alad1nks.startkmp.ui.App

fun main() =
    application {
        Window(
            onCloseRequest = ::exitApplication,
            title = "startkmp",
        ) {
            App()
        }
    }
