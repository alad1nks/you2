package com.alad1nks.you2.di

import com.alad1nks.you2.core.storage.js.di.StorageJsModule
import org.koin.core.module.Module

actual val PlatformModules: List<Module> get() =
    listOf(
        StorageJsModule,
    )
