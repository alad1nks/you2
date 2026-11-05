package com.alad1nks.you2

import androidx.compose.ui.window.ComposeUIViewController
import com.alad1nks.you2.auth.AuthClient
import com.alad1nks.you2.auth.NativeAuthClient
import com.alad1nks.you2.auth.NativeAuthProvider
import com.alad1nks.you2.cloud.NativeCloudProvider
import com.alad1nks.you2.cloud.NativeCloudRepository
import com.alad1nks.you2.core.data.repository.WishesRepository
import com.alad1nks.you2.di.PlatformModules
import com.alad1nks.you2.ui.App
import org.koin.dsl.module
import org.koin.dsl.onClose
import platform.UIKit.UIViewController

fun mainViewController(authProvider: NativeAuthProvider, cloudProvider: NativeCloudProvider): UIViewController {
    val modules =
        PlatformModules +
            module {
                single<AuthClient> { NativeAuthClient(authProvider) }
                single<WishesRepository> { NativeCloudRepository(cloudProvider, get(), get()) } onClose {
                    (it as? NativeCloudRepository)?.close()
                }
            }
    return ComposeUIViewController { App(platformModules = modules) }
}
