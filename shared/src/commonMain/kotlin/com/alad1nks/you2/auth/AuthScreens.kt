package com.alad1nks.you2.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.backhandler.BackHandler
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.alad1nks.you2.resources.generated.Res
import com.alad1nks.you2.resources.generated.auth_back
import com.alad1nks.you2.resources.generated.auth_confirm
import com.alad1nks.you2.resources.generated.auth_email
import com.alad1nks.you2.resources.generated.auth_error_credentials
import com.alad1nks.you2.resources.generated.auth_error_email
import com.alad1nks.you2.resources.generated.auth_error_in_use
import com.alad1nks.you2.resources.generated.auth_error_mismatch
import com.alad1nks.you2.resources.generated.auth_error_network
import com.alad1nks.you2.resources.generated.auth_error_password
import com.alad1nks.you2.resources.generated.auth_error_rate
import com.alad1nks.you2.resources.generated.auth_error_unknown
import com.alad1nks.you2.resources.generated.auth_error_weak
import com.alad1nks.you2.resources.generated.auth_forgot
import com.alad1nks.you2.resources.generated.auth_hide
import com.alad1nks.you2.resources.generated.auth_password
import com.alad1nks.you2.resources.generated.auth_register
import com.alad1nks.you2.resources.generated.auth_reset
import com.alad1nks.you2.resources.generated.auth_reset_sent
import com.alad1nks.you2.resources.generated.auth_send_reset
import com.alad1nks.you2.resources.generated.auth_show
import com.alad1nks.you2.resources.generated.auth_sign_in
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalComposeUiApi::class)
@Composable
internal fun AuthForm(state: AuthState, viewModel: AuthViewModel) {
    var email by rememberSaveable { mutableStateOf("") }
    // Passwords deliberately never enter saved instance state, preferences, or logs.
    var password by remember(state.mode) { mutableStateOf("") }
    var confirmation by remember(state.mode) { mutableStateOf("") }
    var showPassword by remember(state.mode) { mutableStateOf(false) }
    val title =
        when (state.mode) {
            AuthMode.SIGN_IN -> Res.string.auth_sign_in
            AuthMode.REGISTER -> Res.string.auth_register
            AuthMode.RESET -> Res.string.auth_reset
        }
    BackHandler(state.mode != AuthMode.SIGN_IN) {
        if (!state.busy) viewModel.changeMode(AuthMode.SIGN_IN)
    }
    AuthPage {
        Text("you2", style = MaterialTheme.typography.displaySmall, color = MaterialTheme.colorScheme.primary)
        Text(stringResource(title), style = MaterialTheme.typography.headlineMedium)
        OutlinedTextField(
            value = email,
            onValueChange = {
                email = it
                viewModel.clearMessage()
            },
            label = { Text(stringResource(Res.string.auth_email)) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            singleLine = true,
            enabled = !state.busy,
            modifier = Modifier.fillMaxWidth(),
        )
        if (state.mode != AuthMode.RESET) {
            OutlinedTextField(
                value = password,
                onValueChange = {
                    password = it
                    viewModel.clearMessage()
                },
                label = { Text(stringResource(Res.string.auth_password)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    TextButton(onClick = { showPassword = !showPassword }) {
                        Text(stringResource(if (showPassword) Res.string.auth_hide else Res.string.auth_show))
                    }
                },
                singleLine = true,
                enabled = !state.busy,
                modifier = Modifier.fillMaxWidth(),
            )
        }
        if (state.mode == AuthMode.REGISTER) {
            OutlinedTextField(
                value = confirmation,
                onValueChange = {
                    confirmation = it
                    viewModel.clearMessage()
                },
                label = { Text(stringResource(Res.string.auth_confirm)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                singleLine = true,
                enabled = !state.busy,
                modifier = Modifier.fillMaxWidth(),
            )
        }
        state.error?.let {
            Text(
                stringResource(it.messageId()),
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite },
            )
        }
        if (state.resetSent) {
            Text(
                stringResource(Res.string.auth_reset_sent),
                modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite },
            )
        }
        if (state.busy) CircularProgressIndicator()
        Button(
            onClick = { viewModel.submit(email, password, confirmation) },
            enabled = !state.busy,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(stringResource(if (state.mode == AuthMode.RESET) Res.string.auth_send_reset else title))
        }
        if (state.mode == AuthMode.SIGN_IN) {
            TextButton(onClick = { viewModel.changeMode(AuthMode.REGISTER) }, enabled = !state.busy) {
                Text(stringResource(Res.string.auth_register))
            }
            TextButton(onClick = { viewModel.changeMode(AuthMode.RESET) }, enabled = !state.busy) {
                Text(stringResource(Res.string.auth_forgot))
            }
        } else {
            TextButton(onClick = { viewModel.changeMode(AuthMode.SIGN_IN) }, enabled = !state.busy) {
                Text(stringResource(Res.string.auth_back))
            }
        }
    }
}

@Composable
private fun AuthPage(content: @Composable () -> Unit) {
    Box(Modifier.fillMaxSize().safeDrawingPadding().imePadding(), contentAlignment = Alignment.Center) {
        Column(
            modifier =
                Modifier.fillMaxWidth()
                    .verticalScroll(rememberScrollState()).padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            content()
        }
    }
}

private fun AuthError.messageId(): StringResource =
    when (this) {
        AuthError.EMAIL -> Res.string.auth_error_email
        AuthError.PASSWORD -> Res.string.auth_error_password
        AuthError.PASSWORD_MISMATCH -> Res.string.auth_error_mismatch
        AuthError.CREDENTIALS -> Res.string.auth_error_credentials
        AuthError.WEAK_PASSWORD -> Res.string.auth_error_weak
        AuthError.EMAIL_IN_USE -> Res.string.auth_error_in_use
        AuthError.NETWORK -> Res.string.auth_error_network
        AuthError.TOO_MANY_REQUESTS -> Res.string.auth_error_rate
        AuthError.UNKNOWN -> Res.string.auth_error_unknown
    }
