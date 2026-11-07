package com.alad1nks.you2.di

import com.alad1nks.you2.auth.AuthClient
import com.alad1nks.you2.auth.WebAuthClient
import com.alad1nks.you2.core.storage.js.di.StorageJsModule
import org.koin.core.module.Module
import org.koin.dsl.module

actual val PlatformModules: List<Module> get() =
    listOf(
        StorageJsModule,
        module {
            single<AuthClient> { WebAuthClient() }
        },
    )
