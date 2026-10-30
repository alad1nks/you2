package com.alad1nks.you2.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.alad1nks.you2.core.data.repository.CloudFailure
import com.alad1nks.you2.core.data.repository.CloudPhase
import com.alad1nks.you2.core.data.repository.CloudWishesRepository
import com.alad1nks.you2.core.model.Language
import com.alad1nks.you2.feature.designsystem.theme.AppSpacing
import com.alad1nks.you2.resources.generated.Res
import com.alad1nks.you2.resources.generated.cancel
import com.alad1nks.you2.resources.generated.cloud_access_error
import com.alad1nks.you2.resources.generated.cloud_cache
import com.alad1nks.you2.resources.generated.cloud_data_error
import com.alad1nks.you2.resources.generated.cloud_error
import com.alad1nks.you2.resources.generated.cloud_invite_error
import com.alad1nks.you2.resources.generated.cloud_loading
import com.alad1nks.you2.resources.generated.cloud_network_error
import com.alad1nks.you2.resources.generated.cloud_retry
import com.alad1nks.you2.resources.generated.cloud_saving
import com.alad1nks.you2.resources.generated.cloud_saving_hint
import com.alad1nks.you2.resources.generated.sign_out
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun CloudSpaceGate(
    repository: CloudWishesRepository?,
    userId: String?,
    onSignOut: () -> Unit,
    darkTheme: Boolean = false,
    language: Language = Language.SYSTEM,
    onSetDarkTheme: (Boolean) -> Unit = {},
    onSetLanguage: (Language) -> Unit = {},
    tabState: MutableState<MainTab> = rememberSaveable { mutableStateOf(MainTab.WISHES) },
    content: @Composable () -> Unit,
) {
    if (repository == null) {
        content()
        return
    }
    val state by repository.cloudState.collectAsState()
    var enteredUnpaired by rememberSaveable(userId) { mutableStateOf(false) }
    SideEffect {
        if (state.userId == userId && state.phase == CloudPhase.UNPAIRED) enteredUnpaired = true
    }
    val showUnpaired =
        state.userId == userId &&
            (state.phase == CloudPhase.UNPAIRED || (enteredUnpaired && state.phase != CloudPhase.ERROR))
    if (showUnpaired) {
        UnpairedExperience(
            state,
            repository,
            darkTheme,
            language,
            onSetDarkTheme,
            onSetLanguage,
            onSignOut,
            onConnected = {
                tabState.value = MainTab.WISHES
                enteredUnpaired = false
            },
            tabState = tabState,
        )
    } else if (state.userId == userId && state.phase == CloudPhase.READY) {
        Column(Modifier.fillMaxSize()) {
            if (state.fromCache) {
                Text(stringResource(Res.string.cloud_cache), Modifier.safeDrawingPadding().padding(AppSpacing.small))
            }
            content()
        }
    } else {
        Column(
            Modifier.fillMaxSize().safeDrawingPadding().verticalScroll(rememberScrollState()).padding(AppSpacing.page),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.medium, Alignment.CenterVertically),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            if (state.userId != userId || state.phase == CloudPhase.LOADING) {
                CircularProgressIndicator()
                Text(stringResource(Res.string.cloud_loading))
            } else {
                Text(stringResource(state.failure.message()))
                Button(repository::retry) { Text(stringResource(Res.string.cloud_retry)) }
            }
            TextButton(onSignOut) { Text(stringResource(Res.string.sign_out)) }
        }
    }
    if (!showUnpaired && state.userId == userId && state.busy) {
        AlertDialog(
            onDismissRequest = {},
            title = { Text(stringResource(Res.string.cloud_saving)) },
            text = { Text(stringResource(Res.string.cloud_saving_hint)) },
            confirmButton = {},
        )
    } else if (!showUnpaired && state.userId == userId && state.failure != null && state.phase != CloudPhase.ERROR) {
        AlertDialog(
            onDismissRequest = repository::dismissFailure,
            title = { Text(stringResource(Res.string.cloud_error)) },
            text = { Text(stringResource(state.failure.message())) },
            confirmButton = { TextButton(repository::retry) { Text(stringResource(Res.string.cloud_retry)) } },
            dismissButton = { TextButton(repository::dismissFailure) { Text(stringResource(Res.string.cancel)) } },
        )
    }
}

internal fun CloudFailure?.message() =
    when (this) {
        CloudFailure.ACCESS -> Res.string.cloud_access_error
        CloudFailure.NETWORK -> Res.string.cloud_network_error
        CloudFailure.INVITE -> Res.string.cloud_invite_error
        CloudFailure.DATA -> Res.string.cloud_data_error
        else -> Res.string.cloud_error
    }
