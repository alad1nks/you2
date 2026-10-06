package com.alad1nks.you2.core.storage.js.di

import com.alad1nks.you2.core.storage.common.AppPreferences
import com.alad1nks.you2.core.storage.js.AppPreferencesImpl
import org.koin.dsl.module

val StorageJsModule =
    module {
        single<AppPreferences> { AppPreferencesImpl() }
    }
