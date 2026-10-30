package com.alad1nks.you2.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.alad1nks.you2.auth.AuthForm
import com.alad1nks.you2.auth.AuthMode
import com.alad1nks.you2.auth.AuthState
import com.alad1nks.you2.auth.AuthViewModel
import com.alad1nks.you2.core.model.Language
import com.alad1nks.you2.feature.designsystem.AppIcons
import com.alad1nks.you2.feature.designsystem.components.PrimaryButton
import com.alad1nks.you2.feature.designsystem.theme.AppDimensions
import com.alad1nks.you2.feature.designsystem.theme.AppShapes
import com.alad1nks.you2.feature.designsystem.theme.AppSpacing
import com.alad1nks.you2.resources.generated.Res
import com.alad1nks.you2.resources.generated.auth_register
import com.alad1nks.you2.resources.generated.brand
import com.alad1nks.you2.resources.generated.english_language
import com.alad1nks.you2.resources.generated.guest_account_each
import com.alad1nks.you2.resources.generated.guest_invite_hint
import com.alad1nks.you2.resources.generated.guest_log_in
import com.alad1nks.you2.resources.generated.guest_memories
import com.alad1nks.you2.resources.generated.guest_memories_body
import com.alad1nks.you2.resources.generated.guest_memories_hint
import com.alad1nks.you2.resources.generated.guest_memories_title
import com.alad1nks.you2.resources.generated.guest_memory_description
import com.alad1nks.you2.resources.generated.guest_memory_label
import com.alad1nks.you2.resources.generated.guest_memory_title
import com.alad1nks.you2.resources.generated.guest_shared_list
import com.alad1nks.you2.resources.generated.guest_shared_memories
import com.alad1nks.you2.resources.generated.guest_us
import com.alad1nks.you2.resources.generated.guest_us_body
import com.alad1nks.you2.resources.generated.guest_us_title
import com.alad1nks.you2.resources.generated.guest_wish_description
import com.alad1nks.you2.resources.generated.guest_wish_label
import com.alad1nks.you2.resources.generated.guest_wish_title
import com.alad1nks.you2.resources.generated.guest_wishes
import com.alad1nks.you2.resources.generated.guest_wishes_body
import com.alad1nks.you2.resources.generated.guest_wishes_hint
import com.alad1nks.you2.resources.generated.guest_wishes_title
import com.alad1nks.you2.resources.generated.kazakh_language
import com.alad1nks.you2.resources.generated.memories
import com.alad1nks.you2.resources.generated.russian_language
import com.alad1nks.you2.resources.generated.system_language
import com.alad1nks.you2.resources.generated.us
import com.alad1nks.you2.resources.generated.wishes
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.imageResource
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun GuestExperience(
    auth: AuthState,
    authViewModel: AuthViewModel,
    language: Language,
    onSetLanguage: (Language) -> Unit,
) {
    var tab by rememberSaveable { mutableStateOf(MainTab.WISHES) }
    var authenticating by rememberSaveable { mutableStateOf(false) }
    if (authenticating) {
        AuthForm(auth, authViewModel, language, onSetLanguage, onBackToGuest = {
            authViewModel.changeMode(AuthMode.SIGN_IN)
            authenticating = false
        })
    } else {
        GuestScreen(
            tab,
            { tab = it },
            language,
            onSetLanguage,
            onAuthenticate = {
                authViewModel.changeMode(it)
                authenticating = true
            },
        )
    }
}

@Composable
internal fun GuestScreen(
    tab: MainTab,
    onSelectTab: (MainTab) -> Unit,
    language: Language,
    onSetLanguage: (Language) -> Unit,
    onAuthenticate: (AuthMode) -> Unit,
) {
    Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Box(contentAlignment = Alignment.TopCenter) {
            Box(
                Modifier.align(Alignment.BottomCenter).fillMaxWidth()
                    .windowInsetsBottomHeight(WindowInsets.navigationBars)
                    .background(MaterialTheme.colorScheme.surfaceContainer),
            )
            Column(Modifier.widthIn(max = 600.dp).fillMaxSize().safeDrawingPadding()) {
                GuestHeader(language, onSetLanguage)
                BoxWithConstraints(Modifier.weight(1f).fillMaxWidth()) {
                    // At the design size this is 497dp of content + 216dp of actions.
                    // On short screens both sections scroll together, leaving the tabs accessible.
                    val viewportHeight = maxHeight
                    key(tab) {
                        Column(
                            Modifier.fillMaxWidth().verticalScroll(rememberScrollState())
                                .heightIn(min = viewportHeight).testTag("guest-content"),
                        ) {
                            GuestPreview(
                                tab,
                                Modifier.fillMaxWidth().heightIn(min = (viewportHeight - 216.dp).coerceAtLeast(0.dp)),
                            )
                            GuestActions(tab, onAuthenticate)
                        }
                    }
                }
                GuestNavigation(tab, onSelectTab)
            }
        }
    }
}

@Composable
private fun GuestHeader(language: Language, onSetLanguage: (Language) -> Unit) {
    Row(
        Modifier.fillMaxWidth().heightIn(min = AppDimensions.headerHeight).padding(horizontal = AppSpacing.page),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(7.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(AppIcons.FavoriteBorder, null, Modifier.size(AppDimensions.icon), MaterialTheme.colorScheme.primary)
            Text(
                stringResource(Res.string.brand),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary,
            )
        }
        GuestLanguageSelector(language, onSetLanguage)
    }
}

@Composable
internal fun GuestLanguageSelector(language: Language, onSetLanguage: (Language) -> Unit) {
    var expanded by rememberSaveable { mutableStateOf(false) }
    val effectiveLanguage =
        if (language == Language.SYSTEM) {
            when (LocalAppLocale.current.substringBefore('-').substringBefore('_')) {
                "ru" -> Language.RUSSIAN
                "kk" -> Language.KAZAKH
                else -> Language.ENGLISH
            }
        } else {
            language
        }
    Box {
        TextButton({ expanded = true }, Modifier.testTag("guest-language")) {
            Text(stringResource(effectiveLanguage.guestLabel), color = MaterialTheme.colorScheme.secondary)
            Icon(
                AppIcons.KeyboardArrowDown,
                null,
                Modifier.padding(start = 6.dp).size(16.dp),
                tint = MaterialTheme.colorScheme.secondary,
            )
        }
        DropdownMenu(expanded, onDismissRequest = { expanded = false }) {
            Language.entries.forEach { option ->
                DropdownMenuItem(
                    text = { Text(stringResource(option.guestLabel)) },
                    onClick = {
                        expanded = false
                        onSetLanguage(option)
                    },
                )
            }
        }
    }
}

@Composable
private fun GuestPreview(tab: MainTab, modifier: Modifier) {
    Column(
        modifier.padding(horizontal = AppSpacing.page, vertical = AppSpacing.large),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.large),
    ) {
        if (tab == MainTab.US) GuestIllustration(tab)
        Text(
            stringResource(
                when (tab) {
                    MainTab.WISHES -> Res.string.guest_wishes_title
                    MainTab.MEMORIES -> Res.string.guest_memories_title
                    MainTab.US -> Res.string.guest_us_title
                },
            ),
            Modifier.fillMaxWidth(),
            style = MaterialTheme.typography.headlineLarge,
            textAlign = TextAlign.Center,
        )
        Text(
            stringResource(
                when (tab) {
                    MainTab.WISHES -> Res.string.guest_wishes_body
                    MainTab.MEMORIES -> Res.string.guest_memories_body
                    MainTab.US -> Res.string.guest_us_body
                },
            ),
            Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.secondary,
            textAlign = TextAlign.Center,
        )
        if (tab == MainTab.US) {
            Column(Modifier.padding(horizontal = AppSpacing.large), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                GuestBenefit(AppIcons.PersonAdd, Res.string.guest_account_each)
                GuestBenefit(AppIcons.FormatListBulleted, Res.string.guest_shared_list)
                GuestBenefit(AppIcons.FavoriteBorder, Res.string.guest_shared_memories)
            }
        } else {
            GuestIllustration(tab)
            GuestExampleCard(tab == MainTab.MEMORIES)
        }
    }
}

@Composable
private fun GuestIllustration(tab: MainTab) {
    Image(
        imageResource(
            when (tab) {
                MainTab.WISHES -> Res.drawable.guest_wishes
                MainTab.MEMORIES -> Res.drawable.guest_memories
                MainTab.US -> Res.drawable.guest_us
            },
        ),
        contentDescription = null,
        modifier =
            Modifier.fillMaxWidth().height(160.dp)
                .clip(if (tab == MainTab.US) AppShapes.image else RoundedCornerShape(20.dp))
                .testTag("guest-illustration"),
        contentScale = ContentScale.Crop,
        filterQuality = FilterQuality.Medium,
    )
}

@Composable
private fun GuestBenefit(icon: ImageVector, label: StringResource) {
    Row(
        Modifier.fillMaxWidth().heightIn(min = 28.dp),
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.medium),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, null, Modifier.size(20.dp), tint = MaterialTheme.colorScheme.primary)
        Text(
            stringResource(label),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.secondary,
        )
    }
}

@Composable
private fun GuestExampleCard(memory: Boolean) {
    Surface(
        Modifier.fillMaxWidth().heightIn(min = if (memory) 102.dp else 94.dp),
        shape = AppShapes.card,
        color = MaterialTheme.colorScheme.surfaceContainerLowest,
        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Row(
            Modifier.padding(AppSpacing.cardInset),
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.medium),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(
                shape = AppShapes.symbol,
                color =
                    if (memory) {
                        MaterialTheme.colorScheme.primaryContainer
                    } else {
                        MaterialTheme.colorScheme.surfaceContainer
                    },
            ) {
                Icon(
                    if (memory) AppIcons.CheckCircle else AppIcons.FavoriteBorder,
                    null,
                    Modifier.padding(horizontal = 10.dp, vertical = 14.dp).size(AppDimensions.icon),
                    tint = MaterialTheme.colorScheme.primary,
                )
            }
            Column(Modifier.weight(1f).heightIn(min = 66.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    stringResource(if (memory) Res.string.guest_memory_title else Res.string.guest_wish_title),
                    style = MaterialTheme.typography.labelLarge,
                )
                Text(
                    stringResource(
                        if (memory) Res.string.guest_memory_description else Res.string.guest_wish_description,
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.secondary,
                )
                Text(
                    stringResource(if (memory) Res.string.guest_memory_label else Res.string.guest_wish_label),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        }
    }
}

@Composable
private fun GuestActions(tab: MainTab, onAuthenticate: (AuthMode) -> Unit) {
    Column(
        Modifier.fillMaxWidth().heightIn(min = 216.dp)
            .padding(horizontal = AppSpacing.page, vertical = AppSpacing.cardInset),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.medium),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        PrimaryButton(stringResource(Res.string.auth_register), { onAuthenticate(AuthMode.REGISTER) })
        OutlinedButton(
            { onAuthenticate(AuthMode.SIGN_IN) },
            Modifier.fillMaxWidth().heightIn(min = 48.dp),
            shape = AppShapes.pill,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.primary),
        ) {
            Text(stringResource(Res.string.guest_log_in))
        }
        Text(
            stringResource(
                when (tab) {
                    MainTab.WISHES -> Res.string.guest_wishes_hint
                    MainTab.MEMORIES -> Res.string.guest_memories_hint
                    MainTab.US -> Res.string.guest_invite_hint
                },
            ),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.secondary,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun GuestNavigation(selected: MainTab, onSelect: (MainTab) -> Unit) {
    Row(
        Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surfaceContainer).selectableGroup()
            .padding(horizontal = AppSpacing.page, vertical = AppSpacing.small),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        MainTab.entries.forEach { tab ->
            val active = selected == tab
            val color = if (active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
            Column(
                Modifier.weight(1f, fill = false).width(104.dp).heightIn(min = 60.dp)
                    .selectable(active, onClick = { onSelect(tab) }, role = Role.Tab),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(AppSpacing.xs),
            ) {
                Box(
                    Modifier.width(64.dp).height(32.dp).clip(AppShapes.pill)
                        .then(
                            if (active) Modifier.background(MaterialTheme.colorScheme.primaryContainer) else Modifier,
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        when (tab) {
                            MainTab.WISHES -> AppIcons.FavoriteBorder
                            MainTab.MEMORIES -> AppIcons.PhotoLibrary
                            MainTab.US -> AppIcons.People
                        },
                        null,
                        Modifier.size(AppDimensions.icon),
                        tint = color,
                    )
                }
                Text(
                    stringResource(
                        when (tab) {
                            MainTab.WISHES -> Res.string.wishes
                            MainTab.MEMORIES -> Res.string.memories
                            MainTab.US -> Res.string.us
                        },
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = color,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}

private val Language.guestLabel: StringResource
    get() =
        when (this) {
            Language.SYSTEM -> Res.string.system_language
            Language.ENGLISH -> Res.string.english_language
            Language.RUSSIAN -> Res.string.russian_language
            Language.KAZAKH -> Res.string.kazakh_language
        }
