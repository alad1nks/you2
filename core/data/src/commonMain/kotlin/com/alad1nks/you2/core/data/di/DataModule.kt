package com.alad1nks.you2.core.data.di

import com.alad1nks.you2.core.data.repository.DemoDateSource
import com.alad1nks.you2.core.data.repository.InMemoryWishesRepository
import com.alad1nks.you2.core.data.repository.SettingsRepository
import com.alad1nks.you2.core.data.repository.WishesRepository
import com.alad1nks.you2.core.model.DateSource
import org.koin.dsl.module

val DataModule =
    module {
        single<DateSource> { DemoDateSource }
        single<WishesRepository> { InMemoryWishesRepository(get()) }
        single { SettingsRepository(get()) }
    }
