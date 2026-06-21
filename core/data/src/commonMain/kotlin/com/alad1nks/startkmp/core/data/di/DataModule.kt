package com.alad1nks.startkmp.core.data.di

import com.alad1nks.startkmp.core.data.repository.SettingsRepository
import org.koin.dsl.module

val DataModule =
    module {
        single { SettingsRepository(get()) }
    }
