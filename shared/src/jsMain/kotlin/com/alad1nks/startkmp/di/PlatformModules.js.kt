package com.alad1nks.startkmp.di

import com.alad1nks.startkmp.core.storage.js.di.StorageJsModule
import org.koin.core.module.Module

actual val PlatformModules: List<Module> get() =
    listOf(
        StorageJsModule,
    )
