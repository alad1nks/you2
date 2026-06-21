package com.alad1nks.startkmp.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.alad1nks.startkmp.Greeting
import com.alad1nks.startkmp.di.CommonModules
import com.alad1nks.startkmp.di.PlatformModules
import com.alad1nks.startkmp.feature.designsystem.theme.AppTheme
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.KoinApplication
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import org.koin.dsl.koinConfiguration
import startkmp.shared.generated.resources.Res
import startkmp.shared.generated.resources.compose_multiplatform

@Composable
fun App() {
    KoinApplication(
        configuration =
            koinConfiguration {
                modules(CommonModules + PlatformModules)
            },
    ) {
        val viewModel =
            koinViewModel<MainViewModel>(
                parameters = { parametersOf() },
            )
        val darkTheme by viewModel.darkTheme.collectAsState(isSystemInDarkTheme())
        val language by viewModel.language.collectAsState(null)

        AppTheme(
            darkTheme = darkTheme,
        ) {
            CompositionLocalProvider(
                LocalAppLocale provides language?.code,
            ) {
                key(language) {
                    AppContent()
                }
            }
        }
    }
}

@Composable
@Preview
private fun AppContent() {
    var showContent by remember { mutableStateOf(false) }
    Column(
        modifier =
            Modifier
                .background(MaterialTheme.colorScheme.primaryContainer)
                .safeContentPadding()
                .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Button(onClick = { showContent = !showContent }) {
            Text("Click me!")
        }
        AnimatedVisibility(showContent) {
            val greeting = remember { Greeting().greet() }
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Image(painterResource(Res.drawable.compose_multiplatform), null)
                Text("Compose: $greeting")
            }
        }
    }
}
