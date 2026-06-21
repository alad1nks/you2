package com.alad1nks.startkmp.core.domain.di

import com.alad1nks.startkmp.core.domain.GetDarkTheme
import com.alad1nks.startkmp.core.domain.GetDarkThemeImpl
import com.alad1nks.startkmp.core.domain.GetLanguage
import com.alad1nks.startkmp.core.domain.GetLanguageImpl
import com.alad1nks.startkmp.core.domain.SetDarkTheme
import com.alad1nks.startkmp.core.domain.SetDarkThemeImpl
import com.alad1nks.startkmp.core.domain.SetLanguage
import com.alad1nks.startkmp.core.domain.SetLanguageImpl
import org.koin.dsl.module

val DomainModule =
    module {
        factory<GetDarkTheme> { GetDarkThemeImpl(get()) }
        factory<GetLanguage> { GetLanguageImpl(get()) }
        factory<SetDarkTheme> { SetDarkThemeImpl(get()) }
        factory<SetLanguage> { SetLanguageImpl(get()) }
    }
