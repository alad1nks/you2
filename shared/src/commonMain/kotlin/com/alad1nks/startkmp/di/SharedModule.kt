package com.alad1nks.startkmp.di

import com.alad1nks.startkmp.ui.MainViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

internal val SharedModule =
    module {
        viewModel {
            MainViewModel(get(), get())
        }
    }
