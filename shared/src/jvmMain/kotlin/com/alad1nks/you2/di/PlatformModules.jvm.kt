package com.alad1nks.you2.di

import com.alad1nks.you2.auth.AuthClient
import com.alad1nks.you2.auth.DesktopAuthClient
import com.alad1nks.you2.auth.DesktopSessionStore
import com.alad1nks.you2.core.storage.datastore.di.StorageDataStoreModule
import org.koin.core.module.Module
import org.koin.dsl.module
import org.koin.dsl.onClose

actual val PlatformModules: List<Module> get() =
    listOf(
        StorageDataStoreModule,
    )

internal fun desktopPlatformModules(sessionStore: DesktopSessionStore): List<Module> {
    val authModule =
        module {
            val definition = single<AuthClient> { DesktopAuthClient(store = sessionStore) }
            definition onClose { (it as? DesktopAuthClient)?.close() }
        }
    return PlatformModules + authModule
}
