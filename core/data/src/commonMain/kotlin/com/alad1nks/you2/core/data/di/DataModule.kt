package com.alad1nks.you2.core.data.di

import com.alad1nks.you2.core.data.repository.SettingsRepository
import org.koin.dsl.module

val DataModule =
    module {
        single { SettingsRepository(get()) }
    }
