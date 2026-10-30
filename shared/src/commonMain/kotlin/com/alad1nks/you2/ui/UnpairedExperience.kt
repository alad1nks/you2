package com.alad1nks.you2.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.alad1nks.you2.core.data.repository.CloudFailure
import com.alad1nks.you2.core.data.repository.CloudPhase
import com.alad1nks.you2.core.data.repository.CloudState
import com.alad1nks.you2.core.data.repository.CloudWishesRepository
import com.alad1nks.you2.core.model.Language
import com.alad1nks.you2.feature.designsystem.AppIcons
import com.alad1nks.you2.feature.designsystem.components.Avatar
import com.alad1nks.you2.feature.designsystem.components.PrimaryButton
import com.alad1nks.you2.feature.designsystem.theme.AppDimensions
import com.alad1nks.you2.feature.designsystem.theme.AppShapes
import com.alad1nks.you2.feature.designsystem.theme.AppSpacing
import com.alad1nks.you2.resources.generated.Res
import com.alad1nks.you2.resources.generated.appearance
import com.alad1nks.you2.resources.generated.brand
import com.alad1nks.you2.resources.generated.cancel
import com.alad1nks.you2.resources.generated.cloud_create_invite
import com.alad1nks.you2.resources.generated.cloud_retry
import com.alad1nks.you2.resources.generated.cloud_saving
import com.alad1nks.you2.resources.generated.dark_theme
import com.alad1nks.you2.resources.generated.edit_profile
import com.alad1nks.you2.resources.generated.language
import com.alad1nks.you2.resources.generated.light_theme
import com.alad1nks.you2.resources.generated.make_it_yours
import com.alad1nks.you2.resources.generated.memories
import com.alad1nks.you2.resources.generated.pair_beginning
import com.alad1nks.you2.resources.generated.pair_body
import com.alad1nks.you2.resources.generated.pair_close
import com.alad1nks.you2.resources.generated.pair_code
import com.alad1nks.you2.resources.generated.pair_connected
import com.alad1nks.you2.resources.generated.pair_connected_body
import com.alad1nks.you2.resources.generated.pair_copied
import com.alad1nks.you2.resources.generated.pair_copy
import com.alad1nks.you2.resources.generated.pair_copy_again
import com.alad1nks.you2.resources.generated.pair_existing_invitation
import com.alad1nks.you2.resources.generated.pair_expiry
import com.alad1nks.you2.resources.generated.pair_have_code
import com.alad1nks.you2.resources.generated.pair_invalid
import com.alad1nks.you2.resources.generated.pair_invite
import com.alad1nks.you2.resources.generated.pair_invite_body
import com.alad1nks.you2.resources.generated.pair_join
import com.alad1nks.you2.resources.generated.pair_join_body
import com.alad1nks.you2.resources.generated.pair_join_title
import com.alad1nks.you2.resources.generated.pair_memories_body
import com.alad1nks.you2.resources.generated.pair_memories_hint
import com.alad1nks.you2.resources.generated.pair_placeholder
import com.alad1nks.you2.resources.generated.pair_space
import com.alad1nks.you2.resources.generated.pair_title
import com.alad1nks.you2.resources.generated.pair_unpaired
import com.alad1nks.you2.resources.generated.pair_view_wishes
import com.alad1nks.you2.resources.generated.pair_waiting
import com.alad1nks.you2.resources.generated.pair_wishes_body
import com.alad1nks.you2.resources.generated.pair_wishes_hint
import com.alad1nks.you2.resources.generated.pair_you
import com.alad1nks.you2.resources.generated.pair_your_code
import com.alad1nks.you2.resources.generated.sign_out
import com.alad1nks.you2.resources.generated.wishes
import com.alad1nks.you2.resources.generated.your_profile
import org.jetbrains.compose.resources.stringResource

private enum class PairDialog { INVITE, JOIN }

@Composable
internal fun UnpairedExperience(
    state: CloudState,
    repository: CloudWishesRepository,
    darkTheme: Boolean,
    language: Language,
    onSetDarkTheme: (Boolean) -> Unit,
    onSetLanguage: (Language) -> Unit,
    onSignOut: () -> Unit,
    onConnected: () -> Unit,
    tabState: MutableState<MainTab>,
) {
    var tab by tabState
    var dialog by rememberSaveable(state.userId) { mutableStateOf<PairDialog?>(null) }
    var settings by rememberSaveable(state.userId) { mutableStateOf<UsDialog?>(null) }
    val name = state.displayName.ifBlank { stringResource(Res.string.pair_you) }
    val pending = state.busy || state.phase == CloudPhase.LOADING
    val invite = {
        dialog = PairDialog.INVITE
        if (state.inviteCode == null) repository.createInvitation(name)
    }
    val join = { if (state.inviteCode == null && !pending) dialog = PairDialog.JOIN }
    Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Box(contentAlignment = Alignment.TopCenter) {
            Box(
                Modifier.align(Alignment.BottomCenter).fillMaxWidth()
                    .windowInsetsBottomHeight(WindowInsets.navigationBars)
                    .background(MaterialTheme.colorScheme.surfaceContainer),
            )
            Column(Modifier.widthIn(max = 600.dp).fillMaxSize().safeDrawingPadding()) {
                Row(
                    Modifier.fillMaxWidth().heightIn(min = AppDimensions.headerHeight)
                        .padding(horizontal = AppSpacing.page),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(7.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(AppIcons.FavoriteBorder, null, Modifier.size(24.dp), MaterialTheme.colorScheme.primary)
                        Text(
                            stringResource(Res.string.brand),
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                    Surface(onClick = { tab = MainTab.US }, shape = CircleShape) {
                        Avatar(name, Modifier.size(40.dp), textStyle = MaterialTheme.typography.labelLarge)
                    }
                }
                BoxWithConstraints(Modifier.weight(1f).fillMaxWidth()) {
                    val viewport = maxHeight
                    key(tab) {
                        Column(
                            Modifier.fillMaxWidth().verticalScroll(rememberScrollState())
                                .heightIn(
                                    min = viewport,
                                ).padding(horizontal = AppSpacing.page, vertical = AppSpacing.large),
                            verticalArrangement = Arrangement.spacedBy(AppSpacing.cardInset),
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.xs)) {
                                Text(
                                    stringResource(
                                        when (tab) {
                                            MainTab.US -> Res.string.pair_space
                                            MainTab.WISHES -> Res.string.wishes
                                            MainTab.MEMORIES -> Res.string.memories
                                        },
                                    ),
                                    style = MaterialTheme.typography.headlineLarge,
                                )
                                Text(
                                    stringResource(
                                        when (tab) {
                                            MainTab.US -> Res.string.pair_beginning
                                            MainTab.WISHES -> Res.string.pair_wishes_hint
                                            MainTab.MEMORIES -> Res.string.pair_memories_hint
                                        },
                                    ),
                                    color = MaterialTheme.colorScheme.secondary,
                                )
                            }
                            if (tab == MainTab.US) {
                                Surface(
                                    shape = AppShapes.card,
                                    color = MaterialTheme.colorScheme.surfaceContainerLowest,
                                    border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant),
                                ) {
                                    Row(
                                        Modifier.fillMaxWidth().heightIn(min = 88.dp).padding(AppSpacing.large),
                                        horizontalArrangement = Arrangement.spacedBy(AppSpacing.large),
                                        verticalAlignment = Alignment.CenterVertically,
                                    ) {
                                        Avatar(
                                            name,
                                            Modifier.size(56.dp),
                                            textStyle = MaterialTheme.typography.headlineMedium,
                                        )
                                        Column(
                                            Modifier.weight(1f),
                                            verticalArrangement = Arrangement.spacedBy(AppSpacing.xs),
                                        ) {
                                            Text(name, style = MaterialTheme.typography.headlineMedium)
                                            Text(
                                                stringResource(Res.string.pair_unpaired),
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.secondary,
                                            )
                                        }
                                    }
                                }
                                ConnectionCard(tab, pending, state.inviteCode != null, invite, join)
                                Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.medium)) {
                                    Text(
                                        stringResource(Res.string.make_it_yours),
                                        style = MaterialTheme.typography.labelLarge,
                                    )
                                    Surface(
                                        shape = AppShapes.card,
                                        color = MaterialTheme.colorScheme.surfaceContainerLowest,
                                        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant),
                                    ) {
                                        Column {
                                            SettingsRow(
                                                Res.string.your_profile,
                                                stringResource(Res.string.edit_profile),
                                            ) { settings = UsDialog.PROFILE }
                                            SettingsRow(
                                                Res.string.appearance,
                                                stringResource(
                                                    if (darkTheme) Res.string.dark_theme else Res.string.light_theme,
                                                ),
                                            ) {
                                                settings = UsDialog.APPEARANCE
                                            }
                                            SettingsRow(
                                                Res.string.language,
                                                stringResource(language.label),
                                            ) { settings = UsDialog.LANGUAGE }
                                        }
                                    }
                                }
                                OutlinedButton(onSignOut, Modifier.fillMaxWidth().heightIn(min = 52.dp)) {
                                    Icon(AppIcons.Logout, null, Modifier.size(20.dp))
                                    Text(stringResource(Res.string.sign_out), Modifier.padding(start = 10.dp))
                                }
                            } else {
                                Column(
                                    Modifier.fillMaxWidth().weight(1f).padding(top = 10.dp, bottom = 16.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(24.dp, Alignment.CenterVertically),
                                ) {
                                    Surface(
                                        Modifier.size(96.dp),
                                        shape = CircleShape,
                                        color = MaterialTheme.colorScheme.primaryContainer,
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                AppIcons.FavoriteBorder,
                                                null,
                                                Modifier.size(48.dp),
                                                MaterialTheme.colorScheme.primary,
                                            )
                                        }
                                    }
                                    ConnectionCard(tab, pending, state.inviteCode != null, invite, join)
                                }
                            }
                        }
                    }
                }
                GuestNavigation(tab) { tab = it }
            }
        }
    }
    settings?.let {
        UsSettingsDialog(
            it,
            name,
            darkTheme,
            language,
            { settings = null },
            repository::renameUser,
            onSetDarkTheme,
            onSetLanguage,
            syncedProfile = true,
        )
    }
    val connected = state.phase == CloudPhase.READY
    if (connected || dialog != null) {
        PairingDialog(state, dialog == PairDialog.JOIN, name, repository, onConnected) {
            if (!pending) {
                dialog = null
                repository.dismissFailure()
            }
        }
    } else if (state.failure != null) {
        AlertDialog(
            onDismissRequest = repository::dismissFailure,
            text = { Text(stringResource(state.failure.message())) },
            confirmButton = { TextButton(repository::retry) { Text(stringResource(Res.string.cloud_retry)) } },
            dismissButton = { TextButton(repository::dismissFailure) { Text(stringResource(Res.string.cancel)) } },
        )
    }
}

@Composable
private fun ConnectionCard(
    tab: MainTab,
    pending: Boolean,
    hasInvitation: Boolean,
    invite: () -> Unit,
    join: () -> Unit,
) {
    Surface(
        Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.primaryContainer,
    ) {
        Column(
            Modifier.heightIn(min = if (tab == MainTab.US) 238.dp else 232.dp).padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(stringResource(Res.string.pair_title), style = MaterialTheme.typography.headlineMedium)
            Text(
                stringResource(
                    when (tab) {
                        MainTab.US -> Res.string.pair_body
                        MainTab.WISHES -> Res.string.pair_wishes_body
                        MainTab.MEMORIES -> Res.string.pair_memories_body
                    },
                ),
                color = MaterialTheme.colorScheme.secondary,
            )
            PrimaryButton(stringResource(Res.string.pair_invite), invite, enabled = !pending)
            OutlinedButton(
                join,
                Modifier.fillMaxWidth().heightIn(min = 40.dp),
                enabled = !pending && !hasInvitation,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.primary),
            ) {
                Text(stringResource(Res.string.pair_have_code))
            }
            if (hasInvitation) {
                Text(
                    stringResource(Res.string.pair_existing_invitation),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.secondary,
                )
            }
        }
    }
}

@Suppress("DEPRECATION")
@Composable
private fun PairingDialog(
    state: CloudState,
    joining: Boolean,
    name: String,
    repository: CloudWishesRepository,
    onConnected: () -> Unit,
    onDismiss: () -> Unit,
) {
    var code by rememberSaveable { mutableStateOf("") }
    var copied by rememberSaveable(state.inviteCode) { mutableStateOf(false) }
    // Reopening keeps the code stable, but still allows replacing an expired invitation.
    var offerRenewal by rememberSaveable { mutableStateOf(!joining && state.inviteCode != null) }
    val clipboard = LocalClipboardManager.current
    val connected = state.phase == CloudPhase.READY
    val pending = state.busy || state.phase == CloudPhase.LOADING
    val invalid = joining && state.failure in listOf(CloudFailure.INVITE, CloudFailure.ACCESS)
    Dialog(if (connected) onConnected else onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(
            Modifier.padding(24.dp).widthIn(max = 480.dp).fillMaxWidth().imePadding().testTag("pair-dialog"),
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.background,
        ) {
            Column(
                Modifier.verticalScroll(rememberScrollState()).padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Text(
                    stringResource(
                        when {
                            connected -> Res.string.pair_connected
                            joining -> Res.string.pair_join_title
                            else -> Res.string.pair_invite
                        },
                    ),
                    style = MaterialTheme.typography.headlineMedium,
                )
                Text(
                    stringResource(
                        when {
                            connected -> Res.string.pair_connected_body
                            joining -> Res.string.pair_join_body
                            else -> Res.string.pair_invite_body
                        },
                    ),
                    color = MaterialTheme.colorScheme.secondary,
                )
                if (!connected) {
                    Text(
                        stringResource(if (joining) Res.string.pair_code else Res.string.pair_your_code),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    if (joining) {
                        OutlinedTextField(
                            code,
                            {
                                code = it.trim().take(32)
                                if (state.failure != null) repository.dismissFailure()
                            },
                            Modifier.fillMaxWidth().testTag("pair-code"),
                            enabled = !pending,
                            singleLine = true,
                            placeholder = {
                                Text(
                                    stringResource(Res.string.pair_placeholder),
                                    color = MaterialTheme.colorScheme.secondary,
                                )
                            },
                            shape = AppShapes.field,
                            isError = invalid,
                            colors =
                                OutlinedTextFieldDefaults.colors(
                                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
                                    focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
                                    errorContainerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
                                ),
                        )
                    } else if (state.inviteCode != null) {
                        Surface(
                            Modifier.fillMaxWidth(),
                            shape = AppShapes.field,
                            color = MaterialTheme.colorScheme.primaryContainer,
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                        ) {
                            SelectionContainer {
                                Box(
                                    Modifier.heightIn(min = 64.dp).padding(16.dp),
                                    contentAlignment = Alignment.CenterStart,
                                ) {
                                    Text(requireNotNull(state.inviteCode), color = MaterialTheme.colorScheme.primary)
                                }
                            }
                        }
                        Text(stringResource(Res.string.pair_expiry), color = MaterialTheme.colorScheme.secondary)
                        if (copied) {
                            Text(
                                stringResource(Res.string.pair_copied),
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.primary,
                            )
                            Text(stringResource(Res.string.pair_waiting), color = MaterialTheme.colorScheme.secondary)
                        }
                    }
                    if (pending) {
                        LinearProgressIndicator(Modifier.fillMaxWidth())
                        Text(stringResource(Res.string.cloud_saving), color = MaterialTheme.colorScheme.secondary)
                    }
                    if (state.failure != null) {
                        Text(
                            stringResource(if (invalid) Res.string.pair_invalid else state.failure.message()),
                            color = MaterialTheme.colorScheme.error,
                        )
                        if (!invalid) TextButton(repository::retry) { Text(stringResource(Res.string.cloud_retry)) }
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    if (!connected) {
                        OutlinedButton(
                            onDismiss,
                            Modifier.weight(1f).heightIn(min = 48.dp),
                            enabled = !pending,
                            colors =
                                ButtonDefaults.outlinedButtonColors(
                                    contentColor = MaterialTheme.colorScheme.primary,
                                ),
                        ) {
                            Text(stringResource(if (joining) Res.string.cancel else Res.string.pair_close))
                        }
                    }
                    Button(
                        {
                            when {
                                connected -> {
                                    onConnected()
                                }

                                joining -> {
                                    if (state.inviteCode == null) repository.joinInvitation(name, code.trim())
                                }

                                else -> {
                                    state.inviteCode?.let {
                                        clipboard.setText(AnnotatedString(it))
                                        copied = true
                                    }
                                }
                            }
                        },
                        Modifier.weight(1f).heightIn(min = 48.dp),
                        enabled =
                            connected || (
                                !pending &&
                                    if (joining) {
                                        code.isNotBlank() && state.inviteCode == null
                                    } else {
                                        state.inviteCode != null
                                    }
                            ),
                        colors =
                            ButtonDefaults.buttonColors(
                                disabledContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.38f),
                                disabledContentColor = MaterialTheme.colorScheme.onPrimary,
                            ),
                    ) {
                        Text(
                            stringResource(
                                when {
                                    connected -> Res.string.pair_view_wishes
                                    joining -> Res.string.pair_join
                                    copied -> Res.string.pair_copy_again
                                    else -> Res.string.pair_copy
                                },
                            ),
                        )
                    }
                }
                if (offerRenewal && !connected && !joining) {
                    TextButton({
                        copied = false
                        offerRenewal = false
                        repository.createInvitation(name)
                    }, Modifier.fillMaxWidth(), enabled = !pending) {
                        Text(stringResource(Res.string.cloud_create_invite))
                    }
                }
            }
        }
    }
}
