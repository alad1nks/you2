package com.alad1nks.you2.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.alad1nks.you2.core.model.Language
import com.alad1nks.you2.core.model.PairedSpace
import com.alad1nks.you2.feature.designsystem.AppIcons
import com.alad1nks.you2.feature.designsystem.components.AppTextField
import com.alad1nks.you2.feature.designsystem.components.Avatar
import com.alad1nks.you2.feature.designsystem.theme.AppShapes
import com.alad1nks.you2.feature.designsystem.theme.AppSpacing
import com.alad1nks.you2.resources.generated.Res
import com.alad1nks.you2.resources.generated.appearance
import com.alad1nks.you2.resources.generated.cancel
import com.alad1nks.you2.resources.generated.connected
import com.alad1nks.you2.resources.generated.couple_label
import com.alad1nks.you2.resources.generated.dark_theme
import com.alad1nks.you2.resources.generated.demo_signout
import com.alad1nks.you2.resources.generated.edit_profile
import com.alad1nks.you2.resources.generated.english_language
import com.alad1nks.you2.resources.generated.kazakh_language
import com.alad1nks.you2.resources.generated.language
import com.alad1nks.you2.resources.generated.light_theme
import com.alad1nks.you2.resources.generated.make_it_yours
import com.alad1nks.you2.resources.generated.memories_made
import com.alad1nks.you2.resources.generated.profile_hint
import com.alad1nks.you2.resources.generated.profile_name
import com.alad1nks.you2.resources.generated.russian_language
import com.alad1nks.you2.resources.generated.save
import com.alad1nks.you2.resources.generated.shared_space_note
import com.alad1nks.you2.resources.generated.sign_out
import com.alad1nks.you2.resources.generated.system_language
import com.alad1nks.you2.resources.generated.us
import com.alad1nks.you2.resources.generated.us_couple_hint
import com.alad1nks.you2.resources.generated.us_subtitle
import com.alad1nks.you2.resources.generated.wishes_to_try
import com.alad1nks.you2.resources.generated.your_profile
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

internal enum class UsDialog { PROFILE, APPEARANCE, LANGUAGE }

internal fun LazyListScope.usContent(
    space: PairedSpace,
    darkTheme: Boolean,
    language: Language,
    onOpen: (UsDialog) -> Unit,
) {
    item {
        Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.xs)) {
            Text(stringResource(Res.string.us), style = MaterialTheme.typography.headlineLarge)
            Text(stringResource(Res.string.us_subtitle), color = MaterialTheme.colorScheme.secondary)
        }
    }
    item {
        Surface(shape = AppShapes.card, color = MaterialTheme.colorScheme.primaryContainer) {
            Column(
                Modifier.fillMaxWidth().heightIn(min = 184.dp).padding(AppSpacing.large),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterVertically),
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(AppSpacing.medium),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Avatar(
                        space.couple.you.name,
                        Modifier.size(56.dp),
                        MaterialTheme.colorScheme.background,
                        MaterialTheme.typography.headlineMedium,
                    )
                    Icon(
                        AppIcons.FavoriteBorder,
                        null,
                        Modifier.size(24.dp),
                        tint = MaterialTheme.colorScheme.primary,
                    )
                    Avatar(
                        space.couple.partner.name,
                        Modifier.size(56.dp),
                        MaterialTheme.colorScheme.surfaceContainerLowest,
                        MaterialTheme.typography.headlineMedium,
                    )
                }
                Text(
                    stringResource(Res.string.couple_label, space.couple.partner.name),
                    style = MaterialTheme.typography.headlineMedium,
                    textAlign = TextAlign.Center,
                )
                Text(
                    stringResource(Res.string.us_couple_hint),
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.secondary,
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(AppIcons.Link, null, Modifier.size(16.dp))
                    Text(stringResource(Res.string.connected), style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
    item {
        Row(horizontalArrangement = Arrangement.spacedBy(AppSpacing.cardInset)) {
            Statistic(
                space.active.size,
                stringResource(Res.string.wishes_to_try),
                Modifier.weight(1f).testTag("us-wishes"),
            )
            Statistic(
                space.memories.size,
                stringResource(Res.string.memories_made),
                Modifier.weight(1f).testTag("us-memories"),
            )
        }
    }
    item {
        Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.medium)) {
            Text(stringResource(Res.string.make_it_yours), style = MaterialTheme.typography.labelLarge)
            Surface(
                shape = AppShapes.card,
                border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant),
                color = MaterialTheme.colorScheme.surfaceContainerLowest,
            ) {
                Column {
                    SettingsRow(
                        Res.string.your_profile,
                        stringResource(Res.string.edit_profile),
                    ) { onOpen(UsDialog.PROFILE) }
                    SettingsRow(
                        Res.string.appearance,
                        stringResource(if (darkTheme) Res.string.dark_theme else Res.string.light_theme),
                    ) {
                        onOpen(UsDialog.APPEARANCE)
                    }
                    SettingsRow(Res.string.language, stringResource(language.label)) { onOpen(UsDialog.LANGUAGE) }
                }
            }
        }
    }
    item {
        Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.small)) {
            OutlinedButton({}, Modifier.fillMaxWidth().heightIn(min = 52.dp), enabled = false) {
                Icon(AppIcons.Logout, null, Modifier.size(20.dp))
                Text(stringResource(Res.string.sign_out), Modifier.padding(start = AppSpacing.small))
            }
            Text(
                stringResource(Res.string.demo_signout),
                Modifier.fillMaxWidth(),
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.secondary,
            )
        }
    }
    item {
        Text(
            stringResource(Res.string.shared_space_note),
            Modifier.fillMaxWidth(),
            style = MaterialTheme.typography.bodySmall,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.secondary,
        )
    }
}

@Composable
private fun Statistic(count: Int, label: String, modifier: Modifier) {
    Surface(
        modifier,
        shape = AppShapes.field,
        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant),
        color = MaterialTheme.colorScheme.surfaceContainerLowest,
    ) {
        Column(
            Modifier.heightIn(min = 72.dp).padding(AppSpacing.medium),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(AppSpacing.xs),
        ) {
            Text(
                count.toString(),
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary,
            )
            Text(
                label,
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.secondary,
            )
        }
    }
}

@Composable
private fun SettingsRow(label: StringResource, value: String, onClick: () -> Unit) {
    Surface(onClick, color = MaterialTheme.colorScheme.surfaceContainerLowest) {
        Row(
            Modifier.fillMaxWidth().heightIn(
                min = 52.dp,
            ).padding(horizontal = AppSpacing.large, vertical = AppSpacing.small),
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.medium),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(stringResource(label), Modifier.weight(1f), style = MaterialTheme.typography.labelLarge)
            Text(
                value,
                Modifier.weight(0.6f),
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.End,
                color = MaterialTheme.colorScheme.secondary,
            )
            Icon(
                AppIcons.ChevronRight,
                null,
                Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.secondary,
            )
        }
    }
}

@Composable
internal fun UsSettingsDialog(
    dialog: UsDialog,
    name: String,
    darkTheme: Boolean,
    language: Language,
    onDismiss: () -> Unit,
    onRenameUser: (String) -> Unit,
    onSetDarkTheme: (Boolean) -> Unit,
    onSetLanguage: (Language) -> Unit,
) {
    var draft by rememberSaveable { mutableStateOf(name) }
    val validName = draft.isNotBlank() && draft.length <= 80
    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.imePadding(),
        title = {
            Text(
                stringResource(
                    when (dialog) {
                        UsDialog.PROFILE -> Res.string.your_profile
                        UsDialog.APPEARANCE -> Res.string.appearance
                        UsDialog.LANGUAGE -> Res.string.language
                    },
                ),
            )
        },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState()).selectableGroup()) {
                when (dialog) {
                    UsDialog.PROFILE -> {
                        AppTextField(
                            draft,
                            { draft = it },
                            stringResource(Res.string.profile_name),
                            Modifier.testTag("profile-name"),
                            isError = !validName,
                            helperText = stringResource(Res.string.profile_hint),
                        )
                    }

                    UsDialog.APPEARANCE -> {
                        listOf(false, true).forEach { dark ->
                            PreferenceOption(
                                stringResource(if (dark) Res.string.dark_theme else Res.string.light_theme),
                                dark == darkTheme,
                            ) {
                                onSetDarkTheme(dark)
                                onDismiss()
                            }
                        }
                    }

                    UsDialog.LANGUAGE -> {
                        Language.entries.forEach { option ->
                            PreferenceOption(stringResource(option.label), option == language) {
                                onSetLanguage(option)
                                onDismiss()
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            if (dialog == UsDialog.PROFILE) {
                TextButton({
                    onRenameUser(draft.trim())
                    onDismiss()
                }, enabled = validName) { Text(stringResource(Res.string.save)) }
            }
        },
        dismissButton = { TextButton(onDismiss) { Text(stringResource(Res.string.cancel)) } },
    )
}

@Composable
private fun PreferenceOption(label: String, selected: Boolean, onClick: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().heightIn(min = 48.dp).selectable(
            selected,
            role = Role.RadioButton,
            onClick = onClick,
        ).padding(vertical = AppSpacing.small),
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.medium),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RadioButton(selected, onClick = null)
        Text(label)
    }
}

private val Language.label: StringResource
    get() =
        when (this) {
            Language.SYSTEM -> Res.string.system_language
            Language.ENGLISH -> Res.string.english_language
            Language.RUSSIAN -> Res.string.russian_language
            Language.KAZAKH -> Res.string.kazakh_language
        }
