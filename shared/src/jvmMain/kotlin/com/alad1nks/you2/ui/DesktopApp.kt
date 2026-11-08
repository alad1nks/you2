package com.alad1nks.you2.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.alad1nks.you2.auth.DesktopSessionStore
import com.alad1nks.you2.di.desktopPlatformModules

/** Supplies the desktop host's credential store without exposing DI implementation types to it. */
@Composable
fun DesktopApp(sessionStore: DesktopSessionStore) {
    val modules = remember(sessionStore) { desktopPlatformModules(sessionStore) }
    App(platformModules = modules)
}
