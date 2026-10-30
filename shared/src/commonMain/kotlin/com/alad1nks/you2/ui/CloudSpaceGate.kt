package com.alad1nks.you2.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
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
import com.alad1nks.you2.feature.designsystem.theme.AppSpacing
import com.alad1nks.you2.resources.generated.Res
import com.alad1nks.you2.resources.generated.cancel
import com.alad1nks.you2.resources.generated.cloud_access_error
import com.alad1nks.you2.resources.generated.cloud_cache
import com.alad1nks.you2.resources.generated.cloud_create_invite
import com.alad1nks.you2.resources.generated.cloud_data_error
import com.alad1nks.you2.resources.generated.cloud_error
import com.alad1nks.you2.resources.generated.cloud_invite_code
import com.alad1nks.you2.resources.generated.cloud_invite_error
import com.alad1nks.you2.resources.generated.cloud_invite_hint
import com.alad1nks.you2.resources.generated.cloud_join
import com.alad1nks.you2.resources.generated.cloud_loading
import com.alad1nks.you2.resources.generated.cloud_network_error
import com.alad1nks.you2.resources.generated.cloud_pair_hint
import com.alad1nks.you2.resources.generated.cloud_pair_title
import com.alad1nks.you2.resources.generated.cloud_retry
import com.alad1nks.you2.resources.generated.cloud_saving
import com.alad1nks.you2.resources.generated.cloud_saving_hint
import com.alad1nks.you2.resources.generated.profile_name
import com.alad1nks.you2.resources.generated.sign_out
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun CloudSpaceGate(
    repository: CloudWishesRepository?,
    userId: String?,
    onSignOut: () -> Unit,
    content: @Composable () -> Unit,
) {
    if (repository == null) {
        content()
        return
    }
    val state by repository.cloudState.collectAsState()
    if (state.userId == userId && state.phase == CloudPhase.READY) {
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
            } else if (state.phase == CloudPhase.UNPAIRED) {
                var name by rememberSaveable(userId) { mutableStateOf(state.displayName) }
                var code by rememberSaveable(userId) { mutableStateOf("") }
                Text(stringResource(Res.string.cloud_pair_title), style = MaterialTheme.typography.headlineMedium)
                Text(stringResource(Res.string.cloud_pair_hint))
                OutlinedTextField(
                    name,
                    { name = it.take(80) },
                    Modifier.fillMaxWidth(),
                    label = { Text(stringResource(Res.string.profile_name)) },
                    singleLine = true,
                    enabled = !state.busy,
                )
                Button({ repository.createInvitation(name) }, enabled = name.isNotBlank() && !state.busy) {
                    Text(stringResource(Res.string.cloud_create_invite))
                }
                if (state.inviteCode != null) {
                    SelectionContainer { Text(requireNotNull(state.inviteCode)) }
                    Text(stringResource(Res.string.cloud_invite_hint))
                } else {
                    OutlinedTextField(
                        code,
                        { code = it.take(32) },
                        Modifier.fillMaxWidth(),
                        label = { Text(stringResource(Res.string.cloud_invite_code)) },
                        singleLine = true,
                        enabled = !state.busy,
                    )
                    Button(
                        { repository.joinInvitation(name, code) },
                        enabled = name.isNotBlank() && code.isNotBlank() && !state.busy,
                    ) {
                        Text(stringResource(Res.string.cloud_join))
                    }
                }
            } else {
                Text(stringResource(state.failure.message()))
                Button(repository::retry) { Text(stringResource(Res.string.cloud_retry)) }
            }
            TextButton(onSignOut) { Text(stringResource(Res.string.sign_out)) }
        }
    }
    if (state.userId == userId && state.busy) {
        AlertDialog(
            onDismissRequest = {},
            title = { Text(stringResource(Res.string.cloud_saving)) },
            text = { Text(stringResource(Res.string.cloud_saving_hint)) },
            confirmButton = {},
        )
    } else if (state.userId == userId && state.failure != null && state.phase != CloudPhase.ERROR) {
        AlertDialog(
            onDismissRequest = repository::dismissFailure,
            title = { Text(stringResource(Res.string.cloud_error)) },
            text = { Text(stringResource(state.failure.message())) },
            confirmButton = { TextButton(repository::retry) { Text(stringResource(Res.string.cloud_retry)) } },
            dismissButton = { TextButton(repository::dismissFailure) { Text(stringResource(Res.string.cancel)) } },
        )
    }
}

private fun CloudFailure?.message() =
    when (this) {
        CloudFailure.ACCESS -> Res.string.cloud_access_error
        CloudFailure.NETWORK -> Res.string.cloud_network_error
        CloudFailure.INVITE -> Res.string.cloud_invite_error
        CloudFailure.DATA -> Res.string.cloud_data_error
        else -> Res.string.cloud_error
    }
