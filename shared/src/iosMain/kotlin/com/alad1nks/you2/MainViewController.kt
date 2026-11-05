package com.alad1nks.you2

import androidx.compose.ui.window.ComposeUIViewController
import com.alad1nks.you2.auth.AuthClient
import com.alad1nks.you2.auth.NativeAuthClient
import com.alad1nks.you2.auth.NativeAuthProvider
import com.alad1nks.you2.di.PlatformModules
import com.alad1nks.you2.ui.App
import org.koin.dsl.module
import platform.UIKit.UIViewController

fun mainViewController(authProvider: NativeAuthProvider): UIViewController {
    val modules = PlatformModules + module { single<AuthClient> { NativeAuthClient(authProvider) } }
    return ComposeUIViewController { App(platformModules = modules) }
}
