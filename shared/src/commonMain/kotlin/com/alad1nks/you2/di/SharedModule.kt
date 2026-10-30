package com.alad1nks.you2.di

import com.alad1nks.you2.auth.AuthViewModel
import com.alad1nks.you2.ui.MainViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

internal val SharedModule =
    module {
        viewModel { AuthViewModel(getOrNull()) }
        viewModel {
            MainViewModel(get(), get(), get(), get(), get(), get())
        }
    }
