package com.alad1nks.you2.auth

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
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
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.alad1nks.you2.core.model.Language
import com.alad1nks.you2.feature.designsystem.AppIcons
import com.alad1nks.you2.feature.designsystem.components.PrimaryButton
import com.alad1nks.you2.feature.designsystem.theme.AppDimensions
import com.alad1nks.you2.feature.designsystem.theme.AppShapes
import com.alad1nks.you2.feature.designsystem.theme.AppSpacing
import com.alad1nks.you2.feature.designsystem.theme.onSurfaceLight
import com.alad1nks.you2.feature.designsystem.theme.outlineVariantLight
import com.alad1nks.you2.feature.designsystem.theme.surfaceContainerLowestLight
import com.alad1nks.you2.resources.generated.Res
import com.alad1nks.you2.resources.generated.auth_back
import com.alad1nks.you2.resources.generated.auth_confirm
import com.alad1nks.you2.resources.generated.auth_create_body
import com.alad1nks.you2.resources.generated.auth_create_password
import com.alad1nks.you2.resources.generated.auth_create_title
import com.alad1nks.you2.resources.generated.auth_email
import com.alad1nks.you2.resources.generated.auth_email_placeholder
import com.alad1nks.you2.resources.generated.auth_email_separator
import com.alad1nks.you2.resources.generated.auth_enter_password
import com.alad1nks.you2.resources.generated.auth_error_credentials
import com.alad1nks.you2.resources.generated.auth_error_email
import com.alad1nks.you2.resources.generated.auth_error_google
import com.alad1nks.you2.resources.generated.auth_error_in_use
import com.alad1nks.you2.resources.generated.auth_error_mismatch
import com.alad1nks.you2.resources.generated.auth_error_network
import com.alad1nks.you2.resources.generated.auth_error_password
import com.alad1nks.you2.resources.generated.auth_error_rate
import com.alad1nks.you2.resources.generated.auth_error_unknown
import com.alad1nks.you2.resources.generated.auth_error_weak
import com.alad1nks.you2.resources.generated.auth_existing_account
import com.alad1nks.you2.resources.generated.auth_forgot
import com.alad1nks.you2.resources.generated.auth_google
import com.alad1nks.you2.resources.generated.auth_hide
import com.alad1nks.you2.resources.generated.auth_new_here
import com.alad1nks.you2.resources.generated.auth_password
import com.alad1nks.you2.resources.generated.auth_register
import com.alad1nks.you2.resources.generated.auth_reset
import com.alad1nks.you2.resources.generated.auth_reset_body
import com.alad1nks.you2.resources.generated.auth_reset_sent
import com.alad1nks.you2.resources.generated.auth_send_reset
import com.alad1nks.you2.resources.generated.auth_show
import com.alad1nks.you2.resources.generated.auth_welcome
import com.alad1nks.you2.resources.generated.auth_welcome_body
import com.alad1nks.you2.resources.generated.back
import com.alad1nks.you2.resources.generated.google
import com.alad1nks.you2.resources.generated.guest_log_in
import com.alad1nks.you2.ui.GuestLanguageSelector
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalComposeUiApi::class)
@Composable
internal fun AuthForm(
    state: AuthState,
    viewModel: AuthViewModel,
    language: Language = Language.SYSTEM,
    onSetLanguage: (Language) -> Unit = {},
    onBackToGuest: (() -> Unit)? = null,
    googleSignIn: GoogleSignInLauncher? = rememberGoogleSignInLauncher(),
) {
    var email by rememberSaveable { mutableStateOf("") }
    // Passwords deliberately never enter saved instance state, preferences, or logs.
    var password by remember(state.mode) { mutableStateOf("") }
    var confirmation by remember(state.mode) { mutableStateOf("") }
    var showPassword by remember(state.mode) { mutableStateOf(false) }
    val focus = LocalFocusManager.current
    val title =
        when (state.mode) {
            AuthMode.SIGN_IN -> Res.string.auth_welcome
            AuthMode.REGISTER -> Res.string.auth_create_title
            AuthMode.RESET -> Res.string.auth_reset
        }
    val description =
        when (state.mode) {
            AuthMode.SIGN_IN -> Res.string.auth_welcome_body
            AuthMode.REGISTER -> Res.string.auth_create_body
            AuthMode.RESET -> Res.string.auth_reset_body
        }
    val action =
        when (state.mode) {
            AuthMode.SIGN_IN -> Res.string.guest_log_in
            AuthMode.REGISTER -> Res.string.auth_register
            AuthMode.RESET -> Res.string.auth_send_reset
        }
    val ready =
        !state.busy && email.isNotBlank() &&
            (state.mode == AuthMode.RESET || password.isNotEmpty()) &&
            (state.mode != AuthMode.REGISTER || confirmation.isNotEmpty())
    val submit: () -> Unit = {
        if (ready) {
            focus.clearFocus()
            viewModel.submit(email, password, confirmation)
        }
    }
    val goBack: () -> Unit = {
        if (!state.busy) {
            focus.clearFocus()
            if (state.mode == AuthMode.RESET || onBackToGuest == null) {
                viewModel.changeMode(AuthMode.SIGN_IN)
            } else {
                onBackToGuest()
            }
        }
    }
    BackHandler(onBackToGuest != null || state.mode != AuthMode.SIGN_IN) { goBack() }
    AuthPage(
        header = {
            if (onBackToGuest != null || state.mode != AuthMode.SIGN_IN) {
                TextButton(onClick = goBack, enabled = !state.busy) {
                    Icon(AppIcons.ArrowBack, null, Modifier.size(20.dp))
                    Spacer(Modifier.width(AppSpacing.small))
                    Text(stringResource(Res.string.back))
                }
            } else {
                Spacer(Modifier.width(1.dp))
            }
            GuestLanguageSelector(language, onSetLanguage)
        },
        introduction = {
            Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.small)) {
                Text(stringResource(title), style = MaterialTheme.typography.headlineLarge)
                Text(stringResource(description), color = MaterialTheme.colorScheme.secondary)
            }
        },
    ) {
        if (state.mode != AuthMode.RESET && googleSignIn != null) {
            Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.large)) {
                OutlinedButton(
                    onClick = {
                        focus.clearFocus()
                        viewModel.signInWithGoogle(googleSignIn)
                    },
                    enabled = !state.busy,
                    modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp).testTag("auth-google"),
                    shape = AppShapes.pill,
                    border = BorderStroke(1.dp, outlineVariantLight),
                    colors =
                        ButtonDefaults.outlinedButtonColors(
                            containerColor = surfaceContainerLowestLight,
                            contentColor = onSurfaceLight,
                            disabledContainerColor = surfaceContainerLowestLight,
                            disabledContentColor = onSurfaceLight.copy(alpha = 0.38f),
                        ),
                ) {
                    Image(painterResource(Res.drawable.google), null, Modifier.size(20.dp))
                    Spacer(Modifier.width(AppSpacing.medium))
                    Text(stringResource(Res.string.auth_google), textAlign = TextAlign.Center)
                }
                Row(
                    Modifier.fillMaxWidth().heightIn(min = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(AppSpacing.medium),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    HorizontalDivider(Modifier.weight(1f), color = MaterialTheme.colorScheme.outlineVariant)
                    Text(
                        stringResource(Res.string.auth_email_separator),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.secondary,
                    )
                    HorizontalDivider(Modifier.weight(1f), color = MaterialTheme.colorScheme.outlineVariant)
                }
            }
        }
        AuthField(
            value = email,
            onValueChange = {
                email = it
                viewModel.clearMessage()
            },
            label = stringResource(Res.string.auth_email),
            placeholder = stringResource(Res.string.auth_email_placeholder),
            enabled = !state.busy,
            isError = state.error == AuthError.EMAIL || state.error == AuthError.CREDENTIALS,
            keyboardType = KeyboardType.Email,
            imeAction = if (state.mode == AuthMode.RESET) ImeAction.Done else ImeAction.Next,
            onDone = submit,
            tag = "auth-email",
        )
        if (state.mode != AuthMode.RESET) {
            AuthField(
                value = password,
                onValueChange = {
                    password = it
                    viewModel.clearMessage()
                },
                label = stringResource(Res.string.auth_password),
                placeholder =
                    stringResource(
                        if (state.mode == AuthMode.REGISTER) {
                            Res.string.auth_create_password
                        } else {
                            Res.string.auth_enter_password
                        },
                    ),
                enabled = !state.busy,
                isError = state.error in listOf(AuthError.PASSWORD, AuthError.WEAK_PASSWORD, AuthError.CREDENTIALS),
                keyboardType = KeyboardType.Password,
                imeAction = if (state.mode == AuthMode.REGISTER) ImeAction.Next else ImeAction.Done,
                onDone = submit,
                visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(
                        onClick = { showPassword = !showPassword },
                        enabled = !state.busy,
                        modifier = Modifier.size(44.dp).testTag("auth-password-visibility"),
                    ) {
                        Icon(
                            if (showPassword) AppIcons.VisibilityOff else AppIcons.Visibility,
                            contentDescription =
                                stringResource(if (showPassword) Res.string.auth_hide else Res.string.auth_show),
                            modifier = Modifier.size(AppDimensions.icon),
                            tint = MaterialTheme.colorScheme.secondary,
                        )
                    }
                },
                tag = "auth-password",
            )
        }
        if (state.mode == AuthMode.REGISTER) {
            AuthField(
                value = confirmation,
                onValueChange = {
                    confirmation = it
                    viewModel.clearMessage()
                },
                label = stringResource(Res.string.auth_confirm),
                placeholder = stringResource(Res.string.auth_confirm),
                enabled = !state.busy,
                isError = state.error == AuthError.PASSWORD_MISMATCH,
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done,
                onDone = submit,
                visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                tag = "auth-confirmation",
            )
        }
        if (state.mode == AuthMode.SIGN_IN) {
            TextButton(
                onClick = { viewModel.changeMode(AuthMode.RESET) },
                enabled = !state.busy,
                modifier = Modifier.align(Alignment.End),
            ) { Text(stringResource(Res.string.auth_forgot)) }
        }
        state.error?.let {
            Text(
                stringResource(it.messageId()),
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite },
            )
        }
        if (state.resetSent) {
            Surface(color = MaterialTheme.colorScheme.primaryContainer, shape = AppShapes.field) {
                Text(
                    stringResource(Res.string.auth_reset_sent),
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.padding(AppSpacing.large).semantics { liveRegion = LiveRegionMode.Polite },
                )
            }
        }
        PrimaryButton(
            text = stringResource(action),
            onClick = submit,
            enabled = ready,
            modifier = Modifier.testTag("auth-submit"),
            leadingIcon =
                if (state.busy) {
                    (
                        {
                            CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp)
                        }
                    )
                } else {
                    null
                },
        )
        Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            if (state.mode != AuthMode.RESET) {
                Text(
                    stringResource(
                        if (state.mode == AuthMode.SIGN_IN) {
                            Res.string.auth_new_here
                        } else {
                            Res.string.auth_existing_account
                        },
                    ),
                    color = MaterialTheme.colorScheme.secondary,
                    textAlign = TextAlign.Center,
                )
            }
            TextButton(
                onClick = {
                    viewModel.changeMode(
                        if (state.mode == AuthMode.SIGN_IN) AuthMode.REGISTER else AuthMode.SIGN_IN,
                    )
                },
                enabled = !state.busy,
            ) {
                Text(
                    stringResource(
                        when (state.mode) {
                            AuthMode.SIGN_IN -> Res.string.auth_register
                            AuthMode.REGISTER -> Res.string.guest_log_in
                            AuthMode.RESET -> Res.string.auth_back
                        },
                    ),
                )
            }
        }
    }
}

@Composable
private fun AuthField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    enabled: Boolean,
    isError: Boolean,
    keyboardType: KeyboardType,
    imeAction: ImeAction,
    onDone: () -> Unit,
    tag: String,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    trailingIcon: @Composable (() -> Unit)? = null,
) {
    val focus = LocalFocusManager.current
    Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.small)) {
        Text(label, style = MaterialTheme.typography.labelLarge)
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth().testTag(tag).semantics { contentDescription = label },
            placeholder = { Text(placeholder, color = MaterialTheme.colorScheme.secondary) },
            enabled = enabled,
            isError = isError,
            singleLine = true,
            shape = AppShapes.field,
            colors =
                OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
                    disabledContainerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
                    errorContainerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
                ),
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType, imeAction = imeAction),
            keyboardActions = KeyboardActions(onNext = { focus.moveFocus(FocusDirection.Next) }, onDone = { onDone() }),
            visualTransformation = visualTransformation,
            trailingIcon = trailingIcon,
        )
    }
}

@Composable
private fun AuthPage(
    header: @Composable RowScope.() -> Unit,
    introduction: @Composable () -> Unit,
    content: @Composable ColumnScope.() -> Unit,
) {
    Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        BoxWithConstraints(Modifier.fillMaxSize().safeDrawingPadding().imePadding()) {
            val wide = maxWidth >= 1000.dp
            Column(Modifier.fillMaxSize()) {
                Row(
                    Modifier.fillMaxWidth().heightIn(min = AppDimensions.headerHeight)
                        .padding(horizontal = if (wide) 40.dp else AppSpacing.page),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    content = header,
                )
                if (wide) {
                    Row(
                        Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState())
                            .padding(horizontal = 64.dp, vertical = 72.dp),
                        horizontalArrangement = Arrangement.spacedBy(AppSpacing.section),
                    ) {
                        Box(Modifier.weight(1f)) { introduction() }
                        Column(
                            Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(AppSpacing.detailsGap),
                            content = content,
                        )
                    }
                } else {
                    Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.TopCenter) {
                        Column(
                            Modifier.widthIn(max = 588.dp).fillMaxWidth().verticalScroll(rememberScrollState())
                                .padding(AppSpacing.page),
                            verticalArrangement = Arrangement.spacedBy(28.dp),
                        ) {
                            introduction()
                            Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.detailsGap), content = content)
                        }
                    }
                }
            }
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
        AuthError.GOOGLE -> Res.string.auth_error_google
    }
