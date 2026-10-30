package com.alad1nks.you2.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import com.alad1nks.you2.auth.AuthViewModel
import com.alad1nks.you2.core.model.Language
import com.alad1nks.you2.di.CommonModules
import com.alad1nks.you2.di.PlatformModules
import com.alad1nks.you2.feature.designsystem.theme.AppTheme
import org.koin.compose.KoinApplication
import org.koin.compose.viewmodel.koinViewModel
import org.koin.dsl.koinConfiguration

@Composable
fun App(onDarkThemeChanged: (Boolean) -> Unit = {}) {
    KoinApplication(configuration = koinConfiguration { modules(CommonModules + PlatformModules) }) {
        val viewModel = koinViewModel<MainViewModel>()
        val authViewModel = koinViewModel<AuthViewModel>()
        val auth by authViewModel.state.collectAsState()
        // Keep the destination across auth changes; private screen state stays keyed by account.
        val tabState = rememberSaveable { mutableStateOf(MainTab.WISHES) }
        val darkTheme by viewModel.darkTheme.collectAsState(isSystemInDarkTheme())
        SideEffect { onDarkThemeChanged(darkTheme) }
        val language by viewModel.language.collectAsState(Language.SYSTEM)
        val space by viewModel.space.collectAsState()
        AppTheme(darkTheme = darkTheme) {
            CompositionLocalProvider(LocalAppLocale provides language.code) {
                Surface(Modifier.fillMaxSize()) {
                    key(auth.user?.id) {
                        if (authViewModel.isAvailable && auth.user == null) {
                            GuestExperience(auth, authViewModel, language, viewModel::changeLanguage, tabState)
                        } else {
                            CloudSpaceGate(
                                viewModel.cloud,
                                auth.user?.id,
                                authViewModel::signOut,
                                darkTheme,
                                language,
                                viewModel::changeDarkTheme,
                                viewModel::changeLanguage,
                                tabState,
                            ) {
                                AppContent(
                                    space,
                                    viewModel.today,
                                    viewModel::deleteWish,
                                    viewModel::completeWish,
                                    viewModel::saveWish,
                                    viewModel::restoreWish,
                                    viewModel::editMemory,
                                    darkTheme,
                                    language,
                                    viewModel::renameUser,
                                    viewModel::changeDarkTheme,
                                    viewModel::changeLanguage,
                                    onSignOut = if (authViewModel.isAvailable) authViewModel::signOut else null,
                                    initialTab = tabState.value,
                                    onTabChanged = { tabState.value = it },
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
