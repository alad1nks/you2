package com.alad1nks.you2.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.alad1nks.you2.di.CommonModules
import com.alad1nks.you2.di.PlatformModules
import com.alad1nks.you2.feature.designsystem.theme.AppTheme
import org.koin.compose.KoinApplication
import org.koin.compose.viewmodel.koinViewModel
import org.koin.dsl.koinConfiguration

@Composable
fun App() {
    KoinApplication(configuration = koinConfiguration { modules(CommonModules + PlatformModules) }) {
        val viewModel = koinViewModel<MainViewModel>()
        val darkTheme by viewModel.darkTheme.collectAsState(isSystemInDarkTheme())
        val language by viewModel.language.collectAsState(null)
        val space by viewModel.space.collectAsState()
        AppTheme(darkTheme = darkTheme) {
            CompositionLocalProvider(LocalAppLocale provides language?.code) {
                AppContent(space, viewModel.today, viewModel::deleteWish, viewModel::completeWish, viewModel::saveWish)
            }
        }
    }
}
